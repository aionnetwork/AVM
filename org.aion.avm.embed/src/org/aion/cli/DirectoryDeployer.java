package org.aion.cli;

import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.AvmTransactionUtil;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.ExecutionType;
import org.aion.avm.core.FutureResult;
import org.aion.avm.core.IExternalCapabilities;
import org.aion.avm.core.IExternalState;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.embed.StandardCapabilities;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.kernel.*;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;


/**
 * A tool to run our data collection histogram against pre-created contracts.
 * Point this at a directory containing code and argument data and it will deploy every contract found, with corresponding argument data.
 * The expected shape of the directory given is produced by the AvmConfiguration.contractCaptureDirectory option.
 * 
 * NOTE:  This currently deploys 1 contract at a time, in the order they are found in the directory, using the same emulated block.
 * In the future, this may be changed to deploy them in the order and blocks defined by the other meta-data.
 */
public class DirectoryDeployer {
    private static AionAddress DEPLOYER = Helpers.randomAddress();
    private static TestingBlock BLOCK = new TestingBlock(new byte[32], 1, DEPLOYER, System.currentTimeMillis(), new byte[0]);
    private static long ENERGY_LIMIT = 5_000_000L;
    private static long ENERGY_PRICE = 1L;

    public static void main(String[] args) {
        TestingState kernel = new TestingState(BLOCK);
        kernel.adjustBalance(DEPLOYER, new BigInteger("10000000000000000000000"));
        IExternalCapabilities capabilities = new StandardCapabilities();
        AvmConfiguration config = new AvmConfiguration();
        config.deploymentDataHistorgramOutput = System.out;
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(capabilities, config);
        
        File rootDirectory = new File(args[0]);
        assertTrue(rootDirectory.isDirectory());
        int passCount = 0;
        int failCount = 0;
        for (String name : rootDirectory.list()) {
            try {
                Transaction transaction = createTransaction(kernel, new File(rootDirectory, name));
                FutureResult[] futures = avm.run(kernel, new Transaction[] { transaction }, ExecutionType.ASSUME_MAINCHAIN, 0);
                boolean success = futures[0].getResult().transactionStatus.isSuccess();
                if (success) {
                    passCount += 1;
                } else {
                    failCount += 1;
                }
                System.out.println(name + ": " + (success ? "PASS" : "FAIL"));
            } catch (IOException e) {
                System.err.println(name);
                e.printStackTrace();
                System.exit(1);
            }
        }
        System.out.println("Attempted " + (passCount + failCount) + " deployments (" + passCount + " passed, " + failCount + " failed)");
        avm.shutdown();
    }


    private static Transaction createTransaction(IExternalState kernel, File directory) throws IOException {
        // For now, we will only load the code and the arguments.
        byte[] code = Files.readAllBytes(new File(directory, "code.jar").toPath());
        File argFile = new File(directory, "arguments.bin");
        byte[] args = argFile.exists()
                ? Files.readAllBytes(argFile.toPath())
                : new byte[0];
        byte[] createData = new CodeAndArguments(code, args).encodeToBytes();
        return AvmTransactionUtil.create(DEPLOYER, kernel.getNonce(DEPLOYER), BigInteger.ZERO, createData, ENERGY_LIMIT, ENERGY_PRICE);
    }

    private static void assertTrue(boolean flag) {
        // We use a private helper to manage the assertions since the JDK default disables them.
        if (!flag) {
            throw new AssertionError("Case must be true");
        }
    }
}
