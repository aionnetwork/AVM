package org.aion.avm.core.stacktracking;

import i.Helper;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;

class StackWatcherMethodAdapter extends AdviceAdapter implements Opcodes {

    private int idxDep = -1;    //LVT index of stack depth
    private int idxSize = -1;   //LVT index of stack size
    private int maxL = 0;       //maxLocals for current method
    private int maxS = 0;       //maxStack for current method
    private int tc = 0;         //number of try catch block for current method

    // These values come from an early stage of development where we were computing them ourselves.
    // They are no longer necessary because ClassWriter.COMPUTE_FRAMES computes these values for us
    // and overrides whatever we have specified here. However, these values are required for consensus
    // because they are passed into instrumented code to determine when a stack overflow occurs.
    // See AKI-108 for a more detailed analysis (these values are also not always what they should be).
    private static final int NUM_INSTRUMENTED_LOCALS = 2;
    private static final int NUM_INSTRUMENTED_STACK = 1;

    //List of exception handler code label (aka the start of catch block)
    private ArrayList<Label> catchBlockList = new ArrayList<Label>();

    //JAVA asm Type for later use.
    private Type typeInt = Type.getType(int.class);
    private Type typeHelper = Type.getType("L" + Helper.RUNTIME_HELPER_NAME + ";");

    public StackWatcherMethodAdapter(final GeneratorAdapter mv,
            final int access, final String name, final String desc)
    {
        super(Opcodes.ASM6, mv, access, name, desc);
    }

    public void setMax(MethodNode node, int l, int s){
        this.maxL = l + this.NUM_INSTRUMENTED_LOCALS;
        this.maxS = s + this.NUM_INSTRUMENTED_STACK;
        node.maxLocals = maxL;
        node.maxStack = maxS;
    }

    public void setTryCatchBlockNum(int l){
        this.tc = l;
    }

    @Override
    public void visitCode(){
        super.visitCode();

        // Push the current stack size to operand stack and invoke AVMStackWatcher.enterMethod(int)
        Method m1 = Method.getMethod("void enterMethod(int)");
        visitLdcInsn(this.maxL + this.maxS);
        invokeStatic(typeHelper, m1);

        // If current method has at least one try catch block, we need to generate a StackWacher stamp.
        if (this.tc > 0){
            //invoke AVMStackWatcher.getCurStackDepth() and put the result into LVT
            Method m2 = Method.getMethod("int getCurStackDepth()");
            invokeStatic(typeHelper, m2);
            idxDep = newLocal(typeInt);
            storeLocal(idxDep, typeInt);

            //invoke AVMStackWatcher.getCurStackSize() and put the result into LVT
            Method m3 = Method.getMethod("int getCurStackSize()");
            invokeStatic(typeHelper, m3);
            idxSize = newLocal(typeInt);
            storeLocal(idxSize, typeInt);
        }
    }

    @Override
    protected void onMethodExit(int opcode){
        // Push the current stack size to operand stack and invoke AVMStackWatcher.exitMethod(int)
        Method m1 = Method.getMethod("void exitMethod(int)");
        visitLdcInsn(this.maxL + this.maxS);
        invokeStatic(typeHelper, m1);
    }


    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type){
        // visitTryCatchBlock is guaranteed to be called before the visits of its labels.
        // we keep track of all exception handlers so we can instrument them when they are visited.
        catchBlockList.add(handler);
        mv.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitLabel(Label label){
        mv.visitLabel(label);
        // We instrument the code (start of catch block) if the label we are visiting is an exception handler
        if (catchBlockList.contains(label)){
            // Load the stamp from LVT
            loadLocal(this.idxDep, typeInt);
            loadLocal(this.idxSize, typeInt);
            Method m1 = Method.getMethod("void enterCatchBlock(int, int)");
            invokeStatic(typeHelper, m1);
        }
    }
}
