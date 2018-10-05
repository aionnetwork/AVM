package org.aion.avm.core.arraywrapping;

import org.aion.avm.arraywrapper.BooleanArray;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.IObjectArray;
import org.aion.avm.internal.RuntimeAssertionError;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

/**
 * A method visitor that replace access bytecode
 *
 * AALOAD
 * AASTORE
 * BALOAD
 * BASTORE
 *
 * with corresponding array wrapper virtual call.
 *
 * Static analysis is required with {@link org.aion.avm.core.arraywrapping.ArrayWrappingInterpreter} so it can perform
 * type inference.
 *
 */

class ArrayWrappingMethodAdapterRef extends MethodNode implements Opcodes {

    private String className;
    private MethodVisitor mv;

    public ArrayWrappingMethodAdapterRef(final int access,
                                         final String name,
                                         final String descriptor,
                                         final String signature,
                                         final String[] exceptions,
                                         MethodVisitor mv,
                                         String className)
    {
        super(Opcodes.ASM6, access, name, descriptor, signature, exceptions);
        this.className = className;
        this.mv = mv;
    }

    @Override
    public void visitEnd(){

        Frame<BasicValue>[] frames = null;
        if (instructions.size() > 0) {
            try{
                Analyzer<BasicValue> analyzer = new Analyzer<>(new ArrayWrappingInterpreter());
                analyzer.analyze(this.className, this);
                frames = analyzer.getFrames();
            }catch (AnalyzerException e){
                System.out.println("Analyzer fail :" + this.className);
                System.out.println(e.getMessage());
            }
        }

        AbstractInsnNode[] insns = instructions.toArray();

        if (null != insns && null != frames) {
            RuntimeAssertionError.assertTrue(insns.length == frames.length);
        }

        for(int i = 0; i < insns.length; i++) {
            AbstractInsnNode insn = insns[i];
            Frame<BasicValue> f = frames[i];

            // We only handle aaload here since aastore is generic
            // the log is the following
            // check instruction -> check stack map frame -> replace instruction with invokeV and checkcast
            if (insn.getOpcode() == Opcodes.AALOAD) {
                //we pop the second slot on stack
                f.pop();
                BasicValue t = (BasicValue) (f.pop());
                String targetDesc = t.toString();
                String elementType = ArrayWrappingClassGenerator.getElementType(targetDesc);

                MethodInsnNode invokeVNode =
                    new MethodInsnNode(Opcodes.INVOKEINTERFACE,
                                        Helpers.fulllyQualifiedNameToInternalName(IObjectArray.class.getName()),
                                        "get",
                                        "(I)Ljava/lang/Object;",
                                        true);

                TypeInsnNode checkcastNode =
                    new TypeInsnNode(Opcodes.CHECKCAST,
                                        elementType);

                // Insert indicate reverse order, we want
                // invokevirtual -> checkcast here
                instructions.insert(insn, checkcastNode);
                instructions.insert(insn, invokeVNode);
                instructions.remove(insn);
            }

            if (insn.getOpcode() == Opcodes.AASTORE) {
                //we pop the third slot on stack
                f.pop();
                f.pop();
                BasicValue t = (BasicValue) (f.pop());
                String targetDesc = t.toString();
                String elementType = ArrayWrappingClassGenerator.getElementType(targetDesc);

                MethodInsnNode invokeVNode =
                        new MethodInsnNode(Opcodes.INVOKEINTERFACE,
                                Helpers.fulllyQualifiedNameToInternalName(IObjectArray.class.getName()),
                                "set",
                                "(ILjava/lang/Object;)V",
                                true);

                TypeInsnNode checkcastNode =
                        new TypeInsnNode(Opcodes.CHECKCAST,
                                elementType);

                // Insert indicate reverse order, we want
                // checkcast -> invokevirtual here
                instructions.insert(insn, invokeVNode);
                instructions.insert(insn, checkcastNode);
                instructions.remove(insn);
            }

            if (insn.getOpcode() == Opcodes.BALOAD) {
                f.pop();
                BasicValue t = f.pop();
                String targetDesc = t.toString();

                MethodInsnNode invokeVNode;
                if (targetDesc.equals("[Z")) {
                        invokeVNode = new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                                        Helpers.fulllyQualifiedNameToInternalName(BooleanArray.class.getName()),
                                        "get",
                                        "(I)Z",
                                        false);
                } else {
                        invokeVNode = new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                                        Helpers.fulllyQualifiedNameToInternalName(ByteArray.class.getName()),
                                        "get",
                                        "(I)B",
                                        false);
                }

                instructions.insert(insn, invokeVNode);
                instructions.remove(insn);
            }

            if (insn.getOpcode() == Opcodes.BASTORE) {
                f.pop();
                f.pop();
                BasicValue t = f.pop();
                String targetDesc = t.toString();

                MethodInsnNode invokeVNode;
                if (targetDesc.equals("[Z")) {
                        invokeVNode = new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                                        Helpers.fulllyQualifiedNameToInternalName(BooleanArray.class.getName()),
                                        "set",
                                        "(IZ)V",
                                        false);
                    } else {
                        invokeVNode = new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                                        Helpers.fulllyQualifiedNameToInternalName(ByteArray.class.getName()),
                                        "set",
                                        "(IB)V",
                                        false);
                    }
                instructions.insert(insn, invokeVNode);
                instructions.remove(insn);
            }
        }

        accept(mv);
    }
}
