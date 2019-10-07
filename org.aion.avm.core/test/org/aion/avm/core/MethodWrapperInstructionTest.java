package org.aion.avm.core;

import org.aion.avm.core.instrument.MethodWrapperVisitor;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

public class MethodWrapperInstructionTest {

    @Test
    public void testEmptyParams() {
        Assert.assertEquals(0, MethodWrapperVisitor.getMethodParameterLoadInstructions("()V").length);
    }

    @Test
    public void testSinglePrimitiveParam() {
        Integer[] types = MethodWrapperVisitor.getMethodParameterLoadInstructions("(I)V");
        Assert.assertEquals(1, types.length);
        Assert.assertEquals(Opcodes.ILOAD, (int) types[0]);

        types = MethodWrapperVisitor.getMethodParameterLoadInstructions("(J)V");
        Assert.assertEquals(1, types.length);
        Assert.assertEquals(Opcodes.LLOAD, (int) types[0]);

        types = MethodWrapperVisitor.getMethodParameterLoadInstructions("(S)V");
        Assert.assertEquals(1, types.length);
        Assert.assertEquals(Opcodes.ILOAD, (int) types[0]);

        types = MethodWrapperVisitor.getMethodParameterLoadInstructions("(F)V");
        Assert.assertEquals(1, types.length);
        Assert.assertEquals(Opcodes.FLOAD, (int) types[0]);

        types = MethodWrapperVisitor.getMethodParameterLoadInstructions("(D)V");
        Assert.assertEquals(1, types.length);
        Assert.assertEquals(Opcodes.DLOAD, (int) types[0]);

        types = MethodWrapperVisitor.getMethodParameterLoadInstructions("(B)V");
        Assert.assertEquals(1, types.length);
        Assert.assertEquals(Opcodes.ILOAD, (int) types[0]);

        types = MethodWrapperVisitor.getMethodParameterLoadInstructions("(Z)V");
        Assert.assertEquals(1, types.length);
        Assert.assertEquals(Opcodes.ILOAD, (int) types[0]);

        types = MethodWrapperVisitor.getMethodParameterLoadInstructions("(C)V");
        Assert.assertEquals(1, types.length);
        Assert.assertEquals(Opcodes.ILOAD, (int) types[0]);
    }

    @Test
    public void testSingleArrayParam() {
        Integer[] types = MethodWrapperVisitor.getMethodParameterLoadInstructions("([I)V");
        Assert.assertEquals(1, types.length);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[0]);

        types = MethodWrapperVisitor.getMethodParameterLoadInstructions("([[[[C)V");
        Assert.assertEquals(1, types.length);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[0]);

        types = MethodWrapperVisitor.getMethodParameterLoadInstructions("([Ljava/lang/Object;)V");
        Assert.assertEquals(1, types.length);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[0]);

        types = MethodWrapperVisitor.getMethodParameterLoadInstructions("([[[Ljava/lang/String;)V");
        Assert.assertEquals(1, types.length);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[0]);
    }

    @Test
    public void testSingleEnumParam() {
        Integer[] types = MethodWrapperVisitor.getMethodParameterLoadInstructions("(LSomeClass$SomeEnum;)V");
        Assert.assertEquals(1, types.length);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[0]);
    }

    @Test
    public void testSingleObjectParam() {
        Integer[] types = MethodWrapperVisitor.getMethodParameterLoadInstructions("(Ljava/lang/Object;)V");
        Assert.assertEquals(1, types.length);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[0]);
    }

    @Test
    public void testMultiplePrimitiveParams() {
        Integer[] types = MethodWrapperVisitor.getMethodParameterLoadInstructions("(IJ)V");
        Assert.assertEquals(2, types.length);
        Assert.assertEquals(Opcodes.ILOAD, (int) types[0]);
        Assert.assertEquals(Opcodes.LLOAD, (int) types[1]);

        types = MethodWrapperVisitor.getMethodParameterLoadInstructions("(CZZB)V");
        Assert.assertEquals(4, types.length);
        Assert.assertEquals(Opcodes.ILOAD, (int) types[0]);
        Assert.assertEquals(Opcodes.ILOAD, (int) types[1]);
        Assert.assertEquals(Opcodes.ILOAD, (int) types[2]);
        Assert.assertEquals(Opcodes.ILOAD, (int) types[3]);

        types = MethodWrapperVisitor.getMethodParameterLoadInstructions("(ICFFDJZSB)V");
        Assert.assertEquals(9, types.length);
        Assert.assertEquals(Opcodes.ILOAD, (int) types[0]);
        Assert.assertEquals(Opcodes.ILOAD, (int) types[1]);
        Assert.assertEquals(Opcodes.FLOAD, (int) types[2]);
        Assert.assertEquals(Opcodes.FLOAD, (int) types[3]);
        Assert.assertEquals(Opcodes.DLOAD, (int) types[4]);
        Assert.assertEquals(Opcodes.LLOAD, (int) types[5]);
        Assert.assertEquals(Opcodes.ILOAD, (int) types[6]);
        Assert.assertEquals(Opcodes.ILOAD, (int) types[7]);
        Assert.assertEquals(Opcodes.ILOAD, (int) types[8]);
    }

    @Test
    public void testMultipleArrayParams() {
        Integer[] types = MethodWrapperVisitor.getMethodParameterLoadInstructions("([I[[[[J[[C)V");
        Assert.assertEquals(3, types.length);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[0]);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[1]);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[2]);

        types = MethodWrapperVisitor.getMethodParameterLoadInstructions("([Ljava/lang/Object;[[[Ljava/lang/String;)V");
        Assert.assertEquals(2, types.length);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[0]);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[1]);

        types = MethodWrapperVisitor.getMethodParameterLoadInstructions("([I[[[Ljava/lang/Object;[Z[Z[Ljava/lang/String;[[D)V");
        Assert.assertEquals(6, types.length);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[0]);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[1]);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[2]);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[3]);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[4]);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[5]);
    }

    @Test
    public void testMultipleObjectParams() {
        Integer[] types = MethodWrapperVisitor.getMethodParameterLoadInstructions("(Ljava/lang/String;Ljava/lang/Object;)V");
        Assert.assertEquals(2, types.length);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[0]);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[1]);
    }

    @Test
    public void testMultipleOfAllTypeParams() {
        Integer[] types = MethodWrapperVisitor.getMethodParameterLoadInstructions("(DLjava/lang/Object;[[B[Ltest$E;FI[I[[[Ljava/lang/String;)V");
        Assert.assertEquals(8, types.length);
        Assert.assertEquals(Opcodes.DLOAD, (int) types[0]);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[1]);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[2]);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[3]);
        Assert.assertEquals(Opcodes.FLOAD, (int) types[4]);
        Assert.assertEquals(Opcodes.ILOAD, (int) types[5]);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[6]);
        Assert.assertEquals(Opcodes.ALOAD, (int) types[7]);
    }

    @Test
    public void testVoidReturnType() {
        Assert.assertEquals(Opcodes.RETURN, MethodWrapperVisitor.getMethodReturnTypeInstruction("()V"));
        Assert.assertEquals(Opcodes.RETURN, MethodWrapperVisitor.getMethodReturnTypeInstruction("([ILjava/lang/String;BBZ)V"));
    }

    @Test
    public void testPrimitiveReturnType() {
        Assert.assertEquals(Opcodes.IRETURN, MethodWrapperVisitor.getMethodReturnTypeInstruction("()I"));
        Assert.assertEquals(Opcodes.IRETURN, MethodWrapperVisitor.getMethodReturnTypeInstruction("(JJ)C"));
        Assert.assertEquals(Opcodes.IRETURN, MethodWrapperVisitor.getMethodReturnTypeInstruction("([I[[Z)S"));
        Assert.assertEquals(Opcodes.IRETURN, MethodWrapperVisitor.getMethodReturnTypeInstruction("(Ljava/lang/Object;)B"));
        Assert.assertEquals(Opcodes.IRETURN, MethodWrapperVisitor.getMethodReturnTypeInstruction("()Z"));
        Assert.assertEquals(Opcodes.LRETURN, MethodWrapperVisitor.getMethodReturnTypeInstruction("([ILjava/lang/String;BBZ)J"));
        Assert.assertEquals(Opcodes.DRETURN, MethodWrapperVisitor.getMethodReturnTypeInstruction("(BBZ)D"));
        Assert.assertEquals(Opcodes.FRETURN, MethodWrapperVisitor.getMethodReturnTypeInstruction("()F"));
    }

    @Test
    public void testReferenceReturnType() {
        Assert.assertEquals(Opcodes.ARETURN, MethodWrapperVisitor.getMethodReturnTypeInstruction("()[J"));
        Assert.assertEquals(Opcodes.ARETURN, MethodWrapperVisitor.getMethodReturnTypeInstruction("(I)[[[Z"));
        Assert.assertEquals(Opcodes.ARETURN, MethodWrapperVisitor.getMethodReturnTypeInstruction("(Ljava/lang/String;J)Ljava/lang/String;"));
    }

    @Test
    public void testComputeMaxs() {
        // Test out when we are using invokestatic (and do not need to load the 'this' reference)
        Assert.assertArrayEquals(new int[]{ 0, 0 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(true, new Integer[0], Opcodes.RETURN));
        Assert.assertArrayEquals(new int[]{ 1, 0 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(true, new Integer[0], Opcodes.IRETURN));
        Assert.assertArrayEquals(new int[]{ 1, 0 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(true, new Integer[0], Opcodes.ARETURN));
        Assert.assertArrayEquals(new int[]{ 1, 0 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(true, new Integer[0], Opcodes.FRETURN));
        Assert.assertArrayEquals(new int[]{ 2, 0 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(true, new Integer[0], Opcodes.LRETURN));
        Assert.assertArrayEquals(new int[]{ 2, 0 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(true, new Integer[0], Opcodes.DRETURN));
        Assert.assertArrayEquals(new int[]{ 1, 1 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(true, new Integer[]{ Opcodes.ILOAD }, Opcodes.RETURN));
        Assert.assertArrayEquals(new int[]{ 3, 3 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(true, new Integer[]{ Opcodes.ILOAD, Opcodes.FLOAD, Opcodes.ALOAD }, Opcodes.RETURN));
        Assert.assertArrayEquals(new int[]{ 5, 5 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(true, new Integer[]{ Opcodes.DLOAD, Opcodes.LLOAD, Opcodes.ALOAD }, Opcodes.RETURN));
        Assert.assertArrayEquals(new int[]{ 3, 3 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(true, new Integer[]{ Opcodes.DLOAD, Opcodes.ILOAD }, Opcodes.IRETURN));
        Assert.assertArrayEquals(new int[]{ 3, 3 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(true, new Integer[]{ Opcodes.LLOAD, Opcodes.ALOAD }, Opcodes.ARETURN));
        Assert.assertArrayEquals(new int[]{ 3, 3 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(true, new Integer[]{ Opcodes.DLOAD, Opcodes.FLOAD }, Opcodes.FRETURN));
        Assert.assertArrayEquals(new int[]{ 2, 1 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(true, new Integer[]{ Opcodes.ALOAD }, Opcodes.LRETURN));
        Assert.assertArrayEquals(new int[]{ 2, 2 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(true, new Integer[]{ Opcodes.DLOAD }, Opcodes.LRETURN));
        Assert.assertArrayEquals(new int[]{ 2, 1 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(true, new Integer[]{ Opcodes.ILOAD }, Opcodes.DRETURN));
        Assert.assertArrayEquals(new int[]{ 7, 7 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(true, new Integer[]{ Opcodes.DLOAD, Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.FLOAD, Opcodes.LLOAD }, Opcodes.DRETURN));

        // Test out when not using invokestatic (and need to first load the 'this' reference)
        Assert.assertArrayEquals(new int[]{ 1, 1 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(false, new Integer[0], Opcodes.RETURN));
        Assert.assertArrayEquals(new int[]{ 1, 1 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(false, new Integer[0], Opcodes.IRETURN));
        Assert.assertArrayEquals(new int[]{ 1, 1 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(false, new Integer[0], Opcodes.ARETURN));
        Assert.assertArrayEquals(new int[]{ 1, 1 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(false, new Integer[0], Opcodes.FRETURN));
        Assert.assertArrayEquals(new int[]{ 2, 1 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(false, new Integer[0], Opcodes.LRETURN));
        Assert.assertArrayEquals(new int[]{ 2, 1 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(false, new Integer[0], Opcodes.DRETURN));
        Assert.assertArrayEquals(new int[]{ 2, 2 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(false, new Integer[]{ Opcodes.ILOAD }, Opcodes.RETURN));
        Assert.assertArrayEquals(new int[]{ 4, 4 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(false, new Integer[]{ Opcodes.ILOAD, Opcodes.FLOAD, Opcodes.ALOAD }, Opcodes.RETURN));
        Assert.assertArrayEquals(new int[]{ 6, 6 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(false, new Integer[]{ Opcodes.DLOAD, Opcodes.LLOAD, Opcodes.ALOAD }, Opcodes.RETURN));
        Assert.assertArrayEquals(new int[]{ 4, 4 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(false, new Integer[]{ Opcodes.DLOAD, Opcodes.ILOAD }, Opcodes.IRETURN));
        Assert.assertArrayEquals(new int[]{ 4, 4 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(false, new Integer[]{ Opcodes.LLOAD, Opcodes.ALOAD }, Opcodes.ARETURN));
        Assert.assertArrayEquals(new int[]{ 4, 4 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(false, new Integer[]{ Opcodes.DLOAD, Opcodes.FLOAD }, Opcodes.FRETURN));
        Assert.assertArrayEquals(new int[]{ 2, 2 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(false, new Integer[]{ Opcodes.ALOAD }, Opcodes.LRETURN));
        Assert.assertArrayEquals(new int[]{ 3, 3 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(false, new Integer[]{ Opcodes.DLOAD }, Opcodes.LRETURN));
        Assert.assertArrayEquals(new int[]{ 2, 2 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(false, new Integer[]{ Opcodes.ILOAD }, Opcodes.DRETURN));
        Assert.assertArrayEquals(new int[]{ 8, 8 }, MethodWrapperVisitor.computeMaxStackAndMaxLocals(false, new Integer[]{ Opcodes.DLOAD, Opcodes.ALOAD, Opcodes.ALOAD, Opcodes.FLOAD, Opcodes.LLOAD }, Opcodes.DRETURN));
    }

    @Test
    @Ignore
    public void testConstructorDescriptors() {
        Assert.assertEquals("(V)V", MethodWrapperVisitor.deriveNewWrappedConstructorDescriptor("()V"));
        Assert.assertEquals("(IV)V", MethodWrapperVisitor.deriveNewWrappedConstructorDescriptor("(I)V"));
        Assert.assertEquals("(JZBCSIFDV)V", MethodWrapperVisitor.deriveNewWrappedConstructorDescriptor("(JZBCSIFD)V"));
        Assert.assertEquals("([I[[Ljava.lang.String;V)V", MethodWrapperVisitor.deriveNewWrappedConstructorDescriptor("([I[[Ljava.lang.String;)V"));
        Assert.assertEquals("([Ljava.lang.String;Ljava.lang.Object;V)V", MethodWrapperVisitor.deriveNewWrappedConstructorDescriptor("([Ljava.lang.String;Ljava.lang.Object;)V"));
        Assert.assertEquals("([Ljava.lang.String;Ljava.lang.Object;ZV)V", MethodWrapperVisitor.deriveNewWrappedConstructorDescriptor("([Ljava.lang.String;Ljava.lang.Object;Z)V"));
    }
}
