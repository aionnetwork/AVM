package org.aion.avm.core.arraywrapping;

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
    @SuppressWarnings("unchecked")
    public void visitEnd(){

        Frame[] frames = null;
        if (instructions.size() > 0) {
            try{
                Analyzer analyzer = new Analyzer(new ArrayWrappingInterpreter());
                analyzer.analyze(this.className, this);
                frames = analyzer.getFrames();
            }catch (AnalyzerException e){
                System.out.println("Analyzer fail :" + this.className);
                System.out.println(e.getMessage());
            }
        }

        AbstractInsnNode[] insns = instructions.toArray();

        for(int i = 0; i < insns.length; i++) {
            AbstractInsnNode insn = insns[i];
            Frame f = frames[i];

            // We only handle aaload here since aastore is generic
            // the log is the following
            // check instruction -> check stack map frame -> replace instruction with invokeV and checkcast
            if (insn.getOpcode() == Opcodes.AALOAD) {
                //we peek the second slot on stack
                BasicValue t = (BasicValue) (f.getStack(1));
                String targetDesc = t.toString();
                String elementType = ArrayWrappingBytecodeFactory.getElementType(targetDesc);

                MethodInsnNode invokeVNode =
                    new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                                        "org/aion/avm/arraywrapper/ObjectArray",
                                        "get",
                                        "(I)Ljava/lang/Object;",
                                        false);

                TypeInsnNode checkcastNode =
                    new TypeInsnNode(Opcodes.CHECKCAST,
                                        elementType);

                instructions.insert(insn, checkcastNode);
                instructions.insert(insn, invokeVNode);
                instructions.remove(insn);
            }
        }
        accept(mv);
    }
}
