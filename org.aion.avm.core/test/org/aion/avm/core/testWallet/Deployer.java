package org.aion.avm.core.testWallet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.Forest;
import org.aion.avm.core.HierarchyTreeBuilder;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassGenerator;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
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
        IHelper.currentContractHelper.set(new FakeHelper());
        invokeDirect(args);
        IHelper.currentContractHelper.set(null);
        
        // Now, try the transformed version.
        invokeTransformed(args);
    }


    private static void invokeDirect(String[] args) {
        // We create a test runtime but this is only to support the EventLogger.
        TestingRuntime loggingRuntime = new TestingRuntime(null, null);
        EventLogger.init(loggingRuntime);
        
        // We can now init the actual contract (the Wallet is the root so init it).
        Address sender = buildAddress(1);
        Address extra1 = buildAddress(2);
        Address extra2 = buildAddress(3);
        int requiredVotes = 2;
        long dailyLimit = 5000;
        // This helper to avoid creating/referencing the arraywrapper will init Wallet.
        BlockchainRuntime deploymentRuntime = new TestingRuntime(sender, null);
        Wallet.avoidArrayWrappingFactory(deploymentRuntime, extra1, extra2, requiredVotes, dailyLimit);
        
        // First of all, just prove that we can send them some energy.
        Address paymentFrom = buildAddress(4);
        long paymendValue = 5;
        DirectProxy.payable(paymentFrom, paymendValue);
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kDeposit));
        
        // Try to add an owner - we need to call this twice to see the event output: sender and extra1.
        Address newOwner = buildAddress(5);
        try {
            DirectProxy.addOwner(new TestingRuntime(sender, new byte[] {5,6,42}), newOwner);
        } catch (RequireFailedException e) {
            // Expected.
        }
        Assert.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kOwnerAdded));
        DirectProxy.addOwner(new TestingRuntime(extra1, new byte[] {5,6,42}), newOwner);
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kOwnerAdded));
        
        // Send a normal transaction, which is under the limit, and observe that it goes through.
        Address transactionTo = buildAddress(6);
        long transactionSize = dailyLimit - 1;
        Assert.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kSingleTransact));
        DirectProxy.execute(new TestingRuntime(sender, new byte[] {5,6,42}), transactionTo, transactionSize, new byte[] {1});
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kSingleTransact));
        
        // Now, send another transaction, observe that it requires multisig confirmation, and confirm it with our new owner.
        Address confirmTransactionTo = buildAddress(7);
        Assert.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kConfirmationNeeded));
        byte[] toConfirm = DirectProxy.execute(new TestingRuntime(sender, new byte[] {5,6,42}), confirmTransactionTo, transactionSize, new byte[] {1});
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kSingleTransact));
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kConfirmationNeeded));
        boolean didConfirm = DirectProxy.confirm(new TestingRuntime(newOwner, new byte[] {5,6,42}), toConfirm);
        Assert.assertTrue(didConfirm);
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kMultiTransact));
        
        // Change the count of required confirmations.
        try {
            DirectProxy.changeRequirement(new TestingRuntime(sender, new byte[] {5,6,42}), 3);
        } catch (RequireFailedException e) {
            // Expected.
        }
        Assert.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kRequirementChanged));
        DirectProxy.changeRequirement(new TestingRuntime(extra1, new byte[] {5,6,42}), 3);
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kRequirementChanged));
        
        // Change the owner.
        Address lateOwner = buildAddress(8);
        Assert.assertTrue(sender.equals(DirectProxy.getOwner(new TestingRuntime(lateOwner, new byte[] {5,6,42}), 0)));
        try {
            DirectProxy.changeOwner(new TestingRuntime(sender, new byte[] {5,6,42}), sender, lateOwner);
            Assert.assertTrue(false);
        } catch (RequireFailedException e) {
            // Expected.
        }
        try {
            DirectProxy.changeOwner(new TestingRuntime(extra1, new byte[] {5,6,42}), sender, lateOwner);
            Assert.assertTrue(false);
        } catch (RequireFailedException e) {
            // Expected.
        }
        Assert.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kOwnerChanged));
        DirectProxy.changeOwner(new TestingRuntime(extra2, new byte[] {5,6,42}), sender, lateOwner);
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kOwnerChanged));
        
        // Try to remove an owner, but have someone revoke that so that it can't happen.
        try {
            DirectProxy.removeOwner(new TestingRuntime(lateOwner, new byte[] {5,6,42}), extra1);
            Assert.assertTrue(false);
        } catch (RequireFailedException e) {
            // Expected.
        }
        try {
            DirectProxy.removeOwner(new TestingRuntime(extra2, new byte[] {5,6,42}), extra1);
            Assert.assertTrue(false);
        } catch (RequireFailedException e) {
            // Expected.
        }
        Assert.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kRevoke));
        DirectProxy.revoke(new TestingRuntime(lateOwner, new byte[] {5,6,42}));
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kRevoke));
        try {
            // This fails since one of the owners revoked.
            DirectProxy.removeOwner(new TestingRuntime(extra1, new byte[] {5,6,42}), extra1);
            Assert.assertTrue(false);
        } catch (RequireFailedException e) {
            // Expected.
        }
        Assert.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kOwnerRemoved));
        // But this succeeds when they re-agree.
        DirectProxy.removeOwner(new TestingRuntime(lateOwner, new byte[] {5,6,42}), extra1);
        Assert.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kOwnerRemoved));
        Assert.assertTrue(extra2.equals(DirectProxy.getOwner(new TestingRuntime(extra1, new byte[] {5,6,42}), 0)));
        
        // We should have seen 13 confirmations over the course of the test run.
        Assert.assertTrue(13 == loggingRuntime.getEventCount(EventLogger.kConfirmation));
    }

    private static void invokeTransformed(String[] args) throws Throwable {
        AvmSharedClassLoader sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
        
        LoadingHelper helper = new LoadingHelper();
        helper.loadClass(Multiowned.class);
        helper.loadClass(AionMap.class);
        helper.loadClass(AionSet.class);
        helper.loadClass(AionList.class);
        helper.loadClass(ByteArrayWrapper.class);
        helper.loadClass(Operation.class);
        helper.loadClass(ByteArrayHelpers.class);
        helper.loadClass(BytesKey.class);
        helper.loadClass(Multiowned.PendingState.class);
        helper.loadClass(RequireFailedException.class);
        helper.loadClass(Daylimit.class);
        helper.loadClass(Wallet.class);
        helper.loadClass(Wallet.Transaction.class);
        helper.loadClass(EventLogger.class);
        helper.loadClass(Abi.class);
        helper.loadClass(Abi.Encoder.class);
        helper.loadClass(Abi.Decoder.class);
        
        AvmImpl avm = new AvmImpl(sharedClassLoader);
        Map<String, Integer> runtimeObjectSizes = AvmImpl.computeRuntimeObjectSizes();
        Map<String, Integer> allObjectSizes = AvmImpl.computeObjectSizes(helper.getClassHierarchy(), runtimeObjectSizes);
        
        Map<String, byte[]> transformedClasses = Helpers.mapIncludingHelperBytecode(avm.transformClasses(helper.igetInputClasses(), helper.getClassHierarchy(), allObjectSizes));
        
        AvmClassLoader loader = new AvmClassLoader(sharedClassLoader, transformedClasses);
        Function<String, byte[]> wrapperGenerator = (cName) -> ArrayWrappingClassGenerator.arrayWrappingFactory(cName, true, loader);
        loader.addHandler(wrapperGenerator);
        
        // Note that this single externalRuntime instance doesn't really make sense - it is only useful in the cases where we aren't using
        // it for invocation context, just environment (energy counter, event logging, etc).
        TestingRuntime externalRuntime = new TestingRuntime(null, null);
        // (note that setting a single runtime instance for this group of invocations doesn't really make sense - it just provides the energy counter).
        Helpers.instantiateHelper(loader, externalRuntime);
        
        
        
        
        
        // Now, run the test.
        Address sender = buildAddress(1);
        Address extra1 = buildAddress(2);
        Address extra2 = buildAddress(3);
        Class<?> walletClass = loader.loadUserClassByOriginalName(Wallet.class.getName());
        loader.loadUserClassByOriginalName(EventLogger.class.getName())
            .getMethod(UserClassMappingVisitor.mapMethodName("init"), BlockchainRuntime.class)
            .invoke(null, externalRuntime);
        
        // We can now init the actual contract (the Wallet is the root so init it).
        int requiredVotes = 2;
        long dailyLimit = 5000;
        // This helper to avoid creating/referencing the arraywrapper will init Wallet.
        BlockchainRuntime deploymentRuntime = new TestingRuntime(sender, null);
        walletClass
            .getMethod(UserClassMappingVisitor.mapMethodName("avoidArrayWrappingFactory"), BlockchainRuntime.class, Address.class, Address.class, int.class, long.class)
            .invoke(null, deploymentRuntime, extra1, extra2, requiredVotes, dailyLimit);
        
        // First of all, just prove that we can send them some energy.
        Address paymentFrom = buildAddress(4);
        long paymentValue = 5;
        CallProxy.payable(walletClass, paymentFrom, paymentValue);
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kDeposit));
        
        // Try to add an owner - we need to call this twice to see the event output: sender and extra1.
        Address newOwner = buildAddress(5);
        try {
            CallProxy.addOwner(walletClass, new TestingRuntime(sender, new byte[] {5,6,42}), newOwner);
            Assert.assertTrue(false);
        } catch (InvocationTargetException e) {
            // Expected re-mapped RequireFailedException.
            Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + RequireFailedException.class.getName()).equals(e.getCause().getClass().getName()));
        }
        Assert.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kOwnerAdded));
        CallProxy.addOwner(walletClass, new TestingRuntime(extra1, new byte[] {5,6,42}), newOwner);
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kOwnerAdded));
        
        // Send a normal transaction, which is under the limit, and observe that it goes through.
        Address transactionTo = buildAddress(6);
        long transactionSize = dailyLimit - 1;
        Assert.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kSingleTransact));
        CallProxy.execute(walletClass, new TestingRuntime(sender, new byte[] {5,6,42}), transactionTo, transactionSize, new byte[] {1});
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kSingleTransact));
        
        // Now, send another transaction, observe that it requires multisig confirmation, and confirm it with our new owner.
        Address confirmTransactionTo = buildAddress(7);
        Assert.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kConfirmationNeeded));
        byte[] toConfirm = CallProxy.execute(walletClass, new TestingRuntime(sender, new byte[] {5,6,42}), confirmTransactionTo, transactionSize, new byte[] {1});
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kSingleTransact));
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kConfirmationNeeded));
        boolean didConfirm = CallProxy.confirm(walletClass, new TestingRuntime(newOwner, new byte[] {5,6,42}), toConfirm);
        Assert.assertTrue(didConfirm);
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kMultiTransact));
        
        // Change the count of required confirmations.
        try {
            CallProxy.changeRequirement(walletClass, new TestingRuntime(sender, new byte[] {5,6,42}), 3);
            Assert.assertTrue(false);
        } catch (InvocationTargetException e) {
            // Expected re-mapped RequireFailedException.
            Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + RequireFailedException.class.getName()).equals(e.getCause().getClass().getName()));
        }
        Assert.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kRequirementChanged));
        CallProxy.changeRequirement(walletClass, new TestingRuntime(extra1, new byte[] {5,6,42}), 3);
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kRequirementChanged));
        
        // Change the owner.
        Address lateOwner = buildAddress(8);
        Assert.assertTrue(sender.equals(CallProxy.getOwner(walletClass, new TestingRuntime(extra1, new byte[] {5,6,42}), 0)));
        try {
            CallProxy.changeOwner(walletClass, new TestingRuntime(sender, new byte[] {5,6,42}), sender, lateOwner);
            Assert.assertTrue(false);
            Assert.assertTrue(false);
        } catch (InvocationTargetException e) {
            // Expected re-mapped RequireFailedException.
            Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + RequireFailedException.class.getName()).equals(e.getCause().getClass().getName()));
        }
        try {
            CallProxy.changeOwner(walletClass, new TestingRuntime(extra1, new byte[] {5,6,42}), sender, lateOwner);
            Assert.assertTrue(false);
        } catch (InvocationTargetException e) {
            // Expected re-mapped RequireFailedException.
            Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + RequireFailedException.class.getName()).equals(e.getCause().getClass().getName()));
        }
        Assert.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kOwnerChanged));
        CallProxy.changeOwner(walletClass, new TestingRuntime(extra2, new byte[] {5,6,42}), sender, lateOwner);
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kOwnerChanged));
        
        // Try to remove an owner, but have someone revoke that so that it can't happen.
        try {
            CallProxy.removeOwner(walletClass, new TestingRuntime(lateOwner, new byte[] {5,6,42}), extra1);
            Assert.assertTrue(false);
        } catch (InvocationTargetException e) {
            // Expected re-mapped RequireFailedException.
            Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + RequireFailedException.class.getName()).equals(e.getCause().getClass().getName()));
        }
        try {
            CallProxy.removeOwner(walletClass, new TestingRuntime(extra2, new byte[] {5,6,42}), extra1);
            Assert.assertTrue(false);
        } catch (InvocationTargetException e) {
            // Expected re-mapped RequireFailedException.
            Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + RequireFailedException.class.getName()).equals(e.getCause().getClass().getName()));
        }
        Assert.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kRevoke));
        CallProxy.revoke(walletClass, new TestingRuntime(lateOwner, new byte[] {5,6,42}));
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kRevoke));
        try {
            // This fails since one of the owners revoked.
            CallProxy.removeOwner(walletClass, new TestingRuntime(extra1, new byte[] {5,6,42}), extra1);
            Assert.assertTrue(false);
        } catch (InvocationTargetException e) {
            // Expected re-mapped RequireFailedException.
            Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + RequireFailedException.class.getName()).equals(e.getCause().getClass().getName()));
        }
        Assert.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kOwnerRemoved));
        // But this succeeds when they re-agree.
        CallProxy.removeOwner(walletClass, new TestingRuntime(lateOwner, new byte[] {5,6,42}), extra1);
        Assert.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kOwnerRemoved));
        Assert.assertTrue(extra2.equals(CallProxy.getOwner(walletClass, new TestingRuntime(extra1, new byte[] {5,6,42}), 0)));
        
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


    private static class TestingRuntime implements BlockchainRuntime {
        private final Address sender;
        private final ByteArray data;
        private final Map<java.lang.String, Integer> eventCounts;
        
        public TestingRuntime(Address sender, byte[] data) {
            this.sender = sender;
            this.data = (null != data)
                    ? new ByteArray(data)
                    : null;
            this.eventCounts = new HashMap<>();
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


    private static class LoadingHelper {
        private final HierarchyTreeBuilder treeBuilder;
        private final Map<String, byte[]> inputClasses;
        
        public LoadingHelper() {
            this.treeBuilder = new HierarchyTreeBuilder();
            this.inputClasses = new HashMap<>();
        }
        
        public void loadClass(Class<?> clazz) {
            // We want to get the name of this class to load the class file.
            String className = clazz.getName();
            byte[] bytes = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
            Class<?> superClass = clazz.getSuperclass();
            // Note that superClass is null for interfaces so we just say they are under Object.
            if (null == superClass) {
                superClass = Object.class;
            }
            this.treeBuilder.addClass(className, superClass.getName(), bytes);
            this.inputClasses.put(className, bytes);
            
            // Note that, in some cases, a compiler may generate an anonymous inner class (even in cases where there is a named inner class) so search for those, automatically.
            int i = 1;
            String innerName = className + "$" + Integer.toString(i);
            InputStream stream = clazz.getClassLoader().getResourceAsStream(innerName.replaceAll("\\.", "/") + ".class");
            while (null != stream) {
                byte[] raw = null;
                try {
                    raw = stream.readAllBytes();
                    stream.close();
                } catch (IOException e) {
                    Assert.unexpected(e);
                }
                this.treeBuilder.addClass(innerName, Object.class.getName(), raw);
                this.inputClasses.put(innerName, raw);
                
                i += 1;
                innerName = className + "$" + Integer.toString(i);
                stream = clazz.getClassLoader().getResourceAsStream(innerName.replaceAll("\\.", "/") + ".class");
            }
        }
        
        public Map<String, byte[]> igetInputClasses() {
            return this.inputClasses;
        }
        
        public Forest<String, byte[]> getClassHierarchy() {
            return this.treeBuilder.asMutableForest();
        }
    }
}
