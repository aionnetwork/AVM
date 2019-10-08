package org.aion.avm.core.transformation;

import avm.Address;
import org.aion.avm.core.*;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.rejection.RejectClassNameWhichIsWaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaayTooLong;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIException;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.types.TransactionResult;
import org.junit.*;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

public class LazyCodeTransformationTest {

    private static AionAddress from = TestingState.PREMINED_ADDRESS;
    private static AvmImpl avm;
    private static AionAddress dappAddress;
    private static TestingState kernel;
    private static String initialNameValue = "1stName";

    @Before
    public void setupClass() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingState(block);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        byte[] jar = getCode();

        CodeAndArguments codeAndArguments = new CodeAndArguments(jar, ABIEncoder.encodeOneString(initialNameValue));
        byte[] txData = codeAndArguments.encodeToBytes();
        dappAddress = deploy(from, kernel, txData);
    }

    @After
    public void tearDown() {
        avm.shutdown();
    }

    @Test
    public void validateClassIsNotInitialized() {
        String newName = "2ndName";
        ABIStreamingEncoder encoder = new ABIStreamingEncoder().encodeOneString("setName").encodeOneString(newName);
        TransactionResult result = callDapp(kernel, from, dappAddress, encoder.toBytes(), kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());

        encoder = new ABIStreamingEncoder().encodeOneString("getName");
        result = callDapp(kernel, from, dappAddress, encoder.toBytes(), kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(newName, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneString());

        encoder = new ABIStreamingEncoder().encodeOneString("getChangeCount");
        result = callDapp(kernel, from, dappAddress, encoder.toBytes(), kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(1, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        // set TransformedCode to null so that the stored DApp code is re-transformed
        kernel.setTransformedCode(dappAddress, null);

        // Assert that field values are as expected and have not changed
        encoder = new ABIStreamingEncoder().encodeOneString("getName");
        result = callDapp(kernel, from, dappAddress, encoder.toBytes(), kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(newName, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneString());

        encoder = new ABIStreamingEncoder().encodeOneString("getChangeCount");
        result = callDapp(kernel, from, dappAddress, encoder.toBytes(), kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(1, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    @Test
    public void validateTransformedCodeIsUsedIfAvailable() {
        //if transformed code is available, the contract has been called and re-transformed. The code should not be retrieved
        kernel.putCode(dappAddress, new byte[0]);

        ABIStreamingEncoder encoder = new ABIStreamingEncoder().encodeOneString("getName");
        TransactionResult result = callDapp(kernel, from, dappAddress, encoder.toBytes(), kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(initialNameValue, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneString());
    }

    @Test
    public void validateTransformedCodeIsStoredCorrectly() {
        //if transformed code is not available, the contract code will be re-transformed
        byte[] transformedCode = kernel.getTransformedCode(dappAddress);
        kernel.setTransformedCode(dappAddress, null);

        ABIStreamingEncoder encoder = new ABIStreamingEncoder().encodeOneString("getName");
        TransactionResult result = callDapp(kernel, from, dappAddress, encoder.toBytes(), kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(initialNameValue, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneString());
        Assert.assertArrayEquals(transformedCode, kernel.getTransformedCode(dappAddress));
    }

    @Test
    public void selfDestructTest() {
        kernel.generateBlock();

        ABIStreamingEncoder encoder = new ABIStreamingEncoder().encodeOneString("selfDestruct");
        TransactionResult result = callDapp(kernel, from, dappAddress, encoder.toBytes(), kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());

        kernel.generateBlock();
        Assert.assertNull(kernel.getCode(dappAddress));
        Assert.assertNull(kernel.getTransformedCode(dappAddress));

        encoder = new ABIStreamingEncoder().encodeOneString("getName");
        result = callDapp(kernel, from, dappAddress, encoder.toBytes(), kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertFalse(result.copyOfTransactionOutput().isPresent());
    }

    /**
     * Cache Interaction
     */
    @Test
    public void validateTransformedCodeIsStoredInCache() {
        //if transformed code is not available, the contract code will be re-transformed
        kernel.setTransformedCode(dappAddress, null);

        ABIStreamingEncoder encoder = new ABIStreamingEncoder().encodeOneString("getName");
        TransactionResult result = callDapp(kernel, from, dappAddress, encoder.toBytes(), kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(initialNameValue, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneString());

        // transformed code should be in cache now
        kernel.generateBlock();
        kernel.setTransformedCode(dappAddress, new byte[0]);

        encoder = new ABIStreamingEncoder().encodeOneString("getName");
        result = callDapp(kernel, from, dappAddress, encoder.toBytes(), kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(initialNameValue, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneString());
    }

    @Test
    public void validateCacheIsNotUsedIfTransformedCodeIsNull() {
        kernel.generateBlock();

        // add to cache
        ABIStreamingEncoder encoder = new ABIStreamingEncoder().encodeOneString("getName");
        TransactionResult result = callDapp(kernel, from, dappAddress, encoder.toBytes(), kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(initialNameValue, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneString());

        kernel.generateBlock();
        kernel.setTransformedCode(dappAddress, null);
        // make sure code is not read from the cache
        kernel.putCode(dappAddress, new byte[]{0});

        encoder = new ABIStreamingEncoder().encodeOneString("getName");
        result = callDapp(kernel, from, dappAddress, encoder.toBytes(), kernel.getBlockNumber() - 1);
        Assert.assertFalse(result.transactionStatus.isSuccess());
    }

    /**
     * Error cases
     */

    @Test
    public void validateRetransformingInvalidCodeFails() {
        // In order to mock rejection, setup the kernel database as if this class was acceptable before and was deployed successfully
        byte[] bytes = JarBuilder.buildJarForMainAndClasses(RejectClassNameWhichIsWaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaayTooLong.class);
        kernel.putCode(dappAddress, bytes);
        kernel.setTransformedCode(dappAddress, null);
        kernel.putObjectGraph(dappAddress, new byte[]{0, 0, 0, 1});

        ABIStreamingEncoder encoder = new ABIStreamingEncoder().encodeOneString("getName");
        TransactionResult result = callDapp(kernel, from, dappAddress, encoder.toBytes(), kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isFailed());

        kernel.generateBlock();

        result = callDapp(kernel, from, dappAddress, encoder.toBytes(), kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isFailed());
    }

    @Test
    public void validateRetransformingInvalidCodeInternalTxFails() {
        // In order to mock rejection, setup the kernel database as if this class was acceptable before and was deployed successfully
        byte[] bytes = JarBuilder.buildJarForMainAndClasses(RejectClassNameWhichIsWaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaayTooLong.class);
        AionAddress callee = new AionAddress(Helpers.hexStringToBytes("a025f4fd54064e869f158c1b4eb0ed34820f67e60ee80a53b469f725efc06371"));
        kernel.putCode(callee, bytes);
        kernel.setTransformedCode(callee, null);
        kernel.putObjectGraph(callee, new byte[]{0, 0, 0, 1});
        kernel.generateBlock();

        ABIStreamingEncoder encoder = new ABIStreamingEncoder().encodeOneString("call").encodeOneAddress(new Address(callee.toByteArray()));
        TransactionResult result = callDapp(kernel, from, dappAddress, encoder.toBytes(), kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());
    }

    private static AionAddress deploy(AionAddress deployer, TestingState kernel, byte[] txData) {
        Transaction tx1 = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, 5_000_000, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{tx1}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
        return new AionAddress(result.copyOfTransactionOutput().orElseThrow());
    }

    private static TransactionResult callDapp(TestingState kernel, AionAddress sender, AionAddress dappAddress, byte[] data, long commonMainchainBlockNumber) {

        Transaction tx = AvmTransactionUtil.call(sender, dappAddress, kernel.getNonce(sender), BigInteger.ZERO, data, 2_000_000, 1);
        return avm.run(kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, commonMainchainBlockNumber)[0].getResult();
    }

    private static byte[] getCode() {
        return JarBuilder.buildJarForMainAndClasses(SampleContract.class, ABIDecoder.class, ABIEncoder.class, ABIException.class);
    }
}
