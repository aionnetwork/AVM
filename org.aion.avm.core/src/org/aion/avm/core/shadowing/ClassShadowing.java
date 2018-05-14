package org.aion.avm.core.shadowing;

import org.objectweb.asm.*;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility for replacing class refs
 */
public class ClassShadowing {

    private static final String JAVA_LANG = "java/lang";
    private static final String JAVA_LANG_SHADOW = "org/aion/avm/java/lang";

    private static final String METHOD_PREFIX = "avm_";

    /**
     * Modify the class reference if the type starts with {@link #JAVA_LANG}.
     *
     * @param type
     * @return
     */
    private static String replaceType(String type) {
        return type.startsWith(JAVA_LANG) ? JAVA_LANG_SHADOW + type.substring(JAVA_LANG.length()) : type;
    }

    /**
     * Modify the method reference if the owner type starts with {@link #JAVA_LANG}.
     *
     * @param type
     * @return
     */
    private static String replaceMethodName(String type, String methodName) {
        if (type.startsWith(JAVA_LANG)) {
            return methodName.equals("<init>") ? methodName : METHOD_PREFIX + methodName;
        } else {
            return methodName;
        }
    }

    /**
     * Modify the method descriptor if it uses any type that starts with {@link #JAVA_LANG}
     *
     * @param methodDescriptor
     * @return
     */
    private static String replaceMethodDescriptor(String methodDescriptor) {
        StringBuilder sb = new StringBuilder();

        int from = 0;
        while (from < methodDescriptor.length()) {
            from = readType(sb, methodDescriptor, from);
        }

        return sb.toString();
    }

    private static int readType(StringBuilder sb, String methodDescriptor, int from) {
        char c = methodDescriptor.charAt(from);

        switch (c) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 'V':
                sb.append(c);
                return from + 1;
            case 'L': {
                sb.append(c);
                int idx = methodDescriptor.indexOf(';', from);
                sb.append(replaceType(methodDescriptor.substring(from + 1, idx)));
                sb.append(';');
                return idx + 1;
            }
            case '[': {
                sb.append(c);
                return readType(sb, methodDescriptor, from + 1);
            }
            case '(': {
                sb.append(c);
                int idx = methodDescriptor.indexOf(')', from);
                sb.append(replaceMethodDescriptor(methodDescriptor.substring(from + 1, idx)));
                sb.append(')');
                return idx + 1;
            }
            default:
                throw new RuntimeException("Failed to parse type: descriptor = " + methodDescriptor + ", from = " + from);
        }
    }


    public static byte[] replaceJavaLang(byte[] classFile) {
        ClassReader in = new ClassReader(classFile);
        ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM6, out) {

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
                String[] newInterfaces = Stream.of(interfaces).map(i -> replaceType(i)).collect(Collectors.toList()).stream().toArray(String[]::new);
                out.visit(version, access, name, signature, newSuperName, newInterfaces);
            }

            @Override
            public MethodVisitor visitMethod(
                    final int access,
                    final String name,
                    final String descriptor,
                    final String signature,
                    final String[] exceptions) {

                MethodVisitor mv = super.visitMethod(access, name, replaceMethodDescriptor(descriptor), signature, exceptions);

                return new MethodVisitor(Opcodes.ASM6, mv) {
                    @Override
                    public void visitMethodInsn(
                            final int opcode,
                            final String owner,
                            final String name,
                            final String descriptor,
                            final boolean isInterface) {
                        mv.visitMethodInsn(opcode, replaceType(owner), replaceMethodName(owner, name), replaceMethodDescriptor(descriptor), isInterface);
                    }

                    @Override
                    public void visitTypeInsn(final int opcode, final String type) {
                        mv.visitTypeInsn(opcode, replaceType(type));
                    }

                    @Override
                    public void visitLdcInsn(final Object value) {
                        if (value instanceof Type) {
                            Type type = (Type) value;
                            // TODO: how to deal with METHOD and HANDLE?
                            if (type.getSort() == Type.ARRAY || type.getSort() == Type.OBJECT) {
                                mv.visitLdcInsn(Type.getType(replaceMethodDescriptor(type.getDescriptor())));
                                return;
                            }
                        }

                        mv.visitLdcInsn(value);
                    }
                };
            }
        };
        in.accept(visitor, ClassReader.SKIP_DEBUG);

        return out.toByteArray();
    }
}
