package org.aion.avm.core.shadowing;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.ClassWhiteList;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.IObject;
import org.objectweb.asm.*;

import java.util.stream.Stream;

/**
 * Utility for replacing class refs
 *
 * 1) All whitelisted JDK classes are tranformed into their shadow implementations;
 * 2) All method ref to whitelisted JDK class and user defined class will be transformed into it's `avm_` version.
 */
public class ClassShadowing extends ClassToolchain.ToolChainClassVisitor {
    private static final String JAVA_LANG_OBJECT = "java/lang/Object";
    private static final String AVM_JAVA_LANG = "org/aion/avm/java/lang";

    private final String runtimeClassName;
    private final ClassWhiteList classWhiteList;
    private final Replacer replacer;
    public static final String METHOD_PREFIX = "avm_";


    public ClassShadowing(String runtimeClassName, String shadowPackage, ClassWhiteList classWhiteList) {
        super(Opcodes.ASM6);
        this.replacer = new Replacer(shadowPackage, classWhiteList);
        this.runtimeClassName = runtimeClassName;
        this.classWhiteList = classWhiteList;
    }

    public ClassShadowing(String runtimeClassName, ClassWhiteList classWhiteList) {
        super(Opcodes.ASM6);
        this.replacer = new Replacer(AVM_JAVA_LANG, classWhiteList);
        this.runtimeClassName = runtimeClassName;
        this.classWhiteList = classWhiteList;
    }

    @Override
    public void visit(
            final int version,
            final int access,
            final String name,
            final String signature,
            final String superName,
            final String[] interfaces) {

        Assert.assertTrue(!this.classWhiteList.isJdkClass(name));

        // Note that we can't change the superName if this is an interface (since those all must specify "java/lang/Object").
        boolean isInterface = (0 != (Opcodes.ACC_INTERFACE & access));
        String newSuperName = isInterface
                ? superName
                : replacer.replaceType(superName, false);
        Stream<String> replacedInterfaces = Stream.of(interfaces).map((oldName) -> replacer.replaceType(oldName, true));
        // If this is an interface, we need to add our "root interface" so that we have a unification point between the interface and our shadow Object.
        if (isInterface) {
            String rootInterfaceName = Helpers.fulllyQualifiedNameToInternalName(IObject.class.getName());
            replacedInterfaces = Stream.concat(replacedInterfaces, Stream.of(rootInterfaceName));
        }

        String[] newInterfaces = replacedInterfaces.toArray(String[]::new);

        // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
        super.visit(version, access, name, null, newSuperName, newInterfaces);
    }

    @Override
    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {

        String newName = replacer.replaceMethodName(name);

        // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
        MethodVisitor mv = super.visitMethod(access, newName, replacer.replaceMethodDescriptor(descriptor), null, exceptions);

        return new MethodVisitor(Opcodes.ASM6, mv) {
            @Override
            public void visitMethodInsn(
                    final int opcode,
                    final String owner,
                    final String name,
                    final String descriptor,
                    final boolean isInterface) {

                // don't replace methods of internal classes, introduced by instrumentation
                String newName = replacer.replaceMethodName(owner, name);

                // Note that it is possible we will see calls from other phases in the chain and we don't want to re-write them
                // (often, they _are_ the bridging code).
                if ((Opcodes.INVOKESTATIC == opcode) && runtimeClassName.equals(owner)) {
                    super.visitMethodInsn(opcode, owner, newName, descriptor, isInterface);
                } else {
                    // Due to our use of the IObject interface at the root of the shadow type hierarchy (issue-80), we may need to replace this invokevirtual
                    // opcode and/or the owner of the call we are making.
                    // If this is invokespecial, it is probably something like "super.<init>" so we can't replace the opcode or type.
                    boolean allowInterfaceReplacement = (Opcodes.INVOKESPECIAL != opcode);
                    // If this is java/lang/Object, and we aren't in one of those invokespecial cases, we probably need to treat this as an interface.
                    boolean newIsInterface = JAVA_LANG_OBJECT.equals(owner)
                            ? allowInterfaceReplacement
                            : isInterface;
                    // If we are changing to the interface, change the opcode.
                    int newOpcode = (newIsInterface && (Opcodes.INVOKEVIRTUAL == opcode))
                            ? Opcodes.INVOKEINTERFACE
                            : opcode;
                    // We need to shadow the owner type, potentially replacing it with the IObject type.
                    String newOwner = replacer.replaceType(owner, allowInterfaceReplacement);
                    super.visitMethodInsn(newOpcode, newOwner, newName, replacer.replaceMethodDescriptor(descriptor), newIsInterface);
                }
            }

            @Override
            public void visitTypeInsn(final int opcode, final String type) {
                super.visitTypeInsn(opcode, replacer.replaceType(type, true));
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                String newOwner = replacer.replaceType(owner, true);
                String newDescriptor = replacer.replaceMethodDescriptor(descriptor);

                // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
                super.visitFieldInsn(opcode, newOwner, name, newDescriptor);
            }

            @Override
            public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                super.visitLocalVariable(name, replacer.replaceMethodDescriptor(descriptor), signature, start, end, index);
            }

            @Override
            public void visitLdcInsn(final Object value) {
                // We will default to passing the value through since only 1 case does anything different.
                Object valueToWrite = value;
                boolean shouldWrapAsString = false;
                boolean shouldWrapAsClass = false;

                if (value instanceof Type) {
                    Type type = (Type) value;
                    // TODO: how to deal with METHOD and HANDLE?
                    switch (type.getSort()) {
                        case Type.ARRAY: {
                            Assert.unimplemented("Array type sort doesn't seem to appear in this usage.");
                            break;
                        }
                        case Type.BOOLEAN:
                        case Type.BYTE:
                        case Type.CHAR:
                        case Type.DOUBLE:
                        case Type.FLOAT:
                        case Type.INT:
                        case Type.LONG:
                        case Type.SHORT: {
                            // These primitive require no special handling - just emit the instruction.
                            break;
                        }
                        case Type.METHOD: {
                            // The method constant sort should only show up related to either invokedynamic or reflection and we don't support those cases.
                            Assert.unreachable("Method constants cannot be loaded should have been filtered earlier.");
                            break;
                        }
                        case Type.OBJECT: {
                            // This is the interesting case where we might need to replace the descriptor.
                            valueToWrite = Type.getType(replacer.replaceMethodDescriptor(type.getDescriptor()));
                            // This is also the case where we want to wrap this as a class (since strings go through their own path).
                            shouldWrapAsClass = true;
                            break;
                        }
                        case Type.VOID: {
                            Assert.unreachable("Void constants cannot be loaded.");
                            break;
                        }
                        default:
                            Assert.unreachable("Unknown type: " + type.getSort());
                    }
                } else if (value instanceof String) {
                    shouldWrapAsString = true;
                }

                // All paths emit the instruction.
                super.visitLdcInsn(valueToWrite);

                // If we need to wrap this, call out to our static helper.
                if (shouldWrapAsString) {
                    String methodName = "wrapAsString";
                    String methodDescriptor = "(Ljava/lang/String;)" + Replacer.SHADOW_WRAPPED_STRING_TYPE;
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeClassName, methodName, methodDescriptor, false);
                } else if (shouldWrapAsClass) {
                    String methodName = "wrapAsClass";
                    String methodDescriptor = "(Ljava/lang/Class;)Lorg/aion/avm/java/lang/Class;";
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeClassName, methodName, methodDescriptor, false);
                }
            }
        };
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        String newDescriptor = replacer.replaceMethodDescriptor(descriptor);

        // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
        return super.visitField(access, name, newDescriptor, null, value);
    }
}
