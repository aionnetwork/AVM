package org.aion.avm.core.testWallet;

import org.aion.avm.api.Address;
import org.aion.avm.core.*;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.dappreading.LoadedJar;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


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
        System.setProperty("avm-rt-jar", "./out/jar/org-aion-avm-rt.jar");

        // This is eventually just a test harness to invoke the decode() but, for now, it will actually invoke the calls, directly.
        // In order to instantiate Address objects, we need to install the IHelper.
        System.out.println("--- DIRECT ---");
        IHelper.currentContractHelper.set(new TestingHelper() {
            @Override
            public void externalBootstrapOnly() {
                // Ideally this wouldn't be called but we aren't currently worrying about it for this launcher (as we are moving away from it, anyway).
            }
        });
        invokeDirect(args);
        IHelper.currentContractHelper.remove();
        System.out.println("--- DONE (DIRECT) ---");

        System.out.println("----- ***** -----");

        // Now, try the transformed version.
        System.out.println("--- TRANSFORMED ---");
        invokeTransformed(args);
        System.out.println("--- DONE (TRANSFORMED) ---");
    }


    private static void invokeDirect(String[] args) throws Throwable {
        // Note that this loggingRuntime is just to give us a consistent interface for reading the eventCounts.
        Map<String, Integer> eventCounts = new HashMap<>();
        TestingBlockchainRuntime loggingRuntime = new TestingBlockchainRuntime().withEventCounter(eventCounts);

        // We can now init the actual contract (the Wallet is the root so init it).
        Address sender = buildAddress(1);
        Address extra1 = buildAddress(2);
        Address extra2 = buildAddress(3);
        int requiredVotes = 2;
        long dailyLimit = 5000;
        // Init the Wallet.
        DirectProxy.init((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(sender.unwrap()).withData(input).withEventCounter(eventCounts);},
                extra1, extra2, requiredVotes, dailyLimit);

        // First of all, just prove that we can send them some energy.
        Address paymentFrom = buildAddress(4);
        long paymendValue = 5;
        DirectProxy.payable((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(paymentFrom.unwrap()).withData(input).withEventCounter(eventCounts);},
                paymentFrom, paymendValue);
        RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kDeposit));

        // Try to add an owner - we need to call this twice to see the event output: sender and extra1.
        Address newOwner = buildAddress(5);
        boolean didAdd = DirectProxy.addOwner((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(sender.unwrap()).withData(input).withEventCounter(eventCounts);}, newOwner);
        RuntimeAssertionError.assertTrue(!didAdd);
        RuntimeAssertionError.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kOwnerAdded));
        didAdd = DirectProxy.addOwner((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(extra1.unwrap()).withData(input).withEventCounter(eventCounts);}, newOwner);
        RuntimeAssertionError.assertTrue(didAdd);
        RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kOwnerAdded));

        // Send a normal transaction, which is under the limit, and observe that it goes through.
        Address transactionTo = buildAddress(6);
        long transactionSize = dailyLimit - 1;
        RuntimeAssertionError.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kSingleTransact));
        DirectProxy.execute((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(sender.unwrap()).withData(input).withEventCounter(eventCounts);},
                transactionTo, transactionSize, new byte[] {1});
        RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kSingleTransact));

        // Now, send another transaction, observe that it requires multisig confirmation, and confirm it with our new owner.
        Address confirmTransactionTo = buildAddress(7);
        RuntimeAssertionError.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kConfirmationNeeded));
        byte[] toConfirm = DirectProxy.execute((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(sender.unwrap()).withData(input).withEventCounter(eventCounts);},
                confirmTransactionTo, transactionSize, new byte[] {1});
        RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kSingleTransact));
        RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kConfirmationNeeded));
        boolean didConfirm = DirectProxy.confirm((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(newOwner.unwrap()).withData(input).withEventCounter(eventCounts);},
                toConfirm);
        RuntimeAssertionError.assertTrue(didConfirm);
        RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kMultiTransact));

        // Change the count of required confirmations.
        boolean didChange = DirectProxy.changeRequirement((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(sender.unwrap()).withData(input).withEventCounter(eventCounts);}, 3);
        RuntimeAssertionError.assertTrue(!didChange);
        RuntimeAssertionError.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kRequirementChanged));
        didChange = DirectProxy.changeRequirement((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(extra1.unwrap()).withData(input).withEventCounter(eventCounts);}, 3);
        RuntimeAssertionError.assertTrue(didChange);
        RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kRequirementChanged));

        // Change the owner.
        Address lateOwner = buildAddress(8);
        RuntimeAssertionError.assertTrue(sender.equals(DirectProxy.getOwner((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(lateOwner.unwrap()).withData(input).withEventCounter(eventCounts);},
                0)));
        didChange = DirectProxy.changeOwner((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(sender.unwrap()).withData(input).withEventCounter(eventCounts);}, sender, lateOwner);
        RuntimeAssertionError.assertTrue(!didChange);
        didChange = DirectProxy.changeOwner((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(extra1.unwrap()).withData(input).withEventCounter(eventCounts);}, sender, lateOwner);
        RuntimeAssertionError.assertTrue(!didChange);
        RuntimeAssertionError.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kOwnerChanged));
        didChange = DirectProxy.changeOwner((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(extra2.unwrap()).withData(input).withEventCounter(eventCounts);}, sender, lateOwner);
        RuntimeAssertionError.assertTrue(didChange);
        RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kOwnerChanged));

        // Try to remove an owner, but have someone revoke that so that it can't happen.
        boolean didRemove = DirectProxy.removeOwner((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(lateOwner.unwrap()).withData(input).withEventCounter(eventCounts);}, extra1);
        RuntimeAssertionError.assertTrue(!didRemove);
        didRemove = DirectProxy.removeOwner((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(extra2.unwrap()).withData(input).withEventCounter(eventCounts);}, extra1);
        RuntimeAssertionError.assertTrue(!didRemove);
        RuntimeAssertionError.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kRevoke));
        DirectProxy.revoke((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(lateOwner.unwrap()).withData(input).withEventCounter(eventCounts);},
                CallEncoder.removeOwner(extra1)
        );
        RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kRevoke));
        // This fails since one of the owners revoked.
        didRemove = DirectProxy.removeOwner((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(extra1.unwrap()).withData(input).withEventCounter(eventCounts);}, extra1);
        RuntimeAssertionError.assertTrue(!didRemove);
        RuntimeAssertionError.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kOwnerRemoved));
        // But this succeeds when they re-agree.
        didRemove = DirectProxy.removeOwner((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(lateOwner.unwrap()).withData(input).withEventCounter(eventCounts);}, extra1);
        RuntimeAssertionError.assertTrue(didRemove);
        RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kOwnerRemoved));
        RuntimeAssertionError.assertTrue(extra2.equals(DirectProxy.getOwner((input) -> {Helper.blockchainRuntime = new TestingBlockchainRuntime().withCaller(extra1.unwrap()).withData(input).withEventCounter(eventCounts);},
                0)));

        // We should have seen 13 confirmations over the course of the test run.
        RuntimeAssertionError.assertTrue(13 == loggingRuntime.getEventCount(EventLogger.kConfirmation));
    }

    private static void invokeTransformed(String[] args) throws Throwable {
        Map<String, Integer> eventCounts = new HashMap<>();

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
        );
        LoadedJar jar = LoadedJar.fromBytes(jarBytes);

        Map<String, byte[]> transformedClasses = Helpers.mapIncludingHelperBytecode(DAppCreator.transformClasses(jar.classBytesByQualifiedNames, ClassHierarchyForest.createForestFrom(jar)));

        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(transformedClasses);

        // (note that setting a single runtime instance for this group of invocations doesn't really make sense - it just provides the energy counter).
        Helpers.instantiateHelper(loader, 1_000_000L, 1);
        // Note that this single externalRuntime instance doesn't really make sense - it is only useful in the cases where we aren't using
        // it for invocation context, just environment (energy counter, event logging, etc).
        TestingBlockchainRuntime externalRuntime = new TestingBlockchainRuntime().withEventCounter(eventCounts);

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
        CallProxy.init((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(sender.unwrap()).withData(input).withEventCounter(eventCounts));},
                classProvider, extra1, extra2, requiredVotes, dailyLimit);

        // First of all, just prove that we can send them some energy.
        Address paymentFrom = buildAddress(4);
        long paymentValue = 5;
        CallProxy.payable((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(paymentFrom.unwrap()).withData(input).withEventCounter(eventCounts));},
                classProvider, paymentFrom, paymentValue);
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kDeposit));

        // Try to add an owner - we need to call this twice to see the event output: sender and extra1.
        Address newOwner = buildAddress(5);
        boolean didAdd = CallProxy.addOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(sender.unwrap()).withData(input).withEventCounter(eventCounts));}, classProvider, newOwner);
        RuntimeAssertionError.assertTrue(!didAdd);
        RuntimeAssertionError.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kOwnerAdded));
        didAdd = CallProxy.addOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(extra1.unwrap()).withData(input).withEventCounter(eventCounts));}, classProvider, newOwner);
        RuntimeAssertionError.assertTrue(didAdd);
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kOwnerAdded));

        // Send a normal transaction, which is under the limit, and observe that it goes through.
        Address transactionTo = buildAddress(6);
        long transactionSize = dailyLimit - 1;
        RuntimeAssertionError.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kSingleTransact));
        CallProxy.execute((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(sender.unwrap()).withData(input).withEventCounter(eventCounts));},
                classProvider, transactionTo, transactionSize, new byte[] {1});
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kSingleTransact));

        // Now, send another transaction, observe that it requires multisig confirmation, and confirm it with our new owner.
        Address confirmTransactionTo = buildAddress(7);
        RuntimeAssertionError.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kConfirmationNeeded));
        byte[] toConfirm = CallProxy.execute((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(sender.unwrap()).withData(input).withEventCounter(eventCounts));},
                classProvider, confirmTransactionTo, transactionSize, new byte[] {1});
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kSingleTransact));
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kConfirmationNeeded));
        boolean didConfirm = CallProxy.confirm((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(newOwner.unwrap()).withData(input).withEventCounter(eventCounts));},
                classProvider, toConfirm);
        RuntimeAssertionError.assertTrue(didConfirm);
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kMultiTransact));

        // Change the count of required confirmations.
        boolean didChange = CallProxy.changeRequirement((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(sender.unwrap()).withData(input).withEventCounter(eventCounts));}, classProvider, 3);
        RuntimeAssertionError.assertTrue(!didChange);
        RuntimeAssertionError.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kRequirementChanged));
        didChange = CallProxy.changeRequirement((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(extra1.unwrap()).withData(input).withEventCounter(eventCounts));}, classProvider, 3);
        RuntimeAssertionError.assertTrue(didChange);
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kRequirementChanged));

        // Change the owner.
        Address lateOwner = buildAddress(8);
        RuntimeAssertionError.assertTrue(sender.equals(CallProxy.getOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(extra1.unwrap()).withData(input).withEventCounter(eventCounts));},
                classProvider, 0)));
        didChange = CallProxy.changeOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(sender.unwrap()).withData(input).withEventCounter(eventCounts));}, classProvider, sender, lateOwner);
        RuntimeAssertionError.assertTrue(!didChange);
        didChange = CallProxy.changeOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(extra1.unwrap()).withData(input).withEventCounter(eventCounts));}, classProvider, sender, lateOwner);
        RuntimeAssertionError.assertTrue(!didChange);
        RuntimeAssertionError.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kOwnerChanged));
        didChange = CallProxy.changeOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(extra2.unwrap()).withData(input).withEventCounter(eventCounts));}, classProvider, sender, lateOwner);
        RuntimeAssertionError.assertTrue(didChange);
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kOwnerChanged));
        
        // Try to remove an owner, but have someone revoke that so that it can't happen.
        boolean didRemove = CallProxy.removeOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(lateOwner.unwrap()).withData(input).withEventCounter(eventCounts));}, classProvider, extra1);
        RuntimeAssertionError.assertTrue(!didRemove);
        didRemove = CallProxy.removeOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(extra2.unwrap()).withData(input).withEventCounter(eventCounts));}, classProvider, extra1);
        RuntimeAssertionError.assertTrue(!didRemove);
        RuntimeAssertionError.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kRevoke));
        CallProxy.revoke((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(lateOwner.unwrap()).withData(input).withEventCounter(eventCounts));},
                classProvider, CallEncoder.removeOwner(extra1));
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kRevoke));
        // This fails since one of the owners revoked.
        didRemove = CallProxy.removeOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(extra1.unwrap()).withData(input).withEventCounter(eventCounts));}, classProvider, extra1);
        RuntimeAssertionError.assertTrue(!didRemove);
        RuntimeAssertionError.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kOwnerRemoved));
        // But this succeeds when they re-agree.
        didRemove = CallProxy.removeOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(lateOwner.unwrap()).withData(input).withEventCounter(eventCounts));}, classProvider, extra1);
        RuntimeAssertionError.assertTrue(didRemove);
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kOwnerRemoved));
        RuntimeAssertionError.assertTrue(extra2.equals(CallProxy.getOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime().withCaller(extra1.unwrap()).withData(input).withEventCounter(eventCounts));},
                classProvider, 0)));
        
        // We should have seen 13 confirmations over the course of the test run.
        RuntimeAssertionError.assertTrue(13 == externalRuntime.getEventCount(EventLogger.kConfirmation));
    }

    private static Address buildAddress(int fillByte) {
        byte[] raw = new byte[32];
        for (int i = 0; i < raw.length; ++ i) {
            raw[i] = (byte)fillByte;
        }
        return new Address(raw);
    }
}
