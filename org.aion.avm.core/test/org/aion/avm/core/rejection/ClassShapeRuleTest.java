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
        TransactionResult result = deployOnAvm(className, classBytes);
        Assert.assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testLongNoop() throws Exception {
        String className = "ClassName";
        byte[] classBytes = createNoopClass(className, ConsensusLimitConstants.MAX_METHOD_BYTE_LENGTH + 1);
        TransactionResult result = deployOnAvm(className, classBytes);
        Assert.assertTrue(result.transactionStatus.isFailed());
    }

    @Test
    public void testLimitCatch() throws Exception {
        String className = "ClassName";
        byte[] classBytes = createLongThrowClass(className, ConsensusLimitConstants.MAX_EXCEPTION_TABLE_ENTRIES);
        TransactionResult result = deployOnAvm(className, classBytes);
        Assert.assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testDeepCatch() throws Exception {
        String className = "ClassName";
        byte[] classBytes = createLongThrowClass(className, ConsensusLimitConstants.MAX_EXCEPTION_TABLE_ENTRIES + 1);
        TransactionResult result = deployOnAvm(className, classBytes);
        Assert.assertTrue(result.transactionStatus.isFailed());
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

    private TransactionResult deployOnAvm(String className, byte[] classBytes) {
        byte[] jar = JarBuilder.buildJarForExplicitClassNamesAndBytecode(className, classBytes, Collections.emptyMap());
        byte[] deployment = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(BLOCK);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        long energyLimit = 5_000_000l;
        long energyPrice = 1l;
        Transaction tx = AvmTransactionUtil.create(DEPLOYER, kernel.getNonce(DEPLOYER), BigInteger.ZERO, deployment, energyLimit, energyPrice);
        TransactionResult result = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, 0)[0].getResult();
        avm.shutdown();
        return result;
    }
}
