package org.aion.avm.core.testWallet;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aion.avm.api.IBlockchainRuntime;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.ClassHierarchyForest;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassGenerator;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.dappreading.LoadedJar;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.api.Address;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;


/**
 * This is the first cut at a real application we can put on the AVM, based on the Solidity testWallet.
 * This is the entry-point we would register as our "main" class.  It is responsible for decoding the input
 * and determining what routine on the other components must be called to satisfy the request.
 * 
 * Original:  https://github.com/aionnetwork/aion_fastvm/blob/master/solidity/tests/contracts/testWallet.sol
 * 
 * NOTE:  This is not even ready to be used with in the AVM.  It is currently being built as a stand-alone
 * test application, just to make sure that the basics of the core algorithm are correct.
 * After that, it will be moved into a test on AvmImpl, where its special restrictions can be slowly removed
 * as that implementation becomes fleshed out.
 */
public class Deployer {
    public static void main(String[] args) throws Throwable {
        // This is eventually just a test harness to invoke the decode() but, for now, it will actually invoke the calls, directly.
        // In order to instantiate Address objects, we need to install the IHelper.
        System.out.println("--- DIRECT ---");
        IHelper.currentContractHelper.set(new FakeHelper());
        invokeDirect(args);
        IHelper.currentContractHelper.set(null);
        System.out.println("--- DONE (DIRECT) ---");
        
        System.out.println("----- ***** -----");
        
        // Now, try the transformed version.
        System.out.println("--- TRANSFORMED ---");
        invokeTransformed(args);
        System.out.println("--- DONE (TRANSFORMED) ---");
    }


    private static void invokeDirect(String[] args) {
        // Note that this loggingRuntime is just to give us a consistent interface for reading the eventCounts.
        Map<String, Integer> eventCounts = new HashMap<>();
        TestingRuntime loggingRuntime = new TestingRuntime(null, null, eventCounts);
        
        // We can now init the actual contract (the Wallet is the root so init it).
        Address sender = buildAddress(1);
        Address extra1 = buildAddress(2);
        Address extra2 = buildAddress(3);
        int requiredVotes = 2;
        long dailyLimit = 5000;
        // Init the Wallet.
        Helper.blockchainRuntime = new TestingRuntime(sender, null, eventCounts);
        DirectProxy.init(extra1, extra2, requiredVotes, dailyLimit);
        
        // First of all, just prove that we can send them some energy.
        Address paymentFrom = buildAddress(4);
        long paymendValue = 5;
        Helper.blockchainRuntime = new TestingRuntime(paymentFrom, new byte[] {5,6,42}, eventCounts);
        DirectProxy.payable(paymentFrom, paymendValue);
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kDeposit));
        
        // Try to add an owner - we need to call this twice to see the event output: sender and extra1.
        Address newOwner = buildAddress(5);
        try {
            Helper.blockchainRuntime = new TestingRuntime(sender, new byte[] {5,6,42}, eventCounts);
            DirectProxy.addOwner(newOwner);
        } catch (RequireFailedException e) {
            // Expected.
        }
        Assert.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kOwnerAdded));
        Helper.blockchainRuntime = new TestingRuntime(extra1, new byte[] {5,6,42}, eventCounts);
        DirectProxy.addOwner(newOwner);
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kOwnerAdded));
        
        // Send a normal transaction, which is under the limit, and observe that it goes through.
        Address transactionTo = buildAddress(6);
        long transactionSize = dailyLimit - 1;
        Assert.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kSingleTransact));
        Helper.blockchainRuntime = new TestingRuntime(sender, new byte[] {5,6,42}, eventCounts);
        DirectProxy.execute(transactionTo, transactionSize, new byte[] {1});
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kSingleTransact));
        
        // Now, send another transaction, observe that it requires multisig confirmation, and confirm it with our new owner.
        Address confirmTransactionTo = buildAddress(7);
        Assert.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kConfirmationNeeded));
        Helper.blockchainRuntime = new TestingRuntime(sender, new byte[] {5,6,42}, eventCounts);
        byte[] toConfirm = DirectProxy.execute(confirmTransactionTo, transactionSize, new byte[] {1});
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kSingleTransact));
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kConfirmationNeeded));
        Helper.blockchainRuntime = new TestingRuntime(newOwner, new byte[] {5,6,42}, eventCounts);
        boolean didConfirm = DirectProxy.confirm(toConfirm);
        Assert.assertTrue(didConfirm);
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kMultiTransact));
        
        // Change the count of required confirmations.
        try {
            Helper.blockchainRuntime = new TestingRuntime(sender, new byte[] {5,6,42}, eventCounts);
            DirectProxy.changeRequirement(3);
        } catch (RequireFailedException e) {
            // Expected.
        }
        Assert.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kRequirementChanged));
        Helper.blockchainRuntime = new TestingRuntime(extra1, new byte[] {5,6,42}, eventCounts);
        DirectProxy.changeRequirement(3);
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kRequirementChanged));
        
        // Change the owner.
        Address lateOwner = buildAddress(8);
        Helper.blockchainRuntime = new TestingRuntime(lateOwner, new byte[] {5,6,42}, eventCounts);
        Assert.assertTrue(sender.equals(DirectProxy.getOwner(0)));
        try {
            Helper.blockchainRuntime = new TestingRuntime(sender, new byte[] {5,6,42}, eventCounts);
            DirectProxy.changeOwner(sender, lateOwner);
            Assert.assertTrue(false);
        } catch (RequireFailedException e) {
            // Expected.
        }
        try {
            Helper.blockchainRuntime = new TestingRuntime(extra1, new byte[] {5,6,42}, eventCounts);
            DirectProxy.changeOwner(sender, lateOwner);
            Assert.assertTrue(false);
        } catch (RequireFailedException e) {
            // Expected.
        }
        Assert.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kOwnerChanged));
        Helper.blockchainRuntime = new TestingRuntime(extra2, new byte[] {5,6,42}, eventCounts);
        DirectProxy.changeOwner(sender, lateOwner);
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kOwnerChanged));
        
        // Try to remove an owner, but have someone revoke that so that it can't happen.
        try {
            Helper.blockchainRuntime = new TestingRuntime(lateOwner, new byte[] {5,6,42}, eventCounts);
            DirectProxy.removeOwner(extra1);
            Assert.assertTrue(false);
        } catch (RequireFailedException e) {
            // Expected.
        }
        try {
            Helper.blockchainRuntime = new TestingRuntime(extra2, new byte[] {5,6,42}, eventCounts);
            DirectProxy.removeOwner(extra1);
            Assert.assertTrue(false);
        } catch (RequireFailedException e) {
            // Expected.
        }
        Assert.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kRevoke));
        Helper.blockchainRuntime = new TestingRuntime(lateOwner, new byte[] {5,6,42}, eventCounts);
        DirectProxy.revoke();
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kRevoke));
        try {
            // This fails since one of the owners revoked.
            Helper.blockchainRuntime = new TestingRuntime(extra1, new byte[] {5,6,42}, eventCounts);
            DirectProxy.removeOwner(extra1);
            Assert.assertTrue(false);
        } catch (RequireFailedException e) {
            // Expected.
        }
        Assert.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kOwnerRemoved));
        // But this succeeds when they re-agree.
        Helper.blockchainRuntime = new TestingRuntime(lateOwner, new byte[] {5,6,42}, eventCounts);
        DirectProxy.removeOwner(extra1);
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kOwnerRemoved));
        Helper.blockchainRuntime = new TestingRuntime(extra1, new byte[] {5,6,42}, eventCounts);
        Assert.assertTrue(extra2.equals(DirectProxy.getOwner(0)));
        
        // We should have seen 13 confirmations over the course of the test run.
        Assert.assertTrue(13 == loggingRuntime.getEventCount(EventLogger.kConfirmation));
    }

    private static void invokeTransformed(String[] args) throws Throwable {
        Map<String, Integer> eventCounts = new HashMap<>();
        AvmSharedClassLoader sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateShadowJDK());
        
        byte[] jarBytes = JarBuilder.buildJarForMainAndClasses(Wallet.class
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
                , Abi.class
        );
        LoadedJar jar = LoadedJar.fromBytes(jarBytes);
        
        AvmImpl avm = new AvmImpl(sharedClassLoader);
        Map<String, byte[]> transformedClasses = Helpers.mapIncludingHelperBytecode(avm.transformClasses(jar.classBytesByQualifiedNames, ClassHierarchyForest.createForestFrom(jar)));
        
        AvmClassLoader loader = new AvmClassLoader(sharedClassLoader, transformedClasses);
        Function<String, byte[]> wrapperGenerator = (cName) -> ArrayWrappingClassGenerator.arrayWrappingFactory(cName, loader);
        loader.addHandler(wrapperGenerator);

        // (note that setting a single runtime instance for this group of invocations doesn't really make sense - it just provides the energy counter).
        Helpers.instantiateHelper(loader, 1_000_000L);
        // Note that this single externalRuntime instance doesn't really make sense - it is only useful in the cases where we aren't using
        // it for invocation context, just environment (energy counter, event logging, etc).
        TestingRuntime externalRuntime = new TestingRuntime(null, null, eventCounts);
        
        // issue-112:  We create this classProvider to make it easier to emulate a full reload of a DApp.
        // The idea is that we can reload a fresh Wallet class from a new AvmClassLoader for each invocation into the DApp in order to simulate
        // the DApp state at the point where it receives a call.
        // (currently, we just return the same walletClass instance since our persistence design is still being prototyped).
        Class<?> walletClass = loader.loadUserClassByOriginalName(Wallet.class.getName());
        Supplier<Class<?>> classProvider = () -> {
            return walletClass;
        };
        
        // Now, run the test.
        Address sender = buildAddress(1);
        Address extra1 = buildAddress(2);
        Address extra2 = buildAddress(3);
        
        // We can now init the actual contract (the Wallet is the root so init it).
        int requiredVotes = 2;
        long dailyLimit = 5000;
        Helpers.attachBlockchainRuntime(loader, new TestingRuntime(sender, new byte[] {5,6,42}, eventCounts));
        CallProxy.init(classProvider, extra1, extra2, requiredVotes, dailyLimit);
        
        // First of all, just prove that we can send them some energy.
        Address paymentFrom = buildAddress(4);
        long paymentValue = 5;
        Helpers.attachBlockchainRuntime(loader, new TestingRuntime(paymentFrom, new byte[] {5,6,42}, eventCounts));
        CallProxy.payable(classProvider, paymentFrom, paymentValue);
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kDeposit));
        
        // Try to add an owner - we need to call this twice to see the event output: sender and extra1.
        Address newOwner = buildAddress(5);
        try {
            Helpers.attachBlockchainRuntime(loader, new TestingRuntime(sender, new byte[] {5,6,42}, eventCounts));
            CallProxy.addOwner(classProvider, newOwner);
            Assert.assertTrue(false);
        } catch (InvocationTargetException e) {
            // Expected re-mapped RequireFailedException.
            Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + RequireFailedException.class.getName()).equals(e.getCause().getClass().getName()));
        }
        Assert.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kOwnerAdded));
        Helpers.attachBlockchainRuntime(loader, new TestingRuntime(extra1, new byte[] {5,6,42}, eventCounts));
        CallProxy.addOwner(classProvider, newOwner);
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kOwnerAdded));
        
        // Send a normal transaction, which is under the limit, and observe that it goes through.
        Address transactionTo = buildAddress(6);
        long transactionSize = dailyLimit - 1;
        Assert.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kSingleTransact));
        Helpers.attachBlockchainRuntime(loader, new TestingRuntime(sender, new byte[] {5,6,42}, eventCounts));
        CallProxy.execute(classProvider, transactionTo, transactionSize, new byte[] {1});
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kSingleTransact));
        
        // Now, send another transaction, observe that it requires multisig confirmation, and confirm it with our new owner.
        Address confirmTransactionTo = buildAddress(7);
        Assert.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kConfirmationNeeded));
        Helpers.attachBlockchainRuntime(loader, new TestingRuntime(sender, new byte[] {5,6,42}, eventCounts));
        byte[] toConfirm = CallProxy.execute(classProvider, confirmTransactionTo, transactionSize, new byte[] {1});
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kSingleTransact));
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kConfirmationNeeded));
        Helpers.attachBlockchainRuntime(loader, new TestingRuntime(newOwner, new byte[] {5,6,42}, eventCounts));
        boolean didConfirm = CallProxy.confirm(classProvider, toConfirm);
        Assert.assertTrue(didConfirm);
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kMultiTransact));
        
        // Change the count of required confirmations.
        try {
            Helpers.attachBlockchainRuntime(loader, new TestingRuntime(sender, new byte[] {5,6,42}, eventCounts));
            CallProxy.changeRequirement(classProvider, 3);
            Assert.assertTrue(false);
        } catch (InvocationTargetException e) {
            // Expected re-mapped RequireFailedException.
            Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + RequireFailedException.class.getName()).equals(e.getCause().getClass().getName()));
        }
        Assert.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kRequirementChanged));
        Helpers.attachBlockchainRuntime(loader, new TestingRuntime(extra1, new byte[] {5,6,42}, eventCounts));
        CallProxy.changeRequirement(classProvider, 3);
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kRequirementChanged));
        
        // Change the owner.
        Address lateOwner = buildAddress(8);
        Helpers.attachBlockchainRuntime(loader, new TestingRuntime(extra1, new byte[] {5,6,42}, eventCounts));
        Assert.assertTrue(sender.equals(CallProxy.getOwner(classProvider, 0)));
        try {
            Helpers.attachBlockchainRuntime(loader, new TestingRuntime(sender, new byte[] {5,6,42}, eventCounts));
            CallProxy.changeOwner(classProvider, sender, lateOwner);
            Assert.assertTrue(false);
            Assert.assertTrue(false);
        } catch (InvocationTargetException e) {
            // Expected re-mapped RequireFailedException.
            Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + RequireFailedException.class.getName()).equals(e.getCause().getClass().getName()));
        }
        try {
            Helpers.attachBlockchainRuntime(loader, new TestingRuntime(extra1, new byte[] {5,6,42}, eventCounts));
            CallProxy.changeOwner(classProvider, sender, lateOwner);
            Assert.assertTrue(false);
        } catch (InvocationTargetException e) {
            // Expected re-mapped RequireFailedException.
            Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + RequireFailedException.class.getName()).equals(e.getCause().getClass().getName()));
        }
        Assert.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kOwnerChanged));
        Helpers.attachBlockchainRuntime(loader, new TestingRuntime(extra2, new byte[] {5,6,42}, eventCounts));
        CallProxy.changeOwner(classProvider, sender, lateOwner);
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kOwnerChanged));
        
        // Try to remove an owner, but have someone revoke that so that it can't happen.
        try {
            Helpers.attachBlockchainRuntime(loader, new TestingRuntime(lateOwner, new byte[] {5,6,42}, eventCounts));
            CallProxy.removeOwner(classProvider, extra1);
            Assert.assertTrue(false);
        } catch (InvocationTargetException e) {
            // Expected re-mapped RequireFailedException.
            Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + RequireFailedException.class.getName()).equals(e.getCause().getClass().getName()));
        }
        try {
            Helpers.attachBlockchainRuntime(loader, new TestingRuntime(extra2, new byte[] {5,6,42}, eventCounts));
            CallProxy.removeOwner(classProvider, extra1);
            Assert.assertTrue(false);
        } catch (InvocationTargetException e) {
            // Expected re-mapped RequireFailedException.
            Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + RequireFailedException.class.getName()).equals(e.getCause().getClass().getName()));
        }
        Assert.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kRevoke));
        Helpers.attachBlockchainRuntime(loader, new TestingRuntime(lateOwner, new byte[] {5,6,42}, eventCounts));
        CallProxy.revoke(classProvider);
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kRevoke));
        try {
            // This fails since one of the owners revoked.
            Helpers.attachBlockchainRuntime(loader, new TestingRuntime(extra1, new byte[] {5,6,42}, eventCounts));
            CallProxy.removeOwner(classProvider, extra1);
            Assert.assertTrue(false);
        } catch (InvocationTargetException e) {
            // Expected re-mapped RequireFailedException.
            Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + RequireFailedException.class.getName()).equals(e.getCause().getClass().getName()));
        }
        Assert.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kOwnerRemoved));
        // But this succeeds when they re-agree.
        Helpers.attachBlockchainRuntime(loader, new TestingRuntime(lateOwner, new byte[] {5,6,42}, eventCounts));
        CallProxy.removeOwner(classProvider, extra1);
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kOwnerRemoved));
        Helpers.attachBlockchainRuntime(loader, new TestingRuntime(extra1, new byte[] {5,6,42}, eventCounts));
        Assert.assertTrue(extra2.equals(CallProxy.getOwner(classProvider, 0)));
        
        // We should have seen 13 confirmations over the course of the test run.
        Assert.assertTrue(13 == externalRuntime.getEventCount(EventLogger.kConfirmation));
    }

    private static Address buildAddress(int fillByte) {
        byte[] raw = new byte[32];
        for (int i = 0; i < raw.length; ++ i) {
            raw[i] = (byte)fillByte;
        }
        return new Address(raw);
    }


    private static class TestingRuntime extends org.aion.avm.shadow.java.lang.Object implements IBlockchainRuntime {
        private final Address sender;
        private final ByteArray data;
        private final Map<java.lang.String, Integer> eventCounts;
        
        public TestingRuntime(Address sender, byte[] data, Map<java.lang.String, Integer> eventCounts) {
            this.sender = sender;
            this.data = (null != data)
                    ? new ByteArray(data)
                    : null;
            this.eventCounts = eventCounts;
        }
        
        @Override
        public Address avm_getSender() {
            return this.sender;
        }
        @Override
        public Address avm_getAddress() {
            Assert.unimplemented("TODO");
            return null;
        }
        @Override
        public long avm_getEnergyLimit() {
            // Just return a big number so we can run.
            return 1000000;
        }
        @Override
        public ByteArray avm_getData() {
            return this.data;
        }
        @Override
        public ByteArray avm_getStorage(ByteArray key) {
            Assert.unimplemented("TODO");
            return null;
        }
        @Override
        public void avm_putStorage(ByteArray key, ByteArray value) {
            Assert.unimplemented("TODO");
        }
        @Override
        public void avm_updateCode(ByteArray newCode) {
            Assert.unimplemented("TODO");
        }
        @Override
        public void avm_selfDestruct(Address beneficiary) {
            Assert.unimplemented("TODO");
        }
        public long avm_getBlockEpochSeconds() {
            // For now, always say it is day 1:  seconds per day.
            return 60 * 60 * 24;
        }
        @Override
        public long avm_getBlockNumber() {
            // For now, say that this is block 1.
            return 1;
        }
        @Override
        public ByteArray avm_sha3(ByteArray data) {
            // For tests, we just return the initial data with a prefix.
            byte[] result = new byte[data.length() + 1];
            result[0] = (byte)255;
            System.arraycopy(data.getUnderlying(), 0, result, 1, data.length());
            return new ByteArray(result);
        }
        @Override
        public ByteArray avm_call(Address targetAddress, ByteArray value, ByteArray payload, long energyToSend) {
            // We probably want to capture/verify this more concretely but, for now, just return the payload to synthesize "something".
            return payload;
        }
        @Override
        public void avm_log(ByteArray index0, ByteArray data) {
            String reconstituted = new String(index0.getUnderlying(), StandardCharsets.UTF_8);
            System.out.println(reconstituted);
            Integer oldCount = this.eventCounts.get(reconstituted);
            int newCount = ((null != oldCount) ? oldCount : 0) + 1;
            this.eventCounts.put(reconstituted, newCount);
        }
        public int getEventCount(String event) {
            Integer count = this.eventCounts.get(event);
            return (null != count) ? count : 0;
        }
    }


    private static class FakeHelper implements IHelper {
        @Override
        public void externalChargeEnergy(long cost) {
            Assert.unreachable("This shouldn't be called in this test.");
        }
        @Override
        public void externalSetEnergy(long energy) {
            Assert.unreachable("This shouldn't be called in this test.");
        }
        @Override
        public long externalGetEnergyRemaining() {
            Assert.unreachable("This shouldn't be called in this test.");
            return 0;
        }

        @Override
        public org.aion.avm.shadow.java.lang.String externalWrapAsString(String input) {
            Assert.unreachable("This shouldn't be called in this test.");
            return null;
        }
        @Override
        public int externalGetNextHashCode() {
            // Just return anything.
            return 0;
        }
    }
}
