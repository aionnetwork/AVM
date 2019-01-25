package org.aion.avm.core.miscvisitors;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassGenerator;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


/**
 * The user classes can come in associated with essentially any package name but we want to be able to identity them, in later pipeline stages,
 * and ensure that they aren't trying to invade one of our spaces, so we will re-map them all into PackageConstants.kUserDotPrefix, here.
 *
 * The following mechanical transformation has been applied:
 * 1) User-defined code are moved to `org.aion.avm.user`;
 * 2) All method declarations and references has been prepended with `avm_`;
 * 3) All fields declarations and references has been prepended with `avm_`;
 * 4) TODO: `java.base` types has been replaced with shadow `java.base`, i.e. `org.aion.avm.shadow.**`.
 *
 * NOTE: String & class constant wrapping is in separate class visitor
 */
public class UserClassMappingVisitor extends ClassToolchain.ToolChainClassVisitor {
    private final NamespaceMapper mapper;
    private final boolean debugMode;

    public UserClassMappingVisitor(NamespaceMapper mapper, boolean debugMode) {
        super(Opcodes.ASM6);
        
        this.mapper = mapper;
        this.debugMode = debugMode;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // We may need to re-map the name, superName, and interfaces.
        String newName = this.mapper.mapType(name, debugMode);
        String newSuperName = this.mapper.mapType(superName, debugMode);
        String[] newInterfaces = this.mapper.mapTypeArray(interfaces, debugMode);
        
        // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
        super.visit(version, access, newName, null, newSuperName, newInterfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        String newName = NamespaceMapper.mapMethodName(name);
        String newDescriptor = this.mapper.mapDescriptor(descriptor, debugMode);
        String[] newExceptions = this.mapper.mapTypeArray(exceptions, debugMode);
        
        // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
        MethodVisitor mv = super.visitMethod(access, newName, newDescriptor, null, newExceptions);
        
        return new MethodVisitor(Opcodes.ASM6, mv) {
            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                String newOwner = UserClassMappingVisitor.this.mapper.mapType(owner, debugMode);
                String newName = NamespaceMapper.mapMethodName(name);
                String newDescriptor = UserClassMappingVisitor.this.mapper.mapDescriptor(descriptor, debugMode);
                super.visitMethodInsn(opcode, newOwner, newName, newDescriptor, isInterface);
            }
            @Override
            public void visitTypeInsn(final int opcode, final String type) {
                super.visitTypeInsn(opcode, UserClassMappingVisitor.this.mapper.mapType(type, debugMode));
            }
            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                String newOwner = UserClassMappingVisitor.this.mapper.mapType(owner, debugMode);
                String newName = NamespaceMapper.mapFieldName(name);
                String newDescriptor = UserClassMappingVisitor.this.mapper.mapDescriptor(descriptor, debugMode);
                super.visitFieldInsn(opcode, newOwner, newName, newDescriptor);
            }
            @Override
            public void visitInvokeDynamicInsn(String methodName, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                String newName = NamespaceMapper.mapMethodName(methodName);
                String newDescriptor = UserClassMappingVisitor.this.mapper.mapDescriptor(descriptor, debugMode);

                // NOTE: method descriptor can't be replaced, based on Rom's comments
                Handle newBootstrapMethodHandle = UserClassMappingVisitor.this.mapper.mapHandle(bootstrapMethodHandle, false, debugMode);
                
                Object newArgs[] = new Object[bootstrapMethodArguments.length];
                for (int i = 0; i < bootstrapMethodArguments.length; ++i) {
                    Object arg = bootstrapMethodArguments[i];
                    Object newArg = null;
                    if (arg instanceof Type) {
                        newArg = UserClassMappingVisitor.this.mapper.mapMethodType((Type) arg, debugMode);
                    } else if (arg instanceof Handle) {
                        newArg = UserClassMappingVisitor.this.mapper.mapHandle((Handle) arg, true, debugMode);
                    } else {
                        newArg = arg;
                    }
                    newArgs[i] = newArg;
                }
                
                super.visitInvokeDynamicInsn(newName, newDescriptor, newBootstrapMethodHandle, newArgs);
            }
            @Override
            public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
                String newDescriptor = UserClassMappingVisitor.this.mapper.mapDescriptor(descriptor, debugMode);
                super.visitMultiANewArrayInsn(newDescriptor, numDimensions);
            }
            @Override
            public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                String newType = (null != type)
                        ? UserClassMappingVisitor.this.mapper.mapType(type, debugMode)
                        : null;
                super.visitTryCatchBlock(start, end, handler, newType);
            }
            @Override
            public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
                // We might need to adjust types in locals and stack slots.
                Object[] newLocals = new Object[local.length];
                for (int i = 0; i < local.length; ++i) {
                    if (local[i] instanceof String) {
                        newLocals[i] = UserClassMappingVisitor.this.mapper.mapType((String)local[i], debugMode);
                    } else {
                        newLocals[i] = local[i];
                    }
                }
                Object[] newStack = new Object[stack.length];
                for (int i = 0; i < stack.length; ++i) {
                    if (stack[i] instanceof String) {
                        newStack[i] = UserClassMappingVisitor.this.mapper.mapType((String)stack[i], debugMode);
                    } else {
                        newStack[i] = stack[i];
                    }
                }
                super.visitFrame(type, nLocal, newLocals, nStack, newStack);
            }

            @Override
            public void visitLdcInsn(final Object value) {
                Object valueToWrite = value;
                if (value instanceof Type) {
                    if(((Type) value).getSort() == Type.OBJECT){
                        valueToWrite = Type.getType(UserClassMappingVisitor.this.mapper.mapDescriptor(((Type) value).getDescriptor(), debugMode));
                    }else if (((Type) value).getSort() == Type.ARRAY){
                        valueToWrite = Type.getType("L" + ArrayWrappingClassGenerator.getPreciseArrayWrapperDescriptor(UserClassMappingVisitor.this.mapper.mapDescriptor((((Type) value).getDescriptor()), debugMode)) + ";");
                    }
                }
                super.visitLdcInsn(valueToWrite);
            }
        };
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        String newName = NamespaceMapper.mapFieldName(name);
        String newDescriptor = this.mapper.mapDescriptor(descriptor, debugMode);

        // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
        return super.visitField(access, newName, newDescriptor, null, value);
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        String newOwner = this.mapper.mapType(owner, debugMode);
        String newName = NamespaceMapper.mapMethodName(name);
        String newDescriptor = descriptor == null ? null: this.mapper.mapDescriptor(descriptor, debugMode);
        super.visitOuterClass(newOwner, newName, newDescriptor);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        String newName = this.mapper.mapType(name, debugMode);
        String newOuterName = (null != outerName)
                ? this.mapper.mapType(outerName, debugMode)
                : null;
        String newInnerName = (null != innerName)
                ? innerName // mapType(innerName) TODO: disable because it's simple name, more investigation may be required
                : null;
        super.visitInnerClass(newName, newOuterName, newInnerName, access);
    }
}
