package org.aion.avm.core.miscvisitors;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * Does a VERY basic invocation of LoopingExceptionStrippingVisitor to verify that it filters out backward exceptions.
 */
public class LoopingExceptionStrippingVisitorTest {
    @Test
    public void testNormalTryCatch() throws Exception {
        String testClassName = "TestClass";
        ClassWriter writer = new ClassWriter(0);
        TryCatchCountingVisitor counter = new TryCatchCountingVisitor(writer);
        LoopingExceptionStrippingVisitor visitor = new LoopingExceptionStrippingVisitor();
        visitor.setDelegate(counter);
        visitor.visit(Opcodes.V10, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, testClassName, null, "java/lang/Object", null);
        
        // Create our labels.
        Label start = new Label();
        Label end = new Label();
        Label handler = new Label();
        String type = null;
        
        // Write a target method.
        MethodVisitor methodVisitor = visitor.visitMethod(Opcodes.ACC_STATIC, "targetMethod", "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitInsn(Opcodes.ACONST_NULL);
        methodVisitor.visitInsn(Opcodes.POP);
        methodVisitor.visitLabel(start);
        methodVisitor.visitInsn(Opcodes.ACONST_NULL);
        methodVisitor.visitInsn(Opcodes.POP);
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitLabel(end);
        methodVisitor.visitLabel(handler);
        methodVisitor.visitInsn(Opcodes.POP);
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitTryCatchBlock(start, end, handler, type);
        methodVisitor.visitMaxs(1, 0);
        methodVisitor.visitEnd();
        
        // Finish.
        visitor.visitEnd();
        
        Assert.assertEquals(1, counter.counter);
    }

    @Test
    public void testLoopingFinally() throws Exception {
        String testClassName = "TestClass";
        ClassWriter writer = new ClassWriter(0);
        TryCatchCountingVisitor counter = new TryCatchCountingVisitor(writer);
        LoopingExceptionStrippingVisitor visitor = new LoopingExceptionStrippingVisitor();
        visitor.setDelegate(counter);
        visitor.visit(Opcodes.V10, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, testClassName, null, "java/lang/Object", null);
        
        // Create our labels.
        Label start = new Label();
        Label end = new Label();
        Label handler = new Label();
        String type = null;
        
        // Write a target method.
        MethodVisitor methodVisitor = visitor.visitMethod(Opcodes.ACC_STATIC, "targetMethod", "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitInsn(Opcodes.ACONST_NULL);
        methodVisitor.visitInsn(Opcodes.POP);
        methodVisitor.visitLabel(start);
        methodVisitor.visitInsn(Opcodes.ACONST_NULL);
        methodVisitor.visitLabel(handler);
        methodVisitor.visitInsn(Opcodes.POP);
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitLabel(end);
        methodVisitor.visitTryCatchBlock(start, end, handler, type);
        methodVisitor.visitMaxs(1, 0);
        methodVisitor.visitEnd();
        
        // Finish.
        visitor.visitEnd();
        
        Assert.assertEquals(0, counter.counter);
    }

    @Test
    public void testBackwardFinally() throws Exception {
        String testClassName = "TestClass";
        ClassWriter writer = new ClassWriter(0);
        TryCatchCountingVisitor counter = new TryCatchCountingVisitor(writer);
        LoopingExceptionStrippingVisitor visitor = new LoopingExceptionStrippingVisitor();
        visitor.setDelegate(counter);
        visitor.visit(Opcodes.V10, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, testClassName, null, "java/lang/Object", null);
        
        // Create our labels.
        Label methodStart = new Label();
        Label start = new Label();
        Label end = new Label();
        Label handler = new Label();
        String type = null;
        
        // Write a target method.
        MethodVisitor methodVisitor = visitor.visitMethod(Opcodes.ACC_STATIC, "targetMethod", "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitJumpInsn(Opcodes.GOTO, methodStart);
        methodVisitor.visitLabel(handler);
        methodVisitor.visitInsn(Opcodes.POP);
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitLabel(methodStart);
        methodVisitor.visitInsn(Opcodes.ACONST_NULL);
        methodVisitor.visitInsn(Opcodes.POP);
        methodVisitor.visitLabel(start);
        methodVisitor.visitInsn(Opcodes.ACONST_NULL);
        methodVisitor.visitInsn(Opcodes.POP);
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitLabel(end);
        methodVisitor.visitTryCatchBlock(start, end, handler, type);
        methodVisitor.visitMaxs(1, 0);
        methodVisitor.visitEnd();
        
        // Finish.
        visitor.visitEnd();
        
        Assert.assertEquals(0, counter.counter);
    }


    private static class TryCatchCountingVisitor extends ClassVisitor {
        public int counter;
        
        public TryCatchCountingVisitor(ClassVisitor classVisitor) {
            super(Opcodes.ASM6, classVisitor);
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
            return new MethodVisitor(Opcodes.ASM6, visitor) {
                @Override
                public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                    TryCatchCountingVisitor.this.counter += 1;
                    super.visitTryCatchBlock(start, end, handler, type);
                }
            };
        }
    }
}
