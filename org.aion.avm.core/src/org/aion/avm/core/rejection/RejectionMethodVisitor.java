package org.aion.avm.core.rejection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.aion.avm.core.miscvisitors.PreRenameClassAccessRules;
import org.aion.avm.core.util.DescriptorParser;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.internal.RuntimeAssertionError;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;


/**
 * Does a simple read-only pass over the loaded method, ensuring it isn't doing anything it isn't allowed to do:
 * -uses bytecode in blacklist
 * -references class not in whitelist
 * -overrides methods which we will not support as the user may expect
 * -issue an invoke initially defined on a class not in whitelist
 * 
 * When a violation is detected, throws the RejectedClassException.
 */
public class RejectionMethodVisitor extends MethodVisitor {
    private final PreRenameClassAccessRules classAccessRules;
    private final NamespaceMapper namespaceMapper;
    private final boolean debugMode;

    public RejectionMethodVisitor(MethodVisitor visitor, PreRenameClassAccessRules classAccessRules, NamespaceMapper namespaceMapper, boolean debugMode) {
        super(Opcodes.ASM6, visitor);
        this.classAccessRules = classAccessRules;
        this.namespaceMapper = namespaceMapper;
        this.debugMode = debugMode;
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        // Filter this.
        return new RejectionAnnotationVisitor();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        // Filter this.
        return new RejectionAnnotationVisitor();
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        // Filter this.
        return new RejectionAnnotationVisitor();
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        // Filter this.
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        // Filter this.
        return new RejectionAnnotationVisitor();
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        // "Non-standard attributes" are not supported, so filter them.
    }

    @Override
    public void visitInsn(int opcode) {
        checkOpcode(opcode);
        super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        checkOpcode(opcode);
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        checkOpcode(opcode);
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        checkOpcode(opcode);
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        checkOpcode(opcode);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (this.classAccessRules.canUserAccessClass(owner)) {
            // Just as a general help to the user (forcing failure earlier), we want to check that, if this is a JCL method, it exists in our shadow.
            // (otherwise, this creates a very late-bound surprise bug).
            if (!isInterface && this.classAccessRules.isJclClass(owner)) {
                boolean didMatch = checkJclMethodExists(owner, name, descriptor);
                if (!didMatch) {
                    RejectedClassException.jclMethodNotImplemented(owner, name, descriptor);
                }
            }
        } else {
            RejectedClassException.nonWhiteListedClass(owner);
        }
        checkOpcode(opcode);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        checkOpcode(opcode);
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        // Filter this.
        return new RejectionAnnotationVisitor();
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        // Filter this.
        return new RejectionAnnotationVisitor();
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        // Filter this.
        return new RejectionAnnotationVisitor();
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        // This is debug data, so filter it out if we're not in debug mode.
        if(debugMode){
            super.visitLocalVariable(name, descriptor, signature, start, end, index);
        }
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        // This is debug data, so filter it out if we're not in debug mode.
        if(debugMode){
            super.visitLineNumber(line, start);
        }
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        super.visitTryCatchBlock(start, end, handler, type);
    }


    private void checkOpcode(int opcode) {
        if (false
                // We reject JSR and RET (although these haven't been generated in a long time, anyway, and aren't allowed in new class files).
                || (Opcodes.JSR == opcode)
                || (Opcodes.RET == opcode)
                
                // We also want to reject instructions which could interact with the thread state:  MONITORENTER, MONITOREXIT.
                || (Opcodes.MONITORENTER == opcode)
                || (Opcodes.MONITOREXIT == opcode)
        ) {
            RejectedClassException.blacklistedOpcode(opcode);
        }
    }

    private boolean checkJclMethodExists(String owner, String name, String descriptor) {
        boolean didMatch = false;
        // Map the owner, name, and descriptor into the shadow space, look up the corresponding class, reflect, and see if this method exists.
        String mappedOwner = this.namespaceMapper.mapType(owner, debugMode);
        String mappedName = NamespaceMapper.mapMethodName(name);
        String mappedDescriptor = this.namespaceMapper.mapDescriptor(descriptor, debugMode);
        // TODO:  Determine if we need to implement some kind of cache for these reflected operations.
        // (if so, it should probably be implemented in NodeEnvironment, since these classes are shared and long-lived).
        try {
            Class<?> ownerShadowClass = NodeEnvironment.singleton.loadSharedClass(Helpers.internalNameToFulllyQualifiedName(mappedOwner));
            if ("<init>".equals(name)) {
                // We need to apply our logic to the constructors.
                for (Constructor<?> constructor : ownerShadowClass.getDeclaredConstructors()) {
                    String oneDesc = buildDescriptor(constructor.getParameterTypes(), Void.TYPE);
                    didMatch = doDescriptorArgsMatch(mappedDescriptor, oneDesc);
                    if (didMatch) {
                        break;
                    }
                }
            } else {
                // We need to apply our logic to normal methods.
                // Note that we aren't checking differences between static/virtual invoke since this check is just to eagerly tell the user if they
                // are going outside of our implemented subset.  This subset doesn't change method receiver nature.
                // We need to walk up the hierarchy, too.
                Class<?> next = ownerShadowClass;
                while (!didMatch && (null != next)) {
                    for (Method method : next.getDeclaredMethods()) {
                        if (mappedName.equals(method.getName())) {
                            String oneDesc = buildDescriptor(method.getParameterTypes(), method.getReturnType());
                            didMatch = doDescriptorArgsMatch(mappedDescriptor, oneDesc);
                            if (didMatch) {
                                break;
                            }
                        }
                    }
                    next = next.getSuperclass();
                }
            }
        } catch (ClassNotFoundException e) {
            // This would have been caught before we got here (class check is done before the method check).
            RuntimeAssertionError.unexpected(e);
        }
        return didMatch;
    }

    private String buildDescriptor(Class<?>[] parameterTypes, Class<?> returnType) {
        StringBuilder builder = new StringBuilder();
        builder.append(DescriptorParser.ARGS_START);
        for (Class<?> one : parameterTypes) {
            writeClass(builder, one);
        }
        builder.append(DescriptorParser.ARGS_END);
        writeClass(builder, returnType);
        return builder.toString();
    }

    private void writeClass(StringBuilder builder, Class<?> clazz) {
        if (clazz.isArray()) {
            builder.append(DescriptorParser.ARRAY);
            writeClass(builder, clazz.getComponentType());
        } else if (!clazz.isPrimitive()) {
            // TODO:  Remove these primitive array special-cases once we integrate the array wrapping mapping logic into the NamespaceMapper.
            // TODO:  Move the explicit IObject->Object special-case into the NamespaceMapper if we can generalize the descriptor case (since,
            // in general, this mapping should not be applied but it is something we need to do for method descriptors, specifically).
            String className = clazz.getName();
            if ((PackageConstants.kArrayWrapperDotPrefix + "ByteArray").equals(className)) {
                builder.append("[B");
            } else if ((PackageConstants.kArrayWrapperDotPrefix + "CharArray").equals(className)) {
                builder.append("[C");
            } else if ((PackageConstants.kInternalDotPrefix + "IObject").equals(className)) {
                builder.append(DescriptorParser.OBJECT_START);
                builder.append(PackageConstants.kShadowSlashPrefix + "java/lang/Object");
                builder.append(DescriptorParser.OBJECT_END);
            } else {
                builder.append(DescriptorParser.OBJECT_START);
                builder.append(Helpers.fulllyQualifiedNameToInternalName(className));
                builder.append(DescriptorParser.OBJECT_END);
            }
        } else if (Byte.TYPE == clazz) {
            builder.append(DescriptorParser.BYTE);
        } else if (Character.TYPE == clazz) {
            builder.append(DescriptorParser.CHAR);
        } else if (Double.TYPE == clazz) {
            builder.append(DescriptorParser.DOUBLE);
        } else if (Float.TYPE == clazz) {
            builder.append(DescriptorParser.FLOAT);
        } else if (Integer.TYPE == clazz) {
            builder.append(DescriptorParser.INTEGER);
        } else if (Long.TYPE == clazz) {
            builder.append(DescriptorParser.LONG);
        } else if (Short.TYPE == clazz) {
            builder.append(DescriptorParser.SHORT);
        } else if (Boolean.TYPE == clazz) {
            builder.append(DescriptorParser.BOOLEAN);
        } else if (Void.TYPE == clazz) {
            builder.append(DescriptorParser.VOID);
        } else {
            // This means we haven't implemented something.
            RuntimeAssertionError.unreachable("Missing descriptor type: " + clazz);
        }
    }

    private boolean doDescriptorArgsMatch(String expected, String check) {
        // We only want to compare what is in the argument list as this kind of match doesn't care about return type.
        // (additionally, our ObjectArray handling means we lose some data in the transformation which would require duplication, here).
        String expectedArgs = expected.substring(1, expected.indexOf(")"));
        String checkArgs = check.substring(1, check.indexOf(")"));
        return expectedArgs.equals(checkArgs);
    }
}
