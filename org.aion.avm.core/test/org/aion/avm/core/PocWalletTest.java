package org.aion.avm.core;

import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.testExchange.*;
import org.aion.avm.core.testWallet.ByteArrayHelpers;
import org.aion.avm.core.testWallet.ByteArrayWrapper;
import org.aion.avm.core.testWallet.BytesKey;
import org.aion.avm.core.testWallet.CallEncoder;
import org.aion.avm.core.testWallet.Daylimit;
import org.aion.avm.core.testWallet.EventLogger;
import org.aion.avm.core.testWallet.Multiowned;
import org.aion.avm.core.testWallet.Operation;
import org.aion.avm.core.testWallet.RequireFailedException;
import org.aion.avm.core.testWallet.Wallet;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.IHelper;
import org.aion.avm.shadow.java.lang.Class;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.avm.api.Address;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;


/**
 * Our current thinking is that we will use a JUnit launcher for the proof-of-concept demonstration.  This is that entry-point.
 * See issue-124 for more of the background.
 */
@RunWith(Enclosed.class)
public class PocWalletTest {

    public static class POCWallet {

        // For now, we will just reuse the from, to, and block for each call (in the future, this will change).
        private byte[] from = Helpers.randomBytes(Address.LENGTH);
        private byte[] to = Helpers.randomBytes(Address.LENGTH);
        private Block block = new Block(1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
        private long energyLimit = 5_000_000;
        private long energyPrice = 1;

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

            Transaction createTransaction = new Transaction(Transaction.Type.CREATE, from, to, 0,
                    Helpers.encodeCodeAndData(testWalletJar, testWalletArguments), energyLimit, energyPrice);
            KernelInterface kernelInterface = new KernelInterfaceImpl();
            TransactionContext createContext = new TransactionContextImpl(createTransaction, block);
            TransactionResult createResult = new AvmImpl(kernelInterface).run(createContext);

            Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());
            Assert.assertNotNull(kernelInterface.getCode(createResult.getReturnData()));
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
            Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());

            byte[] testWalletJar = buildTestWalletJar();
            byte[] testWalletArguments = new byte[0];
            Transaction createTransaction = new Transaction(Transaction.Type.CREATE, from, to, 0,
                    Helpers.encodeCodeAndData(testWalletJar, testWalletArguments), energyLimit, energyPrice);
            TransactionContext createContext = new TransactionContextImpl(createTransaction, block);
            TransactionResult createResult = avm.run(createContext);
            Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());

            // contract address is stored in return data
            byte[] contractAddress = createResult.getReturnData();

            byte[] initArgs = encodeInit(extra1, extra2, requiredVotes, dailyLimit);
            Transaction initTransaction = new Transaction(Transaction.Type.CALL, from, contractAddress, 0, initArgs, energyLimit, energyPrice);
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
            Avm avm = NodeEnvironment.singleton.buildAvmInstance(new KernelInterfaceImpl());
            
            // Deploy.
            byte[] contractAddress = deployTestWallet();
            
            // Run the init.
            runInit(contractAddress, extra1, extra2, requiredVotes, dailyLimit);
            
            // Call "execute" with something above the daily limit so we will create the "Transaction" inner class instance.
            byte[] to = Helpers.randomBytes(Address.LENGTH);
            byte[] data = Helpers.randomBytes(Address.LENGTH);
            byte[] execArgs = encodeExecute(to, dailyLimit + 1, data);
            Transaction executeTransaction = new Transaction(Transaction.Type.CALL, from, contractAddress, 0, execArgs, energyLimit, energyPrice);
            TransactionContext executeContext = new TransactionContextImpl(executeTransaction, block);
            TransactionResult executeResult = avm.run(executeContext);
            Assert.assertEquals(TransactionResult.Code.SUCCESS, executeResult.getStatusCode());
            byte[] toConfirm = (byte[]) TestingHelper.decodeResult(executeResult);
            
            // Now, confirm as one of the other owners to observe we can instantiate the Transaction instance, from storage.
            byte[] confirmArgs = CallEncoder.confirm(toConfirm);
            Transaction confirmTransaction = new Transaction(Transaction.Type.CALL, extra1, contractAddress, 0, confirmArgs, energyLimit, energyPrice);
            TransactionContext confirmContext = new TransactionContextImpl(confirmTransaction, block);
            // TODO:  Change this once we have something reasonable to cross-call.  For now, this hits NPE since it isn't calling anything real.
            TransactionResult confirmResult = avm.run(confirmContext);
            Assert.assertEquals(TransactionResult.Code.FAILURE, confirmResult.getStatusCode());
        }


        private void runInit(byte[] contractAddress, byte[] extra1, byte[] extra2, int requiredVotes, long dailyLimit) throws Exception {
            byte[] initArgs = encodeInit(extra1, extra2, requiredVotes, dailyLimit);
            Transaction initTransaction = new Transaction(Transaction.Type.CALL, from, contractAddress, 0, initArgs, energyLimit, energyPrice);
            TransactionContext initContext = new TransactionContextImpl(initTransaction, block);
            TransactionResult initResult = new AvmImpl(new KernelInterfaceImpl()).run(initContext);
            Assert.assertEquals(TransactionResult.Code.SUCCESS, initResult.getStatusCode());
        }

        private byte[] deployTestWallet() {
            byte[] testWalletJar = buildTestWalletJar();
            byte[] testWalletArguments = new byte[0];

            Transaction createTransaction = new Transaction(Transaction.Type.CREATE, from, to, 0,
                    Helpers.encodeCodeAndData(testWalletJar, testWalletArguments), energyLimit, energyPrice);
            TransactionContext createContext = new TransactionContextImpl(createTransaction, block);
            TransactionResult createResult = new AvmImpl(new KernelInterfaceImpl()).run(createContext);
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
                    Assert.fail("Not in test");
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
                }});
            Address instance = new Address(bytes);
            IHelper.currentContractHelper.remove();
            return instance;
        }
    }
}
