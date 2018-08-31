package org.aion.avm.core;

import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.testWallet.*;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.IHelper;
import org.aion.avm.shadow.java.lang.Class;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.kernel.*;
import org.junit.Assert;
import org.junit.Test;


/**
 * Our current thinking is that we will use a JUnit launcher for the proof-of-concept demonstration.  This is that entry-point.
 * See issue-124 for more of the background.
 */
public class PocWalletTest {

    // For now, we will just reuse the from, to, and block for each call (in the future, this will change).
    private byte[] from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private byte[] to = Helpers.randomBytes(Address.LENGTH);
    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 10_000_000;
    private long energyPrice = 1;

    private KernelInterfaceImpl kernel = new KernelInterfaceImpl();
    private Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);

    private byte[] buildTestWalletJar() {
        return JarBuilder.buildJarForMainAndClasses(Wallet.class
                , Multiowned.class
                , AionMap.class
                , AionSet.class
                , AionList.class
                , ByteArrayWrapper.class
                , Operation.class
                , ByteArrayHelpers.class
                , BytesKey.class
                , RequireFailedException.class
                , Daylimit.class
                , EventLogger.class
        );
    }

    /**
     * Tests that a deploy call will store the code for the Wallet JAR.
     * This means that it transformed it correctly and nothing was missing.
     */
    @Test
    public void testDeployWritesCode() {
        byte[] testWalletJar = buildTestWalletJar();
        byte[] testWalletArguments = new byte[0];

        Transaction createTransaction = new Transaction(Transaction.Type.CREATE, from, null, kernel.getNonce(from), 0,
                new CodeAndArguments(testWalletJar, testWalletArguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionContext createContext = new TransactionContextImpl(createTransaction, block);
        TransactionResult createResult = avm.run(createContext);

        Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());
        Assert.assertNotNull(kernel.getCode(createResult.getReturnData()));
    }

    /**
     * Tests that we can run init on the deployed code, albeit as a second transaction (since we haven't yet decided how to invoke init on deploy).
     */
    @Test
    public void testDeployAndCallInit() throws Exception {
        // Constructor args.
        byte[] extra1 = Helpers.randomBytes(Address.LENGTH);
        byte[] extra2 = Helpers.randomBytes(Address.LENGTH);
        int requiredVotes = 2;
        long dailyLimit = 5000;

        byte[] testWalletJar = buildTestWalletJar();
        byte[] testWalletArguments = new byte[0];
        Transaction createTransaction = new Transaction(Transaction.Type.CREATE, from, null, kernel.getNonce(from), 0,
                new CodeAndArguments(testWalletJar, testWalletArguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionContext createContext = new TransactionContextImpl(createTransaction, block);
        TransactionResult createResult = avm.run(createContext);
        Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());

        // contract address is stored in return data
        byte[] contractAddress = createResult.getReturnData();

        byte[] initArgs = encodeInit(extra1, extra2, requiredVotes, dailyLimit);
        Transaction initTransaction = new Transaction(Transaction.Type.CALL, from, contractAddress, kernel.getNonce(from), 0, initArgs, energyLimit, energyPrice);
        TransactionContext initContext = new TransactionContextImpl(initTransaction, block);
        TransactionResult initResult = avm.run(initContext);
        Assert.assertEquals(TransactionResult.Code.SUCCESS, initResult.getStatusCode());
    }

    /**
     * Tests that inner classes work properly within the serialization system (since their constructors need to be marked accessible).
     */
    @Test
    public void testExecuteWithInnerClasses() throws Exception {
        // Constructor args.
        byte[] extra1 = Helpers.randomBytes(Address.LENGTH);
        byte[] extra2 = Helpers.randomBytes(Address.LENGTH);
        int requiredVotes = 2;
        long dailyLimit = 5000;

        // Deploy.
        byte[] contractAddress = deployTestWallet();

        // Run the init.
        runInit(contractAddress, extra1, extra2, requiredVotes, dailyLimit);

        // Call "execute" with something above the daily limit so we will create the "Transaction" inner class instance.
        byte[] to = Helpers.randomBytes(Address.LENGTH);
        byte[] data = Helpers.randomBytes(Address.LENGTH);
        byte[] execArgs = encodeExecute(to, dailyLimit + 1, data);
        Transaction executeTransaction = new Transaction(Transaction.Type.CALL, from, contractAddress, kernel.getNonce(from), 0, execArgs, energyLimit, energyPrice);
        TransactionContext executeContext = new TransactionContextImpl(executeTransaction, block);
        TransactionResult executeResult = avm.run(executeContext);
        Assert.assertEquals(TransactionResult.Code.SUCCESS, executeResult.getStatusCode());
        byte[] toConfirm = (byte[]) TestingHelper.decodeResult(executeResult);

        // Now, confirm as one of the other owners to observe we can instantiate the Transaction instance, from storage.
        kernel.adjustBalance(extra1, 1_000_000_000L);
        byte[] confirmArgs = CallEncoder.confirm(toConfirm);
        Transaction confirmTransaction = new Transaction(Transaction.Type.CALL, extra1, contractAddress, kernel.getNonce(extra1), 0, confirmArgs, energyLimit, energyPrice);
        TransactionContext confirmContext = new TransactionContextImpl(confirmTransaction, block);
        TransactionResult confirmResult = avm.run(confirmContext);
        Assert.assertEquals(TransactionResult.Code.SUCCESS, confirmResult.getStatusCode()); // transfer to non-existing accounts
    }


    private void runInit(byte[] contractAddress, byte[] extra1, byte[] extra2, int requiredVotes, long dailyLimit) throws Exception {
        byte[] initArgs = encodeInit(extra1, extra2, requiredVotes, dailyLimit);
        Transaction initTransaction = new Transaction(Transaction.Type.CALL, from, contractAddress, kernel.getNonce(from), 0, initArgs, energyLimit, energyPrice);
        TransactionContext initContext = new TransactionContextImpl(initTransaction, block);
        TransactionResult initResult = avm.run(initContext);
        Assert.assertEquals(TransactionResult.Code.SUCCESS, initResult.getStatusCode());
    }

    private byte[] deployTestWallet() {
        byte[] testWalletJar = buildTestWalletJar();
        byte[] testWalletArguments = new byte[0];

        Transaction createTransaction = new Transaction(Transaction.Type.CREATE, from, null, kernel.getNonce(from), 0,
                new CodeAndArguments(testWalletJar, testWalletArguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionContext createContext = new TransactionContextImpl(createTransaction, block);
        TransactionResult createResult = avm.run(createContext);
        Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());

        // contract address is stored in return data
        byte[] contractAddress = createResult.getReturnData();
        return contractAddress;
    }

    /**
     * Just calls CallEncoder after faking up Address objects.
     */
    private static byte[] encodeInit(byte[] extra1Bytes, byte[] extra2Bytes, int requiredVotes, long dailyLimit) throws Exception {
        Address extra1 = createAddressInFakeContract(extra1Bytes);
        Address extra2 = createAddressInFakeContract(extra2Bytes);

        return CallEncoder.init(extra1, extra2, requiredVotes, dailyLimit);
    }

    /**
     * Just calls CallEncoder after faking up Address objects.
     */
    private static byte[] encodeExecute(byte[] toBytes, long value, byte[] data) throws Exception {
        Address to = createAddressInFakeContract(toBytes);

        return CallEncoder.execute(to, value, data);
    }

    private static Address createAddressInFakeContract(byte[] bytes) {
        // Create a fake runtime for encoding the arguments (since these are shadow objects - they can only be instantiated within the context of a contract).
        IHelper.currentContractHelper.set(new IHelper() {
            @Override
            public void externalChargeEnergy(long cost) {
                // free!!!
            }

            @Override
            public void externalSetEnergy(long energy) {
                Assert.fail("Not in test");
            }

            @Override
            public long externalGetEnergyRemaining() {
                Assert.fail("Not in test");
                return 0;
            }

            @Override
            public Class<?> externalWrapAsClass(java.lang.Class<?> input) {
                Assert.fail("Not in test");
                return null;
            }

            @Override
            public int externalGetNextHashCode() {
                // Will be called.
                return 1;
            }

            @Override
            public int captureSnapshotAndNextHashCode() {
                Assert.fail("Not in test");
                return 0;
            }

            @Override
            public void applySpanshotAndNextHashCode(int nextHashCode) {
                Assert.fail("Not in test");
            }

            @Override
            public void externalBootstrapOnly() {
                Assert.fail("Not in test");
            }
        });
        Address instance = new Address(bytes);
        IHelper.currentContractHelper.remove();
        return instance;
    }
}
