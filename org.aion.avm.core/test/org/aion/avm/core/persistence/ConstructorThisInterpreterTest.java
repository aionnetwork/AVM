package org.aion.avm.core.persistence;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;


public class ConstructorThisInterpreterTest {
    // NOTE:  Output is ONLY produced if REPORT is set to true.
    private static final boolean REPORT = false;

    @Test
    public void examineTestConstructor() throws Exception {
        MethodNode node = buildTestConstructor();
        Analyzer<ConstructorThisInterpreter.ThisValue> analyzer = new Analyzer<>(new ConstructorThisInterpreter());
        Frame<ConstructorThisInterpreter.ThisValue>[] frames = analyzer.analyze(ConstructorThisInterpreterTest.class.getName(), node);
        
        // Below are the totals derived from manually inspecting the bytecode below and how we interact with it.
        int bytecodeCount = 17;
        int explicitFrameCount = 2;
        int labelCount = 2;
        Assert.assertEquals(bytecodeCount + explicitFrameCount + labelCount, frames.length);
        
        // To make this clear (since this is not obvious), we write the frame stacks.
        for (Frame<ConstructorThisInterpreter.ThisValue> frame : frames) {
            int size = frame.getStackSize();
            report(size + ": ");
            for (int i = size; i > 0; --i) {
                ConstructorThisInterpreter.ThisValue val = frame.getStack(i-1);
                String value = (null != val)
                        ? (val.isThis ? "T" : "F")
                        : "_";
                report(value);
            }
            reportLine();
        }
        
        // Now, verify the top of the stack at each bytecode.
        int index = 0;
        Assert.assertEquals(null, peekStackTop(frames[index++]));
        // ALOAD
        Assert.assertEquals(true, peekStackTop(frames[index++]).isThis);
        // INVOKESPECIAL
        Assert.assertEquals(null, peekStackTop(frames[index++]));
        // ICONST_5
        Assert.assertEquals(false, peekStackTop(frames[index++]).isThis);
        // ILOAD
        Assert.assertEquals(false, peekStackTop(frames[index++]).isThis);
        // IF_ICMPNE
        Assert.assertEquals(null, peekStackTop(frames[index++]));
        // ALOAD
        Assert.assertEquals(true, peekStackTop(frames[index++]).isThis);
        // GOTO
        Assert.assertEquals(null, peekStackTop(frames[index++]));
        // (label)
        Assert.assertEquals(null, peekStackTop(frames[index++]));
        // (frame)
        Assert.assertEquals(null, peekStackTop(frames[index++]));
        // ALOAD
        Assert.assertEquals(false, peekStackTop(frames[index++]).isThis);
        // (label)
        Assert.assertEquals(false, peekStackTop(frames[index++]).isThis);
        // (frame)
        Assert.assertEquals(false, peekStackTop(frames[index++]).isThis);
        // ILOAD
        Assert.assertEquals(false, peekStackTop(frames[index++]).isThis);
        // PUTFIELD
        Assert.assertEquals(null, peekStackTop(frames[index++]));
        // ALOAD
        Assert.assertEquals(true, peekStackTop(frames[index++]).isThis);
        // ICONST_1
        Assert.assertEquals(false, peekStackTop(frames[index++]).isThis);
        // PUTFIELD
        Assert.assertEquals(null, peekStackTop(frames[index++]));
        // ALOAD
        Assert.assertEquals(false, peekStackTop(frames[index++]).isThis);
        // ICONST_2
        Assert.assertEquals(false, peekStackTop(frames[index++]).isThis);
        // PUTFIELD
        Assert.assertEquals(null, peekStackTop(frames[index++]));
        // RETURN
        Assert.assertEquals(frames.length, index);
    }

    @Test
    public void examineSecondConstructor() throws Exception {
        MethodNode node = buildSecondConstructor();
        Analyzer<ConstructorThisInterpreter.ThisValue> analyzer = new Analyzer<>(new ConstructorThisInterpreter());
        Frame<ConstructorThisInterpreter.ThisValue>[] frames = analyzer.analyze(ConstructorThisInterpreterTest.class.getName(), node);
        
        // Below are the totals derived from manually inspecting the bytecode below and how we interact with it.
        int bytecodeCount = 6;
        int explicitFrameCount = 0;
        int labelCount = 0;
        Assert.assertEquals(bytecodeCount + explicitFrameCount + labelCount, frames.length);
        
        // To make this clear (since this is not obvious), we write the frame stacks.
        for (Frame<ConstructorThisInterpreter.ThisValue> frame : frames) {
            int size = frame.getStackSize();
            report(size + ": ");
            for (int i = size; i > 0; --i) {
                ConstructorThisInterpreter.ThisValue val = frame.getStack(i-1);
                String value = (null != val)
                        ? (val.isThis ? "T" : "F")
                        : "_";
                report(value);
            }
            reportLine();
        }
        
        // Now, verify the top of the stack at each bytecode.
        int index = 0;
        Assert.assertEquals(null, peekStackTop(frames[index++]));
        // ALOAD
        Assert.assertEquals(true, peekStackTop(frames[index++]).isThis);
        // ALOAD
        Assert.assertEquals(false, peekStackTop(frames[index++]).isThis);
        // PUTFIELD
        Assert.assertEquals(null, peekStackTop(frames[index++]));
        // ALOAD
        Assert.assertEquals(true, peekStackTop(frames[index++]).isThis);
        // INVOKESPECIAL
        Assert.assertEquals(null, peekStackTop(frames[index++]));
        // RETURN
        Assert.assertEquals(frames.length, index);
    }


    private ConstructorThisInterpreter.ThisValue peekStackTop(Frame<ConstructorThisInterpreter.ThisValue> frame) {
        int size = frame.getStackSize();
        return (size > 0)
                ? frame.getStack(size - 1)
                : null;
    }

    private MethodNode buildTestConstructor() {
        MethodNode methodVisitor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "(Lorg/aion/avm/core/persistence/ConstructorThisInterpreterTest;I)V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        methodVisitor.visitInsn(Opcodes.ICONST_5);
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 2);
        Label label2 = new Label();
        methodVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, label2);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        Label label3 = new Label();
        methodVisitor.visitJumpInsn(Opcodes.GOTO, label3);
        methodVisitor.visitLabel(label2);
        methodVisitor.visitFrame(Opcodes.F_FULL, 3, new Object[] {"org/aion/avm/core/persistence/ConstructorThisInterpreterTest", "org/aion/avm/core/persistence/ConstructorThisInterpreterTest", Opcodes.INTEGER}, 0, new Object[] {});
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        methodVisitor.visitLabel(label3);
        methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"org/aion/avm/core/persistence/ConstructorThisInterpreterTest"});
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 2);
        methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, "org/aion/avm/core/persistence/ConstructorThisInterpreterTest", "bar", "I");
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitInsn(Opcodes.ICONST_1);
        methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, "org/aion/avm/core/persistence/ConstructorThisInterpreterTest", "bar", "I");
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        methodVisitor.visitInsn(Opcodes.ICONST_2);
        methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, "org/aion/avm/core/persistence/ConstructorThisInterpreterTest", "bar", "I");
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitMaxs(2, 3);
        methodVisitor.visitEnd();
        return methodVisitor;
    }

    private MethodNode buildSecondConstructor() {
        MethodNode methodVisitor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "(Lorg/aion/avm/core/NonStaticInnerClassTarget$Inner;)V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, "org/aion/avm/core/NonStaticInnerClassTarget$Inner$Deeper", "this$1", "Lorg/aion/avm/core/NonStaticInnerClassTarget$Inner;");
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();
        return methodVisitor;
    }

    private static void report(String output) {
        if (REPORT) {
            System.out.print(output);
        }
    }

    private static void reportLine() {
        if (REPORT) {
            System.out.println();
        }
    }
}
