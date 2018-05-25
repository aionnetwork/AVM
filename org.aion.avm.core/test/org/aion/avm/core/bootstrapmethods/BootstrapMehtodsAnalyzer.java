package org.aion.avm.core.bootstrapmethods;

import org.aion.avm.core.util.Helpers;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

public class BootstrapMehtodsAnalyzer {

    public static void main(String args[]) {
        ClassReader in = new ClassReader(Helpers.readFileToBytes("./out/test/org.aion.avm.core/org/aion/avm/core/bootstrapmethods/TestLambda.class"));
        in.accept(new ClassVisitor(Opcodes.ASM6) {

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                System.out.println("Class: " + name);
            }

            @Override
            public void visitInnerClass(
                    final String name, final String outerName, final String innerName, final int access) {
                System.out.println("Inner class: " + outerName + " <> " + name);
            }

            @Override
            public MethodVisitor visitMethod(
                    final int access,
                    final String name,
                    final String descriptor,
                    final String signature,
                    final String[] exceptions) {
                System.out.println("\nMethod: " + name + ", " + descriptor);

                MethodNode node = new MethodNode(Opcodes.ASM6, access, name, descriptor, signature, exceptions) {
                    @Override
                    public void visitEnd() {
                         for (int i = 0; i < this.instructions.size(); i++) {
                             System.out.println("INST: " + this.instructions.get(i).getOpcode());
                         }
                         System.out.println(this.name + ", " + this.desc);
                    }
                };

                return new MethodVisitor(Opcodes.ASM6, node) {

                    @Override
                    public void visitInvokeDynamicInsn(
                            final String name,
                            final String descriptor,
                            final Handle bootstrapMethodHandle,
                            final Object... bootstrapMethodArguments) {

                        System.out.println("  name: " + name);
                        System.out.println("  descriptor: " + descriptor);
                        System.out.println("  bootstrap method handle: " + bootstrapMethodHandle);
                        for (Object arg : bootstrapMethodArguments) {
                            System.out.println("    argument: " + arg);
                        }

                        node.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
                    }
                };
            }


        }, ClassReader.SKIP_DEBUG);

    }
}
