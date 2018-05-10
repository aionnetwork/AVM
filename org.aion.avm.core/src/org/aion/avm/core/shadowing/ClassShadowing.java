package org.aion.avm.core.shadowing;

import org.objectweb.asm.*;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility for replacing class refs
 */
public class ClassShadowing {

    public static byte[] replaceClassRef(byte[] classFile, Map<String, String> map) {
        ClassReader in = new ClassReader(classFile);
        ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM6, out) {

            @Override
            public void visit(
                    final int version,
                    final int access,
                    final String name,
                    final String signature,
                    final String superName,
                    final String[] interfaces) {
                System.out.println("class: " + name + " extends " + superName);

                String newSuperName = map.containsKey(superName) ? map.get(superName) : superName;
                String[] newInterfaces = Stream.of(interfaces).map(i -> map.containsKey(i) ? map.get(i) : i).collect(Collectors.toList()).stream().toArray(String[]::new);
                out.visit(version, access, name, signature, newSuperName, newInterfaces);
            }

            @Override
            public MethodVisitor visitMethod(
                    final int access,
                    final String name,
                    final String descriptor,
                    final String signature,
                    final String[] exceptions) {
                System.out.println("method: " + name + " " + descriptor);
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

                return new MethodVisitor(Opcodes.ASM6, mv) {
                    @Override
                    public void visitMethodInsn(
                            final int opcode,
                            final String owner,
                            final String name,
                            final String descriptor,
                            final boolean isInterface) {
                        System.out.println("invoke: " + owner + " " + name + " " + descriptor);
                        mv.visitMethodInsn(opcode, map.containsKey(owner) ? map.get(owner) : owner, name, descriptor, isInterface);
                    }
                };
            }
        };
        in.accept(visitor, ClassReader.SKIP_DEBUG);

        return out.toByteArray();
    }
}
