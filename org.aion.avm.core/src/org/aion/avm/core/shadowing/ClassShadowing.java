package org.aion.avm.core.shadowing;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.ClassWhiteList;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.DescriptorParser;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.IObject;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Utility for replacing class refs
 *
 * 1) All whitelisted JDK classes are tranformed into their shadow implementations;
 * 2) All method ref to whitelisted JDK class and user defined class will be transformed into it's `avm_` version.
 */
public class ClassShadowing extends ClassToolchain.ToolChainClassVisitor {

    private static final String JAVA_LANG = "java/lang";
    private static final String JAVA_UTIL = "java/util";
    private static final String JAVA_LANG_OBJECT = "java/lang/Object";

    private static final String AVM_JAVA_LANG = "org/aion/avm/java/lang";
    private static final String AVM_INTERNAL_IOBJECT = "org/aion/avm/internal/IObject";

    private static final String METHOD_PREFIX = "avm_";

    private final String shadowPackage;

    private String runtimeClassName;
    private final ClassWhiteList classWhiteList;

    public ClassShadowing(String runtimeClassName, String shadowPackage, ClassWhiteList classWhiteList) {
        super(Opcodes.ASM6);
        this.runtimeClassName = runtimeClassName;
        this.shadowPackage = shadowPackage;
        this.classWhiteList = classWhiteList;
    }

    public ClassShadowing(String runtimeClassName, ClassWhiteList classWhiteList) {
        super(Opcodes.ASM6);
        this.runtimeClassName = runtimeClassName;
        shadowPackage = AVM_JAVA_LANG;
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
                : replaceType(superName, false);
        Stream<String> replacedInterfaces = Stream.of(interfaces).map((oldName) -> replaceType(oldName, true));
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

        String newName = replaceMethodName(name);

        // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
        MethodVisitor mv = super.visitMethod(access, newName, replaceMethodDescriptor(descriptor), null, exceptions);

        return new MethodVisitor(Opcodes.ASM6, mv) {
            @Override
            public void visitInvokeDynamicInsn(String origMethodName, String methodDescriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                final String newMethodName = replaceMethodName(bootstrapMethodHandle.getOwner(), origMethodName);
                final String newMethodDescriptor = replaceMethodDescriptor(methodDescriptor);
                final Handle newHandle = newShadowHandleFrom(bootstrapMethodHandle, false);
                final Object[] newBootstrapMethodArgs = newShadowArgsFrom(bootstrapMethodArguments);
                super.visitInvokeDynamicInsn(newMethodName, newMethodDescriptor, newHandle, newBootstrapMethodArgs);
            }

            private Handle newShadowHandleFrom(Handle origHandle, boolean shadowMethodDescriptor) {
                final String owner = origHandle.getOwner();
                final String newOwner = replaceType(owner, true);
                final String newMethodName = replaceMethodName(owner, origHandle.getName());
                final String newMethodDescriptor = shadowMethodDescriptor ? replaceMethodDescriptor(origHandle.getDesc()) : origHandle.getDesc();
                return new Handle(origHandle.getTag(), newOwner, newMethodName, newMethodDescriptor, origHandle.isInterface());
            }

            private Object[] newShadowArgsFrom(Object[] origArgs) {
                final var newArgs = new ArrayList<>(origArgs.length);
                for (int i = 0; i < origArgs.length; i++) {
                    final Object origArg = origArgs[i];
                    final Object newArg;
                    {
                        if (origArg instanceof org.objectweb.asm.Type) {
                            newArg = newMethodTypeFrom((org.objectweb.asm.Type) origArg);
                        } else if (origArg instanceof org.objectweb.asm.Handle) {
                            newArg = newShadowHandleFrom((org.objectweb.asm.Handle) origArg, true);
                        } else {
                            newArg = origArg;
                        }
                    }
                    newArgs.add(newArg);
                }
                return newArgs.toArray();
            }

            private org.objectweb.asm.Type newMethodTypeFrom(org.objectweb.asm.Type origType) {
                return org.objectweb.asm.Type.getMethodType(replaceMethodDescriptor(origType.getDescriptor()));
            }

            @Override
            public void visitMethodInsn(
                    final int opcode,
                    final String owner,
                    final String name,
                    final String descriptor,
                    final boolean isInterface) {

                // don't replace methods of internal classes, introduced by instrumentation
                String newName = replaceMethodName(owner, name);

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
                    String newOwner = replaceType(owner, allowInterfaceReplacement);
                    super.visitMethodInsn(newOpcode, newOwner, newName, replaceMethodDescriptor(descriptor), newIsInterface);
                }
            }

            @Override
            public void visitTypeInsn(final int opcode, final String type) {
                super.visitTypeInsn(opcode, replaceType(type, true));
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                String newOwner = replaceType(owner, true);
                String newDescriptor = replaceMethodDescriptor(descriptor);

                // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
                super.visitFieldInsn(opcode, newOwner, name, newDescriptor);
            }

            @Override
            public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                super.visitLocalVariable(name, replaceMethodDescriptor(descriptor), signature, start, end, index);
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
                            valueToWrite = Type.getType(replaceMethodDescriptor(type.getDescriptor()));
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
                    String methodDescriptor = "(Ljava/lang/String;)Lorg/aion/avm/java/lang/String;";
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
        String newDescriptor = replaceMethodDescriptor(descriptor);

        // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
        return super.visitField(access, name, newDescriptor, null, value);
    }

    /**
     * Update the class reference if the type is a white-listed JDK class which starts with {@link #JAVA_LANG}.
     *
     * @param type
     * @param allowInterfaceReplacement If true, we will use IObject instead of our shadow Object when replacing java/lang/Object
     * @return
     */
//    protected String replaceType(String type) {
//        return Stream.of(JAVA_LANG, JAVA_UTIL)
//                .filter(type::startsWith)
//                .findFirst()
//                .map(s -> shadowPackage + type.substring(s.length()))
//                .orElse(type);
//    }
    protected String replaceType(String type, boolean allowInterfaceReplacement) {
        // Note that this assumes we have an agreement with the ClassWhiteList regarding what the JAVA_LANG prefix is
        // but this is unavoidable since it is a high-level interface and we are doing low-level string replacement.
        boolean shouldReplacePrefix = classWhiteList.isJdkClass(type);
        if (shouldReplacePrefix) {
            // This assertion verifies that these agree (in the future, we probably want to source them from the same place and avoid the direct string manipulation, here).
            // (technically, the white-list check is more restrictive than this since it can know about sub-packages while this doesn't).
            Assert.assertTrue(type.startsWith(JAVA_LANG) || type.startsWith(JAVA_UTIL));
        }

        // Handle the 3 relevant cases, independently.
        boolean isTypeJavaLangObject = JAVA_LANG_OBJECT.equals(type);
        if (allowInterfaceReplacement && isTypeJavaLangObject) {
            return AVM_INTERNAL_IOBJECT;
        } else if (isTypeJavaLangObject) {
            return AVM_JAVA_LANG + type.substring(JAVA_LANG.length());
        } else if (shouldReplacePrefix) {
            return Stream.of(JAVA_LANG, JAVA_UTIL)
                    .filter(type::startsWith)
                    .findFirst()
                    .map(s -> shadowPackage + type.substring(s.length()))
                    .orElse(type);
        } else {
            return type;
        }
    }

    /**
     * Update the method reference if the owner type is a white-listed JDK class.
     *
     * @param type
     * @return
     */
    protected String replaceMethodName(String type, String methodName) {
        if (this.classWhiteList.isInWhiteList(type)) {
            return replaceMethodName(methodName);
        } else {
            return methodName;
        }
    }

    protected String replaceMethodName(String methodName) {
        return (methodName.equals("<init>") || methodName.equals("<clinit>")) ? methodName : METHOD_PREFIX + methodName;
    }

    /**
     * Update the method descriptor if it uses any white-listed JDK type.
     *
     * @param methodDescriptor
     * @return
     */
    protected String replaceMethodDescriptor(String methodDescriptor) {
         StringBuilder sb = DescriptorParser.parse(methodDescriptor, new DescriptorParser.Callbacks<StringBuilder>() {
            @Override
            public StringBuilder readObject(int arrayDimensions, String type, StringBuilder userData) {
                populateArray(userData, arrayDimensions);
                userData.append(DescriptorParser.OBJECT_START);
                userData.append(replaceType(type, true));
                userData.append(DescriptorParser.OBJECT_END);
                return userData;
            }
            @Override
            public StringBuilder readBoolean(int arrayDimensions, StringBuilder userData) {
                populateArray(userData, arrayDimensions);
                userData.append(DescriptorParser.BOOLEAN);
                return userData;
            }
            @Override
            public StringBuilder readShort(int arrayDimensions, StringBuilder userData) {
                populateArray(userData, arrayDimensions);
                userData.append(DescriptorParser.SHORT);
                return userData;
            }
            @Override
            public StringBuilder readLong(int arrayDimensions, StringBuilder userData) {
                populateArray(userData, arrayDimensions);
                userData.append(DescriptorParser.LONG);
                return userData;
            }
            @Override
            public StringBuilder readInteger(int arrayDimensions, StringBuilder userData) {
                populateArray(userData, arrayDimensions);
                userData.append(DescriptorParser.INTEGER);
                return userData;
            }
            @Override
            public StringBuilder readFloat(int arrayDimensions, StringBuilder userData) {
                populateArray(userData, arrayDimensions);
                userData.append(DescriptorParser.FLOAT);
                return userData;
            }
            @Override
            public StringBuilder readDouble(int arrayDimensions, StringBuilder userData) {
                populateArray(userData, arrayDimensions);
                userData.append(DescriptorParser.DOUBLE);
                return userData;
            }
            @Override
            public StringBuilder readChar(int arrayDimensions, StringBuilder userData) {
                populateArray(userData, arrayDimensions);
                userData.append(DescriptorParser.CHAR);
                return userData;
            }
            @Override
            public StringBuilder readByte(int arrayDimensions, StringBuilder userData) {
                populateArray(userData, arrayDimensions);
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
            private void populateArray(StringBuilder builder, int dimensions) {
                for (int i = 0; i < dimensions; ++i) {
                    builder.append(DescriptorParser.ARRAY);
                }
            }
        }, new StringBuilder());

        return sb.toString();
    }
}
