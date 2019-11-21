package org.aion.avm.core.miscvisitors.interfaceVisitor;

import org.aion.avm.core.*;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.miscvisitors.interfaceVisitor.interfaces.*;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIException;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.avm.utilities.JarBuilder;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.types.TransactionResult;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.aion.avm.core.miscvisitors.interfaceVisitor.InterfaceFieldClassGeneratorTest.*;
import static org.junit.Assert.assertTrue;

public class InterfaceFieldIntegrationTest {
    private static AionAddress deployer = TestingState.PREMINED_ADDRESS;
    private static AvmImpl avm;
    private static TestingState kernel;

    @BeforeClass
    public static void setupClass() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingState(block);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void FIELDSClassDefinedFail() throws IOException {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(FieldsClassDefinedInterfaceFail.class, Collections.emptyMap(), FIELDSInterfaceFail.class, ABIEncoder.class, ABIDecoder.class, ABIException.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        // objectGraph value is retrieved from the old version of the AVM (with InterfaceFieldMappingVisitor) after deployment
        byte[] objectGraph = Files.readAllBytes(Paths.get("test/resources/FieldsClassDefinedInterfaceFailObjectGraph"));

        kernel.putObjectGraph(dappAddress, objectGraph);

        //re-transform the code
        kernel.setTransformedCode(dappAddress, null);
        kernel.generateBlock();

        // all calls to contract will fail
        TransactionResult result = callDapp(kernel, deployer, dappAddress, "getInterfaceString",
                ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isFailed());

        kernel.generateBlock();
        result = callDapp(kernel, deployer, dappAddress, "getInnerClassString",
                ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isFailed());
    }

    @Test
    public void FIELDSNotDefinedInInterfaceSuccess() throws IOException {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(FieldsNotDefinedSuccess.class, Collections.emptyMap(), NoFIELDSInterface.class, SampleObj.class, ABIEncoder.class, ABIException.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        byte[] objectGraph = Files.readAllBytes(Paths.get("test/resources/FieldsNotDefinedSuccessObjectGraph"));

        kernel.putObjectGraph(dappAddress, objectGraph);
        kernel.generateBlock();

        //re-transform the code
        kernel.setTransformedCode(dappAddress, null);

        TransactionResult result = callDapp(kernel, deployer, dappAddress, "",
                ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(31, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    @Test
    public void FIELDSInInnerInterface() throws IOException {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(ClassWithNestedInterfaces.class, Collections.emptyMap(), LevelOneInterface.class, ABIEncoder.class, ABIException.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        byte[] objectGraph = Files.readAllBytes(Paths.get("test/resources/ClassWithNestedInterfacesObjectGraph"));
        kernel.generateBlock();

        kernel.putObjectGraph(dappAddress, objectGraph);

        //re-transform the code
        kernel.setTransformedCode(dappAddress, null);

        TransactionResult result = callDapp(kernel, deployer, dappAddress, "",
                ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isFailed());
    }

    @Test
    public void FIELDSDefinedInInterfaceSuccess() {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(AcceptFieldsNewAVM.class, Collections.emptyMap(), FIELDSInterfaceSuccess.class, ABIEncoder.class, ABIDecoder.class, ABIException.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        TransactionResult result = callDapp(kernel, deployer, dappAddress, "",
                ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(115, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    @Test
    public void FIELDSAsInnerInterfaceName() throws IOException {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(ClassWithFIELDSAsInterfaceName.class, Collections.emptyMap(), InnerFIELDSInterface.class, ABIEncoder.class, ABIException.class, InnerFIELDSImplementation.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        byte[] objectGraph = Files.readAllBytes(Paths.get("test/resources/ClassWithFIELDSAsInterfaceNameObjectGraph"));
        kernel.putObjectGraph(dappAddress, objectGraph);
        //re-transform the code
        kernel.setTransformedCode(dappAddress, null);

        TransactionResult result = callDapp(kernel, deployer, dappAddress, "",
                ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        // number of processed classes will be different
        Assert.assertTrue(result.transactionStatus.isFailed());
    }

    @Test
    public void FIELDSAsInterfaceName() throws IOException {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(ClassWithFieldsInterface.class, Collections.emptyMap(), ABIEncoder.class, ABIException.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        byte[] objectGraph = Files.readAllBytes(Paths.get("test/resources/ClassWithFieldsInterfaceObjectGraph"));
        kernel.putObjectGraph(dappAddress, objectGraph);
        //re-transform the code
        kernel.setTransformedCode(dappAddress, null);

        TransactionResult result = callDapp(kernel, deployer, dappAddress, "",
                ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        // number of processed classes will be different
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(14, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    @Test
    public void FIELDSAsInterfaceNameAVM2() {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(ClassWithFieldsInterface.class, Collections.emptyMap(), ABIEncoder.class, ABIException.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        TransactionResult result = callDapp(kernel, deployer, dappAddress, "",
                ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        // number of processed classes will be different
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(14, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    @Test
    public void FIELDSAsInnerInterfaceNameAVM2() {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(ClassWithFIELDSAsInterfaceName.class, Collections.emptyMap(), InnerFIELDSInterface.class, ABIEncoder.class, ABIException.class, InnerFIELDSImplementation.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        TransactionResult result = callDapp(kernel, deployer, dappAddress, "",
                ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(309, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    @Test
    public void testInnerInterfaceAllFIELDS() {
        Map<String, byte[]> classes = new HashMap<>();

        classes.put("NestedInterfaces", getInnerFILEDSInterfaceBytes());
        classes.put("NestedInterfaces$FIELDS", getNestedInterfaceCalledFIELDSLevelOne());
        classes.put("NestedInterfaces$FIELDS$FIELDS", getNestedInterfaceCalledFIELDSLevelTwo());

        byte[] jar = JarBuilder.buildJarForExplicitClassNamesAndBytecode("NestedMain", getFIELDMainClassBytes(), classes);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        TransactionResult result = callDapp(kernel, deployer, dappAddress, "",
                ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertArrayEquals(new byte[405], result.copyOfTransactionOutput().orElseThrow());
    }

    @Test
    public void testInterfaceWithNoDeclaredFields() throws IOException {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(ImplementationNoFields.class, Collections.emptyMap(), InterfaceNoFields.class, ABIEncoder.class, ABIException.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        // objectGraph value below is retrieved from the old version of the AVM (with InterfaceFieldMappingVisitor) after deployment
        byte[] objectGraph = Files.readAllBytes(Paths.get("test/resources/ImplementationNoFieldsObjectGraph"));
        kernel.putObjectGraph(dappAddress, objectGraph);

        //re-transform the code
        kernel.setTransformedCode(dappAddress, null);

        TransactionResult result = callDapp(kernel, deployer, dappAddress, "",
                ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(22022, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    @Test
    public void testInterfaceWithNoDeclaredFieldsAVM2() {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(ImplementationNoFields.class, Collections.emptyMap(), InterfaceNoFields.class, ABIEncoder.class, ABIException.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        TransactionResult result = callDapp(kernel, deployer, dappAddress, "",
                ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(22022, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    private static TransactionResult callDapp(TestingState kernel, AionAddress sender, AionAddress dappAddress,
                                              String methodName, ExecutionType executionType, long commonMainchainBlockNumber, Object... args) {
        ABIStreamingEncoder encoder = new ABIStreamingEncoder().encodeOneString(methodName);
        for (Object arg : args) {
            encoder.encodeOneByteArray((byte[]) arg);
        }
        byte[] data = encoder.toBytes();
        Transaction tx = AvmTransactionUtil.call(sender, dappAddress, kernel.getNonce(sender), BigInteger.ZERO, data, 2_000_00, 1);
        return avm.run(kernel, new Transaction[]{tx}, executionType, commonMainchainBlockNumber)[0].getResult();
    }

    private static AionAddress deploy(AionAddress deployer, TestingState kernel, byte[] txData) {
        Transaction tx1 = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, 5_000_000, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{tx1}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
        return new AionAddress(result.copyOfTransactionOutput().orElseThrow());
    }
}

