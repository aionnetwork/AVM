package org.aion.avm.core.miscvisitors;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.DescriptorParser;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.PackageConstants;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Set;
import java.util.stream.Collectors;


/**
 * The user classes can come in associated with essentially any package name but we want to be able to identity them, in later pipeline stages,
 * and ensure that they aren't trying to invade one of our spaces, so we will re-map them all into PackageConstants.kUserDotPrefix, here.
 */
public class UserClassMappingVisitor extends ClassToolchain.ToolChainClassVisitor {
    private final Set<String> userDefinedClassSlashNames;

    public UserClassMappingVisitor(Set<String> userDefinedClassDotNames) {
        super(Opcodes.ASM6);
        
        // Note that the input is given in .-style names, so convert these for our uses.
        this.userDefinedClassSlashNames = userDefinedClassDotNames.stream().map((dotStyle) -> Helpers.fulllyQualifiedNameToInternalName(dotStyle)).collect(Collectors.toSet());
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // We may need to re-map the name, superName, and interfaces.
        String newName = mapType(name);
        String newSuperName = mapType(superName);
        String[] newInterfaces = mapTypeArray(interfaces);
        
        // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
        super.visit(version, access, newName, null, newSuperName, newInterfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        String newName = mapType(name);
        String newDescriptor = mapDescriptor(descriptor);
        String[] newExceptions = mapTypeArray(exceptions);
        
        // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
        MethodVisitor mv = super.visitMethod(access, newName, newDescriptor, null, newExceptions);
        
        return new MethodVisitor(Opcodes.ASM6, mv) {
            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                String newOwner = mapType(owner);
                String newName = mapType(name);
                String newDescriptor = mapDescriptor(descriptor);
                super.visitMethodInsn(opcode, newOwner, newName, newDescriptor, isInterface);
            }
            @Override
            public void visitTypeInsn(final int opcode, final String type) {
                super.visitTypeInsn(opcode, mapType(type));
            }
            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                String newOwner = mapType(owner);
                String newDescriptor = mapDescriptor(descriptor);
                super.visitFieldInsn(opcode, newOwner, name, newDescriptor);
            }
            @Override
            public void visitInvokeDynamicInsn(String methodName, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                String newDescriptor = mapDescriptor(descriptor);
                
                Handle newBootstrapMethodHandle = mapHandle(bootstrapMethodHandle);
                
                Object newArgs[] = new Object[bootstrapMethodArguments.length];
                for (int i = 0; i < bootstrapMethodArguments.length; ++i) {
                    Object arg = bootstrapMethodArguments[i];
                    Object newArg = null;
                    if (arg instanceof Type) {
                        newArg = mapMethodType((Type) arg);
                    } else if (arg instanceof Handle) {
                        newArg = mapHandle((Handle) arg);
                    } else {
                        newArg = arg;
                    }
                    newArgs[i] = newArg;
                }
                
                super.visitInvokeDynamicInsn(methodName, newDescriptor, newBootstrapMethodHandle, newArgs);
            }
            @Override
            public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
                String newDescriptor = mapDescriptor(descriptor);
                super.visitMultiANewArrayInsn(newDescriptor, numDimensions);
            }
            @Override
            public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                String newType = (null != type)
                        ? mapType(type)
                        : null;
                super.visitTryCatchBlock(start, end, handler, newType);
            }
            @Override
            public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
                // We might need to adjust types in locals and stack slots.
                Object[] newLocals = new Object[local.length];
                for (int i = 0; i < local.length; ++i) {
                    if (local[i] instanceof String) {
                        newLocals[i] = mapType((String)local[i]);
                    } else {
                        newLocals[i] = local[i];
                    }
                }
                Object[] newStack = new Object[stack.length];
                for (int i = 0; i < stack.length; ++i) {
                    if (stack[i] instanceof String) {
                        newStack[i] = mapType((String)stack[i]);
                    } else {
                        newStack[i] = stack[i];
                    }
                }
                super.visitFrame(type, nLocal, newLocals, nStack, newStack);
            }
        };
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        String newDescriptor = mapDescriptor(descriptor);

        // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
        return super.visitField(access, name, newDescriptor, null, value);
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        String newOwner = mapType(owner);
        String newName = mapType(name);
        String newDescriptor = mapDescriptor(descriptor);
        super.visitOuterClass(newOwner, newName, newDescriptor);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        String newName = mapType(name);
        String newOuterName = (null != outerName)
                ? mapType(outerName)
                : null;
        String newInnerName = (null != innerName)
                ? mapType(innerName)
                : null;
        super.visitInnerClass(newName, newOuterName, newInnerName, access);
    }


    private Type mapMethodType(Type type) {
        return Type.getMethodType(mapDescriptor(type.getDescriptor()));
    }

    private Handle mapHandle(Handle methodHandle) {
        String methodOwner = methodHandle.getOwner();
        String methodDescriptor = methodHandle.getDesc();
        String newMethodOwner = mapType(methodOwner);
        String newMethodDescriptor = mapDescriptor(methodDescriptor);
        return new Handle(methodHandle.getTag(), newMethodOwner, methodHandle.getName(), newMethodDescriptor, methodHandle.isInterface());
    }

    /**
     * If the given descriptor contains class names is in the user-defined set, it is mapped to the new location.  Otherwise, it is returned, unchanged.
     * 
     * @param descriptor The incoming descriptor.
     * @return The possibly re-mapped descriptor.
     */
    private String mapDescriptor(String descriptor) {
        StringBuilder builder = DescriptorParser.parse(descriptor, new DescriptorParser.Callbacks<>() {
            @Override
            public StringBuilder readObject(int arrayDimensions, String type, StringBuilder userData) {
                writeArrayDimensions(userData, arrayDimensions);
                String newType = mapType(type);
                userData.append(DescriptorParser.OBJECT_START);
                userData.append(newType);
                userData.append(DescriptorParser.OBJECT_END);
                return userData;
            }
            @Override
            public StringBuilder readBoolean(int arrayDimensions, StringBuilder userData) {
                writeArrayDimensions(userData, arrayDimensions);
                userData.append(DescriptorParser.BOOLEAN);
                return userData;
            }
            @Override
            public StringBuilder readShort(int arrayDimensions, StringBuilder userData) {
                writeArrayDimensions(userData, arrayDimensions);
                userData.append(DescriptorParser.SHORT);
                return userData;
            }
            @Override
            public StringBuilder readLong(int arrayDimensions, StringBuilder userData) {
                writeArrayDimensions(userData, arrayDimensions);
                userData.append(DescriptorParser.LONG);
                return userData;
            }
            @Override
            public StringBuilder readInteger(int arrayDimensions, StringBuilder userData) {
                writeArrayDimensions(userData, arrayDimensions);
                userData.append(DescriptorParser.INTEGER);
                return userData;
            }
            @Override
            public StringBuilder readFloat(int arrayDimensions, StringBuilder userData) {
                writeArrayDimensions(userData, arrayDimensions);
                userData.append(DescriptorParser.FLOAT);
                return userData;
            }
            @Override
            public StringBuilder readDouble(int arrayDimensions, StringBuilder userData) {
                writeArrayDimensions(userData, arrayDimensions);
                userData.append(DescriptorParser.DOUBLE);
                return userData;
            }
            @Override
            public StringBuilder readChar(int arrayDimensions, StringBuilder userData) {
                writeArrayDimensions(userData, arrayDimensions);
                userData.append(DescriptorParser.CHAR);
                return userData;
            }
            @Override
            public StringBuilder readByte(int arrayDimensions, StringBuilder userData) {
                writeArrayDimensions(userData, arrayDimensions);
                userData.append(DescriptorParser.BYTE);
                return userData;
            }
            @Override
            public StringBuilder argumentStart(StringBuilder userData) {
                userData.append(DescriptorParser.ARGS_START);
                return userData;
            }
            @Override
            public StringBuilder argumentEnd(StringBuilder userData) {
                userData.append(DescriptorParser.ARGS_END);
                return userData;
            }
            @Override
            public StringBuilder readVoid(StringBuilder userData) {
                userData.append(DescriptorParser.VOID);
                return userData;
            }
            private void writeArrayDimensions(StringBuilder builder, int dimensions) {
                for (int i = 0; i < dimensions; ++i) {
                    builder.append(DescriptorParser.ARRAY);
                }
            }
        }, new StringBuilder());
        
        return builder.toString();
    }

    private String[] mapTypeArray(String[] names) {
        String[] newNames = null;
        if (null != names) {
            newNames = new String[names.length];
            for (int i = 0; i < names.length; ++i) {
                newNames[i] = mapType(names[i]);
            }
        }
        return newNames;
    }

    /**
     * If the given type name is in the user-defined set, it is mapped to the new location.  Otherwise, it is returned, unchanged.
     * NOTE:  This method operates on /-style names.
     * 
     * @param type The incoming type name.
     * @return The possibly re-mapped type.
     */
    private String mapType(String type) {
        Assert.assertTrue(-1 == type.indexOf("."));
        
        String newType= this.userDefinedClassSlashNames.contains(type)
                ? (PackageConstants.kUserSlashPrefix + type)
                : type;
        return newType;
    }
}
