package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.util.Assert;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.*;


public class ArrayWrappingInjector{

    private ClassNode cn;
    public ArrayWrappingInjector(ClassNode cn) {
        this.cn = cn;
    }

    public void injectClass(){

        //System.out.println(cn.name);

        // We perform semantic analysis on each methods
        for (MethodNode method : cn.methods) {

            if (method.instructions.size() > 0) {
                try{
                    Analyzer analyzer = new Analyzer(new ArrayWrappingInterpreter());
                    analyzer.analyze(cn.name, method);
                    Frame[] frames = analyzer.getFrames();
                    // Elements of the frames array now contains info for each instruction
                    // from the analyzed method. BasicInterpreter creates BasicValue, that
                    // is using simplified type system that distinguishes the UNINITIALZED,
                    // INT, FLOAT, LONG, DOUBLE, REFERENCE and RETURNADDRESS types.
                    this.injectMethod(method, frames);
                }catch (AnalyzerException e){
                    System.out.println("Analyzer fail");
                }
            }
        }
    }


    public void injectMethod(MethodNode mn, Frame[] frames){
        AbstractInsnNode[] insns = mn.instructions.toArray();
        for(int i = 0; i < insns.length; i++) {
            AbstractInsnNode insn = insns[i];
            Frame f = frames[i];

            // We only handle aaload here since aastore is generic
            // the log is the following
            // check instruction -> check stack map frame -> replace instruction with invokeV and checkcast
            if (insn.getOpcode() == Opcodes.AALOAD) {
                //we peek the second slot on stack
                BasicValue t = (BasicValue) (f.getStack(1));

                //System.out.println(t);

                //methodNode.instructions.insert(insn, new VarInsnNode(ALOAD, 0));
            }
        }
    }


}
