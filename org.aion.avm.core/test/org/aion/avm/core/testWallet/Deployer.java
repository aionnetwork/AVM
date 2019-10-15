package org.aion.avm.core.testWallet;

import avm.Address;
import java.util.HashSet;
import java.util.Set;
import org.aion.types.AionAddress;
import org.aion.avm.NameStyle;
import org.aion.avm.core.ClassHierarchyForest;
import org.aion.avm.core.ClassRenamer;
import org.aion.avm.core.ClassRenamerBuilder;
import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.DAppCreator;
import org.aion.avm.core.IExternalCapabilities;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.blockchainruntime.TestingBlockchainRuntime;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.core.dappreading.LoadedJar;
import org.aion.avm.core.miscvisitors.ClassRenameVisitor;
import org.aion.avm.core.miscvisitors.SingleLoader;
import org.aion.avm.core.types.ClassInformation;
import org.aion.avm.core.types.ClassHierarchy;
import org.aion.avm.core.types.ClassInformationFactory;
import org.aion.avm.core.types.ClassHierarchyBuilder;
import org.aion.avm.core.types.CommonType;
import org.aion.avm.core.util.BlockchainRuntime;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.TestingHelper;
import org.aion.avm.utilities.Utilities;

import i.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
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
    private static final IExternalCapabilities CAPABILITIES = new EmptyCapabilities() {
        @Override
        public byte[] blake2b(byte[] data) {
            // NOTE:  This test relies on calling blake2b but doesn't rely on the answer being correct so just return the input.
            return data;
        }
    };

    static Map<String, Integer> eventCounts = new HashMap<>();

    private static boolean preserveDebuggability = false;

    public static void main(String[] args) throws Throwable {
        // We run this test 2 ways:
        // (1) Invoke the logic directly, as though this were a normal Java program.
        // (2) Transform the code and call it, as though it were run inside the AVM.
        callableInvokeDirect();
        callableInvokeTransformed();
    }

    /**
     * Pulled out so it can be called from JUnit.
     */
    public static void callableInvokeDirect() throws Throwable {
        TestingHelper.runUnderBoostrapHelper(() -> invokeDirect());
    }

    /**
     * Pulled out so it can be called from JUnit.
     */
    public static void callableInvokeTransformed() throws Throwable {
        invokeTransformed();
    }


    private static void invokeDirect() {

        // Note that this loggingRuntime is just to give us a consistent interface for reading the eventCounts.
        TestingBlockchainRuntime loggingRuntime = new TestingBlockchainRuntime(CAPABILITIES).withEventCounter(eventCounts);

        // We can now init the actual contract (the Wallet is the root so init it).
        AionAddress sender = Helpers.address(1);
        AionAddress extra1 = Helpers.address(2);
        AionAddress extra2 = Helpers.address(3);
        Address senderApi = new Address(sender.toByteArray());
        Address extra1Api = new Address(extra1.toByteArray());
        Address extra2Api = new Address(extra2.toByteArray());
        int requiredVotes = 2;
        long dailyLimit = 5000;

        int PARSING_OPTIONS = ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG;
        int WRITING_OPTIONS = ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS;

        String targetTestName = BlockchainRuntime.class.getName();
        byte[] targetTestBytes = Utilities.loadRequiredResourceAsBytes(Utilities.fulllyQualifiedNameToInternalName(targetTestName) + ".class");

        String newName = "avm/Blockchain";
        byte[] renamedBytes = new ClassToolchain.Builder(targetTestBytes, PARSING_OPTIONS)
                .addNextVisitor(new ClassRenameVisitor(newName))
                .addWriter(new ClassWriter(WRITING_OPTIONS))
                .build()
                .runAndGetBytecode();
        try {
            SingleLoader loader = new SingleLoader("", new byte[0]);
            Class<?> blockchainRuntime = loader.loadClassFromByteCode("avm.Blockchain", renamedBytes);
            Class<?> directProxy = loader.loadClassFromByteCode(DirectProxy.class.getName(), Utilities.loadRequiredResourceAsBytes(Utilities.fulllyQualifiedNameToInternalName(DirectProxy.class.getName()) + ".class"));
            Class<?> walletShim = loader.loadClassFromByteCode(WalletShim.class.getName(), Utilities.loadRequiredResourceAsBytes(Utilities.fulllyQualifiedNameToInternalName(WalletShim.class.getName()) + ".class"));
            Class<?> multiOwned = loader.loadClassFromByteCode(Multiowned.class.getName(), Utilities.loadRequiredResourceAsBytes(Utilities.fulllyQualifiedNameToInternalName(Multiowned.class.getName()) + ".class"));
            Class<?> wallet = loader.loadClassFromByteCode(Wallet.class.getName(), Utilities.loadRequiredResourceAsBytes(Utilities.fulllyQualifiedNameToInternalName(Wallet.class.getName()) + ".class"));
            Class<?> tx = loader.loadClassFromByteCode(Wallet.class.getName() + "$Transaction", Utilities.loadRequiredResourceAsBytes(Utilities.fulllyQualifiedNameToInternalName(Wallet.class.getName() + "$Transaction") + ".class"));
            Class<?> eventLogger = loader.loadClassFromByteCode(EventLogger.class.getName(), Utilities.loadRequiredResourceAsBytes(Utilities.fulllyQualifiedNameToInternalName(EventLogger.class.getName()) + ".class"));
            Class<?> operation = loader.loadClassFromByteCode(Operation.class.getName(), Utilities.loadRequiredResourceAsBytes(Utilities.fulllyQualifiedNameToInternalName(Operation.class.getName()) + ".class"));
            Class<?> dayLimit = loader.loadClassFromByteCode(Daylimit.class.getName(), Utilities.loadRequiredResourceAsBytes(Utilities.fulllyQualifiedNameToInternalName(Daylimit.class.getName()) + ".class"));

            // Init the Wallet.
            directProxy.getDeclaredMethod("init", java.util.function.Consumer.class, Address.class, Address.class, int.class, long.class).invoke(null, getBlockchainRuntime(blockchainRuntime, sender), extra1Api, extra2Api, requiredVotes, dailyLimit);
            // First of all, just prove that we can send them some energy.
            AionAddress paymentFrom = Helpers.address(4);
            long paymendValue = 5;

            directProxy.getDeclaredMethod("payable", Consumer.class, Address.class, long.class).invoke(null, getBlockchainRuntime(blockchainRuntime, sender), new Address(paymentFrom.toByteArray()), paymendValue);
            RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kDeposit));

            // Try to add an owner - we need to call this twice to see the event output: sender and extra1.
            AionAddress newOwner = Helpers.address(5);
            boolean didAdd = (boolean) directProxy.getDeclaredMethod("addOwner", Consumer.class, Address.class).invoke(null, getBlockchainRuntime(blockchainRuntime, sender), new Address(newOwner.toByteArray()));
            RuntimeAssertionError.assertTrue(!didAdd);
            RuntimeAssertionError.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kOwnerAdded));

            didAdd = (boolean) directProxy.getDeclaredMethod("addOwner", Consumer.class, Address.class).invoke(null, getBlockchainRuntime(blockchainRuntime, extra1), new Address(newOwner.toByteArray()));
            RuntimeAssertionError.assertTrue(didAdd);
            RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kOwnerAdded));


            // Send a normal transaction, which is under the limit, and observe that it goes through.
            AionAddress transactionTo = Helpers.address(6);
            long transactionSize = dailyLimit - 1;
            RuntimeAssertionError.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kSingleTransact));

            directProxy.getDeclaredMethod("execute", Consumer.class, Address.class, long.class, byte[].class).invoke(null, getBlockchainRuntime(blockchainRuntime, sender), new Address(transactionTo.toByteArray()), transactionSize, new byte[]{1});
            RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kSingleTransact));


            // Now, send another transaction, observe that it requires multisig confirmation, and confirm it with our new owner.
            AionAddress confirmTransactionTo = Helpers.address(7);
            RuntimeAssertionError.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kConfirmationNeeded));

            byte[] toConfirm = (byte[]) directProxy.getDeclaredMethod("execute", Consumer.class, Address.class, long.class, byte[].class).invoke(null, getBlockchainRuntime(blockchainRuntime, sender), new Address(confirmTransactionTo.toByteArray()), transactionSize, new byte[]{1});
            RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kSingleTransact));
            RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kConfirmationNeeded));

            boolean didConfirm = (boolean) directProxy.getDeclaredMethod("confirm", Consumer.class, byte[].class).invoke(null, getBlockchainRuntime(blockchainRuntime, newOwner), toConfirm);
            RuntimeAssertionError.assertTrue(didConfirm);
            RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kMultiTransact));

            // Change the count of required confirmations.
            boolean didChange = (boolean) directProxy.getDeclaredMethod("changeRequirement", Consumer.class, int.class).invoke(null, getBlockchainRuntime(blockchainRuntime, sender), 3);
            RuntimeAssertionError.assertTrue(!didChange);
            RuntimeAssertionError.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kRequirementChanged));

            didChange = (boolean) directProxy.getDeclaredMethod("changeRequirement", Consumer.class, int.class).invoke(null, getBlockchainRuntime(blockchainRuntime, extra1), 3);
            RuntimeAssertionError.assertTrue(didChange);
            RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kRequirementChanged));


            // Change the owner.
            AionAddress lateOwner = Helpers.address(8);
            Address lateOwnerApi = new Address(lateOwner.toByteArray());
            RuntimeAssertionError.assertTrue(senderApi.equals(directProxy.getDeclaredMethod("getOwner", Consumer.class, int.class).invoke(null, getBlockchainRuntime(blockchainRuntime, lateOwner), 0)));
            didChange = (boolean) directProxy.getDeclaredMethod("changeOwner", Consumer.class, Address.class, Address.class).invoke(null, getBlockchainRuntime(blockchainRuntime, sender), senderApi, lateOwnerApi);
            RuntimeAssertionError.assertTrue(!didChange);
            didChange = (boolean) directProxy.getDeclaredMethod("changeOwner", Consumer.class, Address.class, Address.class).invoke(null, getBlockchainRuntime(blockchainRuntime, extra1), senderApi, lateOwnerApi);
            RuntimeAssertionError.assertTrue(!didChange);
            RuntimeAssertionError.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kOwnerChanged));
            didChange = (boolean) directProxy.getDeclaredMethod("changeOwner", Consumer.class, Address.class, Address.class).invoke(null, getBlockchainRuntime(blockchainRuntime, extra2), senderApi, lateOwnerApi);
            RuntimeAssertionError.assertTrue(didChange);
            RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kOwnerChanged));


            // Try to remove an owner, but have someone revoke that so that it can't happen.
            boolean didRemove = (boolean) directProxy.getDeclaredMethod("removeOwner", Consumer.class, Address.class).invoke(null, getBlockchainRuntime(blockchainRuntime, lateOwner), extra1Api);
            RuntimeAssertionError.assertTrue(!didRemove);
            didRemove = (boolean) directProxy.getDeclaredMethod("removeOwner", Consumer.class, Address.class).invoke(null, getBlockchainRuntime(blockchainRuntime, extra2), extra1Api);
            RuntimeAssertionError.assertTrue(!didRemove);
            RuntimeAssertionError.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kRevoke));
            directProxy.getDeclaredMethod("revoke", Consumer.class, byte[].class).invoke(null, getBlockchainRuntime(blockchainRuntime, lateOwner),
                    CallEncoder.removeOwner(extra1Api)
            );
            RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kRevoke));
            // This fails since one of the owners revoked.
            didRemove = (boolean) directProxy.getDeclaredMethod("removeOwner", Consumer.class, Address.class).invoke(null, getBlockchainRuntime(blockchainRuntime, extra1), extra1Api);
            RuntimeAssertionError.assertTrue(!didRemove);
            RuntimeAssertionError.assertTrue(0 == loggingRuntime.getEventCount(EventLogger.kOwnerRemoved));
            // But this succeeds when they re-agree.
            didRemove = (boolean) directProxy.getDeclaredMethod("removeOwner", Consumer.class, Address.class).invoke(null, getBlockchainRuntime(blockchainRuntime, lateOwner), extra1Api);
            RuntimeAssertionError.assertTrue(didRemove);
            RuntimeAssertionError.assertTrue(1 == loggingRuntime.getEventCount(EventLogger.kOwnerRemoved));
            RuntimeAssertionError.assertTrue(extra2Api.equals(directProxy.getDeclaredMethod("getOwner", Consumer.class, int.class).invoke(null, getBlockchainRuntime(blockchainRuntime, extra1),
                    0)));

            // We should have seen 13 confirmations over the course of the test run.
            RuntimeAssertionError.assertTrue(13 == loggingRuntime.getEventCount(EventLogger.kConfirmation));

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

    }

    private static void invokeTransformed() throws Throwable {
        Map<String, Integer> eventCounts = new HashMap<>();

        byte[] jarBytes = UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(Wallet.class
                , Multiowned.class
                , ByteArrayWrapper.class
                , Operation.class
                , ByteArrayHelpers.class
                , BytesKey.class
                , RequireFailedException.class
                , Daylimit.class
                , EventLogger.class
        );
        LoadedJar jar = LoadedJar.fromBytes(jarBytes);

        ClassHierarchy classHierarchy = buildNewHierarchy(jar);

        Set<String> jclExceptions = new HashSet<>();
        for (CommonType type : CommonType.values()) {
            if (type.isShadowException) {
                jclExceptions.add(type.dotName);
            }
        }

        ClassRenamer classRenamer = new ClassRenamerBuilder(NameStyle.DOT_NAME, preserveDebuggability)
            .loadPreRenameUserDefinedClasses(classHierarchy.getPreRenameUserDefinedClassesAndInterfaces())
            .loadPostRenameJclExceptionClasses(jclExceptions)
            .build();

        Map<String, byte[]> transformedClasses = Helpers.mapIncludingHelperBytecode(DAppCreator.transformClasses(jar.classBytesByQualifiedNames, ClassHierarchyForest.createForestFrom(jar), classHierarchy, classRenamer,
            preserveDebuggability), Helpers.loadDefaultHelperBytecode());

        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(transformedClasses);

        // (note that setting a single runtime instance for this group of invocations doesn't really make sense - it just provides the energy counter).
        CommonInstrumentation instrumentation = new CommonInstrumentation();
        InstrumentationHelpers.attachThread(instrumentation);
        IRuntimeSetup runtimeSetup = Helpers.getSetupForLoader(loader);
        InstrumentationHelpers.pushNewStackFrame(runtimeSetup, loader, 10_000_000L, 1, new InternedClasses());
        // Note that this single externalRuntime instance doesn't really make sense - it is only useful in the cases where we aren't using
        // it for invocation context, just environment (energy counter, event logging, etc).
        TestingBlockchainRuntime externalRuntime = new TestingBlockchainRuntime(CAPABILITIES).withEventCounter(eventCounts);

        // issue-112:  We create this classProvider to make it easier to emulate a full reload of a DApp.
        // The idea is that we can reload a fresh Wallet class from a new AvmClassLoader for each invocation into the DApp in order to simulate
        // the DApp state at the point where it receives a call.
        // (currently, we just return the same walletClass instance since our persistence design is still being prototyped).
        Class<?> walletClass = loader.loadUserClassByOriginalName(Wallet.class.getName(), preserveDebuggability);
        Supplier<Class<?>> classProvider = () -> {
            return walletClass;
        };

        // Now, run the test.
        AionAddress sender = Helpers.address(1);
        AionAddress extra1 = Helpers.address(2);
        AionAddress extra2 = Helpers.address(3);
        Address senderApi = new Address(sender.toByteArray());
        Address extra2Api = new Address(extra2.toByteArray());

        // We can now init the actual contract (the Wallet is the root so init it).
        int requiredVotes = 2;
        long dailyLimit = 5000;
        CallProxy.init((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(sender.toByteArray()).withData(input).withEventCounter(eventCounts));},
                classProvider, new Address(extra1.toByteArray()), new Address(extra2.toByteArray()), requiredVotes, dailyLimit);

        // First of all, just prove that we can send them some energy.
        AionAddress paymentFrom = Helpers.address(4);
        long paymentValue = 5;
        CallProxy.payable((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(paymentFrom.toByteArray()).withData(input).withEventCounter(eventCounts));},
                classProvider, new Address(paymentFrom.toByteArray()), paymentValue);
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kDeposit));

        // Try to add an owner - we need to call this twice to see the event output: sender and extra1.
        AionAddress newOwner = Helpers.address(5);
        boolean didAdd = CallProxy.addOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(sender.toByteArray()).withData(input).withEventCounter(eventCounts));}, classProvider, new Address(newOwner.toByteArray()));
        RuntimeAssertionError.assertTrue(!didAdd);
        RuntimeAssertionError.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kOwnerAdded));
        didAdd = CallProxy.addOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(extra1.toByteArray()).withData(input).withEventCounter(eventCounts));}, classProvider, new Address(newOwner.toByteArray()));
        RuntimeAssertionError.assertTrue(didAdd);
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kOwnerAdded));

        // Send a normal transaction, which is under the limit, and observe that it goes through.
        AionAddress transactionTo = Helpers.address(6);
        long transactionSize = dailyLimit - 1;
        RuntimeAssertionError.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kSingleTransact));
        CallProxy.execute((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(sender.toByteArray()).withData(input).withEventCounter(eventCounts));},
                classProvider, new Address(transactionTo.toByteArray()), transactionSize, new byte[] {1});
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kSingleTransact));

        // Now, send another transaction, observe that it requires multisig confirmation, and confirm it with our new owner.
        AionAddress confirmTransactionTo = Helpers.address(7);
        RuntimeAssertionError.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kConfirmationNeeded));
        byte[] toConfirm = CallProxy.execute((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(sender.toByteArray()).withData(input).withEventCounter(eventCounts));},
                classProvider, new Address(confirmTransactionTo.toByteArray()), transactionSize, new byte[] {1});
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kSingleTransact));
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kConfirmationNeeded));
        boolean didConfirm = CallProxy.confirm((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(newOwner.toByteArray()).withData(input).withEventCounter(eventCounts));},
                classProvider, toConfirm);
        RuntimeAssertionError.assertTrue(didConfirm);
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kMultiTransact));

        // Change the count of required confirmations.
        boolean didChange = CallProxy.changeRequirement((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(sender.toByteArray()).withData(input).withEventCounter(eventCounts));}, classProvider, 3);
        RuntimeAssertionError.assertTrue(!didChange);
        RuntimeAssertionError.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kRequirementChanged));
        didChange = CallProxy.changeRequirement((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(extra1.toByteArray()).withData(input).withEventCounter(eventCounts));}, classProvider, 3);
        RuntimeAssertionError.assertTrue(didChange);
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kRequirementChanged));

        // Change the owner.
        AionAddress lateOwner = Helpers.address(8);
        RuntimeAssertionError.assertTrue(senderApi.equals(CallProxy.getOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(extra1.toByteArray()).withData(input).withEventCounter(eventCounts));},
                classProvider, 0)));
        didChange = CallProxy.changeOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(sender.toByteArray()).withData(input).withEventCounter(eventCounts));}, classProvider, new Address(sender.toByteArray()), new Address(lateOwner.toByteArray()));
        RuntimeAssertionError.assertTrue(!didChange);
        didChange = CallProxy.changeOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(extra1.toByteArray()).withData(input).withEventCounter(eventCounts));}, classProvider, new Address(sender.toByteArray()), new Address(lateOwner.toByteArray()));
        RuntimeAssertionError.assertTrue(!didChange);
        RuntimeAssertionError.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kOwnerChanged));
        didChange = CallProxy.changeOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(extra2.toByteArray()).withData(input).withEventCounter(eventCounts));}, classProvider, new Address(sender.toByteArray()), new Address(lateOwner.toByteArray()));
        RuntimeAssertionError.assertTrue(didChange);
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kOwnerChanged));
        
        // Try to remove an owner, but have someone revoke that so that it can't happen.
        boolean didRemove = CallProxy.removeOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(lateOwner.toByteArray()).withData(input).withEventCounter(eventCounts));}, classProvider, new Address(extra1.toByteArray()));
        RuntimeAssertionError.assertTrue(!didRemove);
        didRemove = CallProxy.removeOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(extra2.toByteArray()).withData(input).withEventCounter(eventCounts));}, classProvider, new Address(extra1.toByteArray()));
        RuntimeAssertionError.assertTrue(!didRemove);
        RuntimeAssertionError.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kRevoke));
        CallProxy.revoke((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(lateOwner.toByteArray()).withData(input).withEventCounter(eventCounts));},
                classProvider, CallEncoder.removeOwner(new Address(extra1.toByteArray())));
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kRevoke));
        // This fails since one of the owners revoked.
        didRemove = CallProxy.removeOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(extra1.toByteArray()).withData(input).withEventCounter(eventCounts));}, classProvider, new Address(extra1.toByteArray()));
        RuntimeAssertionError.assertTrue(!didRemove);
        RuntimeAssertionError.assertTrue(0 == externalRuntime.getEventCount(EventLogger.kOwnerRemoved));
        // But this succeeds when they re-agree.
        didRemove = CallProxy.removeOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(lateOwner.toByteArray()).withData(input).withEventCounter(eventCounts));}, classProvider, new Address(extra1.toByteArray()));
        RuntimeAssertionError.assertTrue(didRemove);
        RuntimeAssertionError.assertTrue(1 == externalRuntime.getEventCount(EventLogger.kOwnerRemoved));
        RuntimeAssertionError.assertTrue(extra2Api.equals(CallProxy.getOwner((input) -> {Helpers.attachBlockchainRuntime(loader, new TestingBlockchainRuntime(CAPABILITIES).withCaller(extra1.toByteArray()).withData(input).withEventCounter(eventCounts));},
                classProvider, 0)));
        
        // We should have seen 13 confirmations over the course of the test run.
        RuntimeAssertionError.assertTrue(13 == externalRuntime.getEventCount(EventLogger.kConfirmation));
        
        InstrumentationHelpers.popExistingStackFrame(runtimeSetup);
        InstrumentationHelpers.detachThread(instrumentation);
    }

    private static ClassHierarchy buildNewHierarchy(LoadedJar jar) {
        ClassInformationFactory classInfoFactory = new ClassInformationFactory();
        Set<ClassInformation> classInfos = classInfoFactory.fromUserDefinedPreRenameJar(jar);

        ClassRenamer classRenamer = new ClassRenamerBuilder(NameStyle.DOT_NAME, preserveDebuggability)
            .loadPreRenameUserDefinedClasses(extractClassNames(classInfos))
            .loadPostRenameJclExceptionClasses(fetchPostRenameJclExceptions())
            .build();

        return new ClassHierarchyBuilder()
            .addShadowJcl()
            .addPreRenameUserDefinedClasses(classRenamer, classInfos)
            .addHandwrittenArrayWrappers()
            .addPostRenameJclExceptions()
            .build();
    }

    private static Consumer<byte[]> getBlockchainRuntime(Class<?> blockchainRuntime, AionAddress sender) {
        return (input) -> {
            try {
                blockchainRuntime.getField("blockchainRuntime").set(blockchainRuntime, new TestingBlockchainRuntime(CAPABILITIES).withCaller(sender.toByteArray()).withData(input).withEventCounter(eventCounts));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        };

    }

    private static Set<String> extractClassNames(Set<ClassInformation> classInformations) {
        Set<String> classNames = new HashSet<>();
        for (ClassInformation classInformation : classInformations) {
            classNames.add(classInformation.dotName);
        }
        return classNames;
    }

    private static Set<String> fetchPostRenameJclExceptions() {
        Set<String> exceptions = new HashSet<>();
        for (CommonType type : CommonType.values()) {
            if (type.isShadowException) {
                exceptions.add(type.dotName);
            }
        }
        return exceptions;
    }
}
