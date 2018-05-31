package org.aion.avm.core.shadowing;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.DescriptorParser;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.stream.Stream;

/**
 * Utility for replacing class refs
 */
public class ClassShadowing extends ClassToolchain.ToolChainClassVisitor {

    private static final String JAVA_LANG = "java/lang";
    private static final String JAVA_LANG_SHADOW = "org/aion/avm/java/lang";

    private static final String METHOD_PREFIX = "avm_";

    private String runtimeClassName;

    public ClassShadowing(String runtimeClassName) {
        super(Opcodes.ASM6);
        this.runtimeClassName = runtimeClassName;
    }

    @Override
    public void visit(
            final int version,
            final int access,
            final String name,
            final String signature,
            final String superName,
            final String[] interfaces) {

        assert (!name.startsWith(JAVA_LANG));

        String newSuperName = replaceType(superName);
        String[] newInterfaces = Stream.of(interfaces).map(this::replaceType).toArray(String[]::new);

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

        // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
        MethodVisitor mv = super.visitMethod(access, name, replaceMethodDescriptor(descriptor), null, exceptions);

        return new MethodVisitor(Opcodes.ASM6, mv) {
            @Override
            public void visitMethodInsn(
                    final int opcode,
                    final String owner,
                    final String name,
                    final String descriptor,
                    final boolean isInterface) {
                // Note that it is possible we will see calls from other phases in the chain and we don't want to re-write them
                // (often, they _are_ the bridging code).
                if ((Opcodes.INVOKESTATIC == opcode) && runtimeClassName.equals(owner)) {
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                } else {
                    super.visitMethodInsn(opcode, replaceType(owner), replaceMethodName(owner, name), replaceMethodDescriptor(descriptor), isInterface);
                }
            }

            @Override
            public void visitTypeInsn(final int opcode, final String type) {
                super.visitTypeInsn(opcode, replaceType(type));
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                String newDescriptor = replaceMethodDescriptor(descriptor);

                // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
                super.visitFieldInsn(opcode, owner, name, newDescriptor);
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
     * Update the class reference if the type starts with {@link #JAVA_LANG}.
     *
     * @param type
     * @return
     */
    protected String replaceType(String type) {
        return type.startsWith(JAVA_LANG) ? JAVA_LANG_SHADOW + type.substring(JAVA_LANG.length()) : type;
    }

    /**
     * Update the method reference if the owner type starts with {@link #JAVA_LANG}.
     *
     * @param type
     * @return
     */
    protected String replaceMethodName(String type, String methodName) {
        if (type.startsWith(JAVA_LANG)) {
            return methodName.equals("<init>") ? methodName : METHOD_PREFIX + methodName;
        } else {
            return methodName;
        }
    }

    /**
     * Update the method descriptor if it uses any type that starts with {@link #JAVA_LANG}
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
                userData.append(replaceType(type));
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
