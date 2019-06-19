package org.aion.avm.core.rejection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.aion.avm.core.AvmTransactionUtil;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.AvmImplTestResource;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingKernel;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;


/**
 * Implemented as part of issue-305 to demonstrate how rejections are actually observed, within the transformation logic.
 */
public class RejectionIntegrationTest {
    private static AionAddress FROM = TestingKernel.PREMINED_ADDRESS;
    private static long ENERGY_LIMIT = 5_000_000L;
    private static long ENERGY_PRICE = 1L;

    private static TestingKernel kernel;
    private static AvmImpl avm;

    @BeforeClass
    public static void setup() {
        TestingBlock BLOCK = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingKernel(BLOCK);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void rejectNonShadowJclSubclassError() throws Exception {
        kernel.generateBlock();
        byte[] jar = JarBuilder.buildJarForMainAndClasses(RejectNonShadowJclSubclassError.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        Transaction transaction = AvmTransactionUtil.create(FROM, kernel.getNonce(FROM), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        AvmTransactionResult createResult = avm.run(RejectionIntegrationTest.kernel, new Transaction[] {transaction})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_REJECTED, createResult.getResultCode());
    }

    @Test
    public void rejectCorruptMainMethod() throws IOException {
        kernel.generateBlock();
        byte[] classBytes = Files.readAllBytes(Paths.get("test/resources/TestClassTemplate_corruptMainMethod.class"));
        byte[] jar = JarBuilder.buildJarForExplicitClassNameAndBytecode("TestClassTemplate", classBytes);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        Transaction transaction = AvmTransactionUtil.create(FROM, kernel.getNonce(FROM), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        AvmTransactionResult createResult = avm.run(RejectionIntegrationTest.kernel, new Transaction[] {transaction})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_REJECTED, createResult.getResultCode());
    }

    @Test
    public void rejectCorruptMethod() throws IOException {
        kernel.generateBlock();
        byte[] classBytes = Files.readAllBytes(Paths.get("test/resources/TestClassTemplate_corruptMethod.class"));
        Map<String, byte[]> classMap = new HashMap<>();
        classMap.put("TestClassTemplate_corruptMethod", classBytes);
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(
            AvmImplTestResource.class, classMap);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        Transaction transaction = AvmTransactionUtil.create(FROM, kernel.getNonce(FROM), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        AvmTransactionResult createResult = avm.run(RejectionIntegrationTest.kernel, new Transaction[] {transaction})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_REJECTED, createResult.getResultCode());
    }
}
