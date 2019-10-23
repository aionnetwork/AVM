package org.aion.avm.core.rejection;

import java.math.BigInteger;
import java.util.Collections;

import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.AvmTransactionUtil;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.ExecutionType;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.utilities.JarBuilder;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.types.TransactionResult;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


public class ClassShapeRuleTest {
    private static AionAddress DEPLOYER = TestingState.PREMINED_ADDRESS;
    private static TestingBlock BLOCK = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    @Test
    public void testLimitNoop() throws Exception {
        String className = "ClassName";
        byte[] classBytes = createNoopClass(className, ConsensusLimitConstants.MAX_METHOD_BYTE_LENGTH);
        boolean didDeploy = deployOnAvm(className, classBytes);
        Assert.assertTrue(didDeploy);
    }

    @Test
    public void testLongNoop() throws Exception {
        String className = "ClassName";
        byte[] classBytes = createNoopClass(className, ConsensusLimitConstants.MAX_METHOD_BYTE_LENGTH + 1);
        boolean didDeploy = deployOnAvm(className, classBytes);
        Assert.assertFalse(didDeploy);
    }

    @Test
    public void testLimitCatch() throws Exception {
        String className = "ClassName";
        byte[] classBytes = createLongThrowClass(className, ConsensusLimitConstants.MAX_EXCEPTION_TABLE_ENTRIES);
        boolean didDeploy = deployOnAvm(className, classBytes);
        Assert.assertTrue(didDeploy);
    }

    @Test
    public void testDeepCatch() throws Exception {
        String className = "ClassName";
        byte[] classBytes = createLongThrowClass(className, ConsensusLimitConstants.MAX_EXCEPTION_TABLE_ENTRIES + 1);
        boolean didDeploy = deployOnAvm(className, classBytes);
        Assert.assertFalse(didDeploy);
    }

    @Test
    public void testLimitIntPush() throws Exception {
        String className = "ClassName";
        byte[] classBytes = createDeepPushClass(className, ConsensusLimitConstants.MAX_OPERAND_STACK_DEPTH, false);
        boolean didDeploy = deployOnAvm(className, classBytes);
        Assert.assertTrue(didDeploy);
    }

    @Test
    public void testDeepIntPush() throws Exception {
        String className = "ClassName";
        byte[] classBytes = createDeepPushClass(className, ConsensusLimitConstants.MAX_OPERAND_STACK_DEPTH + 1, false);
        boolean didDeploy = deployOnAvm(className, classBytes);
        Assert.assertFalse(didDeploy);
    }

    @Test
    public void testLimitLongPush() throws Exception {
        String className = "ClassName";
        byte[] classBytes = createDeepPushClass(className, ceilingDivideByTwo(ConsensusLimitConstants.MAX_OPERAND_STACK_DEPTH), true);
        boolean didDeploy = deployOnAvm(className, classBytes);
        Assert.assertTrue(didDeploy);
    }

    @Test
    public void testDeepLongPush() throws Exception {
        String className = "ClassName";
        byte[] classBytes = createDeepPushClass(className, ceilingDivideByTwo(ConsensusLimitConstants.MAX_OPERAND_STACK_DEPTH) + 1, true);
        boolean didDeploy = deployOnAvm(className, classBytes);
        Assert.assertFalse(didDeploy);
    }

    @Test
    public void testLimitIntVars() throws Exception {
        String className = "ClassName";
        byte[] classBytes = createVarHeavyClass(className, ConsensusLimitConstants.MAX_LOCAL_VARIABLES, false);
        boolean didDeploy = deployOnAvm(className, classBytes);
        Assert.assertTrue(didDeploy);
    }

    @Test
    public void testFailIntVars() throws Exception {
        String className = "ClassName";
        byte[] classBytes = createVarHeavyClass(className, ConsensusLimitConstants.MAX_LOCAL_VARIABLES + 1, false);
        boolean didDeploy = deployOnAvm(className, classBytes);
        Assert.assertFalse(didDeploy);
    }

    @Test
    public void testLimitLongVars() throws Exception {
        String className = "ClassName";
        byte[] classBytes = createVarHeavyClass(className, ConsensusLimitConstants.MAX_LOCAL_VARIABLES - 1, true);
        boolean didDeploy = deployOnAvm(className, classBytes);
        Assert.assertTrue(didDeploy);
    }

    @Test
    public void testFailLongVars() throws Exception {
        String className = "ClassName";
        byte[] classBytes = createVarHeavyClass(className, ConsensusLimitConstants.MAX_LOCAL_VARIABLES, true);
        boolean didDeploy = deployOnAvm(className, classBytes);
        Assert.assertFalse(didDeploy);
    }

    @Test
    public void testLimitMethodCount() throws Exception {
        String className = "ClassName";
        byte[] classBytes = createMethodHeavyClass(className, ConsensusLimitConstants.MAX_METHOD_COUNT);
        boolean didDeploy = deployOnAvm(className, classBytes);
        Assert.assertTrue(didDeploy);
    }

    @Test
    public void testFailMethodCount() throws Exception {
        String className = "ClassName";
        byte[] classBytes = createMethodHeavyClass(className, ConsensusLimitConstants.MAX_METHOD_COUNT + 1);
        boolean didDeploy = deployOnAvm(className, classBytes);
        Assert.assertFalse(didDeploy);
    }


    private static byte[] createNoopClass(String className, int instructionCount) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V10, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, className, null, "java/lang/Object", new String[0]);
        MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "()[B", null, null);
        for (int i = 0; i < (instructionCount - 2); ++i) {
            method.visitInsn(Opcodes.NOP);
        }
        method.visitInsn(Opcodes.ACONST_NULL);
        method.visitInsn(Opcodes.ARETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] createLongThrowClass(String className, int catchDepth) {
        // Note that the way this test writes the method, catchDepth MUST be at least 1.
        Assert.assertTrue(catchDepth >= 1);
        
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V10, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, className, null, "java/lang/Object", new String[0]);
        // To test on filesystem, use "([Ljava/lang/String;)V".
        MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "()[B", null, null);
        Label start = new Label();
        Label end = new Label();
        Label handler = new Label();
        method.visitTryCatchBlock(start, end, handler, "java/lang/NullPointerException");
        method.visitLabel(start);
        method.visitInsn(Opcodes.ACONST_NULL);
        method.visitTypeInsn(Opcodes.CHECKCAST, "[Ljava/lang/Object;");
        method.visitInsn(Opcodes.ARRAYLENGTH);
        method.visitInsn(Opcodes.ACONST_NULL);
        method.visitInsn(Opcodes.ARETURN);
        method.visitLabel(end);
        for (int i = 0; i < (catchDepth - 1); ++i) {
            method.visitLabel(handler);
            start = new Label();
            end = new Label();
            handler = new Label();
            method.visitTryCatchBlock(start, end, handler, "java/lang/NullPointerException");
            method.visitLabel(start);
            method.visitInsn(Opcodes.ATHROW);
            method.visitLabel(end);
        }
        method.visitLabel(handler);
        method.visitInsn(Opcodes.ACONST_NULL);
        method.visitInsn(Opcodes.ARETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] createDeepPushClass(String className, int pushCount, boolean isLong) {
        // Note that the setup for the method requires at least 2 stack depth.
        Assert.assertTrue(pushCount >= 2);
        
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V10, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, className, null, "java/lang/Object", new String[0]);
        MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "()[B", null, null);
        Object oneLong = Long.valueOf(1L);
        Object oneInt = Integer.valueOf(1);
        Object value = isLong
                ? oneLong
                : oneInt;
        // visitLdcInsn uses a type check so make sure that we got the type we were expecting (some compilers try to get clever here).
        if (isLong) {
            Assert.assertTrue(value instanceof Long);
        } else {
            Assert.assertTrue(value instanceof Integer);
        }
        method.visitLdcInsn(value);
        int storeOpcode = isLong ? Opcodes.LSTORE : Opcodes.ISTORE;
        method.visitVarInsn(storeOpcode, 0);
        int loadOpcode = isLong ? Opcodes.LLOAD : Opcodes.ILOAD;
        // Reduce the push count by 1 since we have at least one more slot for the return value.
        for (int i = 0; i < (pushCount - 1); ++i) {
            method.visitVarInsn(loadOpcode, 0);
        }
        method.visitInsn(Opcodes.ACONST_NULL);
        method.visitInsn(Opcodes.ARETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] createVarHeavyClass(String className, int varCount, boolean isLong) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V10, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, className, null, "java/lang/Object", new String[0]);
        MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "()[B", null, null);
        Object oneLong = Long.valueOf(1L);
        Object oneInt = Integer.valueOf(1);
        Object value = isLong
                ? oneLong
                : oneInt;
        int storeOpcode = isLong ? Opcodes.LSTORE : Opcodes.ISTORE;
        // visitLdcInsn uses a type check so make sure that we got the type we were expecting (some compilers try to get clever here).
        if (isLong) {
            Assert.assertTrue(value instanceof Long);
        } else {
            Assert.assertTrue(value instanceof Integer);
        }
        // Note that we will actually invalidate the local variable n-1 when writing a long, but this is just to verify that it reserves the next slot.
        for (int i = 0; i < varCount; ++i) {
            method.visitLdcInsn(value);
            method.visitVarInsn(storeOpcode, i);
        }
        method.visitInsn(Opcodes.ACONST_NULL);
        method.visitInsn(Opcodes.ARETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] createMethodHeavyClass(String className, int methodCount) {
        // Note that the class will have the main so we need at least one method.
        Assert.assertTrue(methodCount >= 1);
        
        String methodPrefix = "method_";
        String methodDescriptor = "()V";
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V10, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, className, null, "java/lang/Object", new String[0]);
        MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "()[B", null, null);
        for (int i = 0; i < (methodCount - 1); ++i) {
            String methodName = methodPrefix + i;
            method.visitMethodInsn(Opcodes.INVOKESTATIC, className, methodName, methodDescriptor, false);
        }
        method.visitInsn(Opcodes.ACONST_NULL);
        method.visitInsn(Opcodes.ARETURN);
        method.visitMaxs(0, 0);
        method.visitEnd();
        for (int i = 0; i < (methodCount - 1); ++i) {
            String methodName = methodPrefix + i;
            MethodVisitor targetMethod = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, methodName, methodDescriptor, null, null);
            targetMethod.visitInsn(Opcodes.RETURN);
            targetMethod.visitMaxs(0, 0);
            targetMethod.visitEnd();
        }
        writer.visitEnd();
        return writer.toByteArray();
    }

    private boolean deployOnAvm(String className, byte[] classBytes) {
        byte[] jar = JarBuilder.buildJarForExplicitClassNamesAndBytecode(className, classBytes, Collections.emptyMap());
        byte[] deployment = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(BLOCK);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        long energyLimit = 5_000_000l;
        long energyPrice = 1l;
        Transaction deploymentTransaction = AvmTransactionUtil.create(DEPLOYER, kernel.getNonce(DEPLOYER), BigInteger.ZERO, deployment, energyLimit, energyPrice);
        TransactionResult deploymentResult = avm.run(kernel, new Transaction[] {deploymentTransaction}, ExecutionType.ASSUME_MAINCHAIN, 0)[0].getResult();
        boolean didDeploy = deploymentResult.transactionStatus.isSuccess();
        
        // If the deployment was a success, we also want to send a basic call which should always succeed.
        if (didDeploy) {
            AionAddress contractAddress = new AionAddress(deploymentResult.copyOfTransactionOutput().orElseThrow());
            Transaction callTransaction = AvmTransactionUtil.call(DEPLOYER, contractAddress, kernel.getNonce(DEPLOYER), BigInteger.ZERO, new byte[0], energyLimit, energyPrice);
            TransactionResult result = avm.run(kernel, new Transaction[] {callTransaction}, ExecutionType.ASSUME_MAINCHAIN, 0)[0].getResult();
            Assert.assertTrue(result.transactionStatus.isSuccess());
        }
        
        // Now, shut down and return whether or not the deployment was a success.
        avm.shutdown();
        return didDeploy;
    }

    private int ceilingDivideByTwo(int value) {
        return (value + 1) / 2;
    }
}
