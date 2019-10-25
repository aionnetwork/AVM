package org.aion.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.types.TransactionResult;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.AvmTransactionUtil;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.ExecutionType;
import org.aion.avm.core.IExternalCapabilities;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.embed.StandardCapabilities;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.kernel.TestingBlock;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class DirectoryDeployerIntegrationTest {
    private static AionAddress DEPLOYER = Helpers.randomAddress();
    private static TestingBlock BLOCK = new TestingBlock(new byte[32], 1, DEPLOYER, System.currentTimeMillis(), new byte[0]);
    private static long ENERGY_LIMIT = 5_000_000L;
    private static long ENERGY_PRICE = 1L;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void compare() throws Exception {
        // The test we are interested in is is seeing if the histogram caused by deploying a basic contract is the same when in-process and out-of-process.
        ByteArrayOutputStream captureStream = new ByteArrayOutputStream();
        TestingState kernel = new TestingState(BLOCK);
        kernel.adjustBalance(DEPLOYER, new BigInteger("10000000000000000000000"));
        IExternalCapabilities capabilities = new StandardCapabilities();
        AvmConfiguration config = new AvmConfiguration();
        config.deploymentDataHistorgramOutput = new PrintStream(captureStream);
        config.contractCaptureDirectory = folder.newFolder();
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(capabilities, config);
        byte[] deployment = new CodeAndArguments(UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(SimpleStackDemo.class), null).encodeToBytes();
        Transaction transaction = AvmTransactionUtil.create(DEPLOYER, kernel.getNonce(DEPLOYER), BigInteger.ZERO, deployment, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult result = avm.run(kernel, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        AionAddress contractAddress = new AionAddress(result.copyOfTransactionOutput().orElseThrow());
        String hexContract = Helpers.bytesToHexString(contractAddress.toByteArray());
        avm.shutdown();
        
        // By this point, we should be able to capture the output.
        String totalHistorgram = new String(captureStream.toByteArray(), StandardCharsets.UTF_8);
        
        // Make sure that we see the contract in that directory.
        Assert.assertEquals(1, config.contractCaptureDirectory.listFiles((file) -> file.getName().equals(hexContract)).length);
        
        // Now, invoke the DirectoryDeployer on the captured directory and make sure its output has this histogram at the end.
        PrintStream originalStdOut = System.out;
        ByteArrayOutputStream fakeStdOutBuffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(fakeStdOutBuffer));
        DirectoryDeployer.main(new String[] { config.contractCaptureDirectory.getAbsolutePath() });
        System.setOut(originalStdOut);
        
        String fromCli = new String(fakeStdOutBuffer.toByteArray(), StandardCharsets.UTF_8);
        Assert.assertTrue(fromCli.endsWith(totalHistorgram));
    }
}
