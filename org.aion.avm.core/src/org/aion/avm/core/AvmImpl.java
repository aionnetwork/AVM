package org.aion.avm.core;

import org.aion.avm.api.*;
import org.aion.avm.arraywrapper.*;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapter;
import org.aion.avm.core.arraywrapping.ArrayWrappingClassAdapterRef;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.exceptionwrapping.ExceptionWrapping;
import org.aion.avm.core.instrument.BytecodeFeeScheduler;
import org.aion.avm.core.instrument.ClassMetering;
import org.aion.avm.core.instrument.HeapMemoryCostCalculator;
import org.aion.avm.core.miscvisitors.ClinitStrippingVisitor;
import org.aion.avm.core.miscvisitors.ConstantVisitor;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.persistence.AutomaticGraphVisitor;
import org.aion.avm.core.persistence.ContractEnvironmentState;
import org.aion.avm.core.persistence.LoadedDApp;
import org.aion.avm.core.rejection.RejectionClassVisitor;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.core.shadowing.InvokedynamicShadower;
import org.aion.avm.core.stacktracking.StackWatcherClassAdapter;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.*;
import org.aion.kernel.Block;
import org.aion.kernel.InternalTransaction;
import org.aion.kernel.Log;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionResult;
import org.aion.kernel.DappCode;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;


public class AvmImpl implements Avm {
    private static final String HELPER_CLASS = PackageConstants.kInternalSlashPrefix + "Helper";

    private static final DappCode.CodeVersion VERSION = DappCode.CodeVersion.VERSION_1_0;

    /**
     * Validates all classes, including but not limited to:
     *
     * <ul>
     * <li>class format (hash, version, etc.)</li>
     * <li>no native method</li>
     * <li>no invalid opcode</li>
     * <li>package name does not start with <code>org.aion.avm</code></li>
     * <li>no access to any <code>org.aion.avm</code> packages but the <code>org.aion.avm.api</code> package</li>
     * <li>any assumptions that the class transformation has made</li>
     * <li>TODO: add more</li>
     * </ul>
     *
     * @param dapp the classes of DApp
     * @return true if the DApp is valid, otherwise false
     */
    public boolean validateDapp(RawDappModule dapp) {

        // TODO: Rom, complete module validation

        return true;
    }

    /**
     * Computes the object size of shadow java.base classes
     *
     * @return a mapping between class name and object size
     *
     * Class name is in the JVM internal name format, see {@link org.aion.avm.core.util.Helpers#fulllyQualifiedNameToInternalName(String)}
     */
    public static Map<String, Integer> computeShadowObjectSizes() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("java/lang/Object", 4);
        map.put("java/lang/Class", 4);
        map.put("java/lang/Math", 4);
        map.put("java/lang/String", 4);
        map.put("java/lang/Enum", 4);
        map.put("java/lang/StringBuffer", 4);
        map.put("java/lang/StringBuilder", 4);

        // TODO (issue-79):  Implement the rest of these by walking the runtime, elsewhere (this "4" is probably not right, in most cases).
        map.put("java/lang/AssertionError", 4);
        map.put("java/lang/Throwable", 4);
        map.put("java/lang/Exception", 4);
        map.put("java/lang/RuntimeException", 4);
        map.put("java/lang/NullPointerException", 4);
        map.put("java/lang/IllegalArgumentException", 4);
        map.put("java/math/MathContext", 4);

        return Collections.unmodifiableMap(map);
    }

    /**
     * Computes the object size of API classes.
     *
     * @return
     */
    public static Map<String, Integer> computeApiObjectSizes() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("org/aion/avm/api/Address", 4);
        map.put("org/aion/avm/api/InvalidTxDataException", 4);

        return Collections.unmodifiableMap(map);
    }

    /**
     * Returns the sizes of all the classes, including the runtime ones and the DApp ones.
     *
     * @param classHierarchy     the class hierarchy
     * @param shadowObjectSizes  the object size of shadow java.base classes
     * @param apiObjectSizes     the object size of API classes
     * @return The size of user objects
     * Class name is in the JVM internal name format, see {@link org.aion.avm.core.util.Helpers#fulllyQualifiedNameToInternalName(String)}
     */
    public static Map<String, Integer> computeUserObjectSizes(Forest<String, byte[]> classHierarchy,
                                                             Map<String, Integer> shadowObjectSizes,
                                                             Map<String, Integer> apiObjectSizes)
    {
        HeapMemoryCostCalculator objectSizeCalculator = new HeapMemoryCostCalculator();

        // copy over the runtime classes sizes
        HashMap<String, Integer> runtimeObjectSizes = new HashMap<>();
        runtimeObjectSizes.putAll(shadowObjectSizes);
        runtimeObjectSizes.putAll(apiObjectSizes);

        // compute the object size of every one in 'classes'
        objectSizeCalculator.calcClassesInstanceSize(classHierarchy, runtimeObjectSizes);


        Map<String, Integer> userObjectSizes = new HashMap<>();
        objectSizeCalculator.getClassHeapSizeMap().forEach((k, v) -> {
            if (!runtimeObjectSizes.containsKey(k)) {
                userObjectSizes.put(k, v);
            }
        });
        return userObjectSizes;
    }

    public static Map<String, Integer> computeAllObjectsSizes(Forest<String, byte[]> forest) {
        Map<String, Integer> map = new HashMap<>();
        map.putAll(computeShadowObjectSizes());
        map.putAll(computeApiObjectSizes());
        map.putAll(computeUserObjectSizes(forest, computeShadowObjectSizes(), computeApiObjectSizes()));

        return map;
    }

    public static Map<String, Integer> computeAllPostRenameObjectSizes(Forest<String, byte[]> forest) {
        Map<String, Integer> preRenameShadowObjectSizes = computeShadowObjectSizes();
        Map<String, Integer> preRenameApiObjectSizes = computeApiObjectSizes();
        Map<String, Integer> preRenameUserObjectSizes = computeUserObjectSizes(forest, preRenameShadowObjectSizes, preRenameApiObjectSizes);

        Map<String, Integer> postRenameObjectSizes = new HashMap<>(preRenameApiObjectSizes);
        for (Map.Entry<String, Integer> entry : preRenameUserObjectSizes.entrySet()) {
            postRenameObjectSizes.put(PackageConstants.kUserSlashPrefix + entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : preRenameShadowObjectSizes.entrySet()) {
            postRenameObjectSizes.put(PackageConstants.kShadowSlashPrefix + entry.getKey(), entry.getValue());
        }
        return postRenameObjectSizes;
    }

    /**
     * Replaces the <code>java.base</code> package with the shadow implementation.
     *
     * @param classes        the class of DApp (names specified in .-style)
     * @param preRenameClassHierarchy The pre-rename hierarchy of user-defined classes in the DApp (/-style).
     * @return the transformed classes and any generated classes (names specified in .-style)
     */
    public Map<String, byte[]> transformClasses(Map<String, byte[]> classes, Forest<String, byte[]> preRenameClassHierarchy) {

        // merge the generated classes and processed classes, assuming the package spaces do not conflict.
        Map<String, byte[]> processedClasses = new HashMap<>();
        // WARNING:  This dynamicHierarchyBuilder is both mutable and shared by TypeAwareClassWriter instances.
        HierarchyTreeBuilder dynamicHierarchyBuilder = new HierarchyTreeBuilder();
        // merge the generated classes and processed classes, assuming the package spaces do not conflict.
        // We also want to expose this type to the class writer so it can compute common superclasses.
        ExceptionWrapping.GeneratedClassConsumer generatedClassesSink = (superClassSlashName, classSlashName, bytecode) -> {
            // Note that the processed classes are expected to use .-style names.
            String classDotName = Helpers.internalNameToFulllyQualifiedName(classSlashName);
            processedClasses.put(classDotName, bytecode);
            String superClassDotName = Helpers.internalNameToFulllyQualifiedName(superClassSlashName);
            dynamicHierarchyBuilder.addClass(classDotName, superClassDotName, bytecode);
        };
        Set<String> preRenameUserDefinedClasses = ClassWhiteList.extractDeclaredClasses(preRenameClassHierarchy);
        Set<String> preRenameUserClassSet = classes.keySet();
        ParentPointers parentClassResolver = new ParentPointers(preRenameUserDefinedClasses, preRenameClassHierarchy);
        Map<String, Integer> postRenameObjectSizes = computeAllPostRenameObjectSizes(preRenameClassHierarchy);

        for (String name : classes.keySet()) {
            // Note that transformClasses requires that the input class names by the .-style names.
            Assert.assertTrue(-1 == name.indexOf("/"));

            // We need to parse with EXPAND_FRAMES, since the StackWatcherClassAdapter uses a MethodNode to parse methods.
            // We also add SKIP_DEBUG since we aren't using debug data and skipping it removes extraneous labels which would otherwise
            // cause the BlockBuildingMethodVisitor to build lots of small blocks instead of a few big ones (each block incurs a Helper
            // static call, which is somewhat expensive - this is how we bill for energy).
            int parsingOptions = ClassReader.EXPAND_FRAMES | ClassReader.SKIP_DEBUG;
            byte[] bytecode = new ClassToolchain.Builder(classes.get(name), parsingOptions)
                    .addNextVisitor(new RejectionClassVisitor())
                    .addNextVisitor(new UserClassMappingVisitor(preRenameUserClassSet))
                    .addNextVisitor(new ConstantVisitor(HELPER_CLASS))
                    .addNextVisitor(new ClassMetering(HELPER_CLASS, postRenameObjectSizes))
                    .addNextVisitor(new InvokedynamicShadower(PackageConstants.kShadowSlashPrefix))
                    .addNextVisitor(new ClassShadowing(HELPER_CLASS))
                    .addNextVisitor(new StackWatcherClassAdapter())
                    .addNextVisitor(new ExceptionWrapping(HELPER_CLASS, parentClassResolver, generatedClassesSink))
                    .addNextVisitor(new AutomaticGraphVisitor())
                    .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, parentClassResolver, dynamicHierarchyBuilder))
                    .build()
                    .runAndGetBytecode();
            bytecode = new ClassToolchain.Builder(bytecode, parsingOptions)
                    .addNextVisitor(new ArrayWrappingClassAdapterRef())
                    .addNextVisitor(new ArrayWrappingClassAdapter())
                    .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, parentClassResolver, dynamicHierarchyBuilder))
                    .build()
                    .runAndGetBytecode();
            String mappedName = PackageConstants.kUserDotPrefix + name;
            processedClasses.put(mappedName, bytecode);
        }
        return processedClasses;
    }

    public static class BlockchainRuntimeImpl extends org.aion.avm.shadow.java.lang.Object implements IBlockchainRuntime {

        private TransactionContext context;
        private IHelper helper;
        private TransactionResult result;

        private Transaction tx;
        private Block block;

        public BlockchainRuntimeImpl(TransactionContext context, IHelper helper, TransactionResult result) {
            this.context = context;
            this.helper = helper;
            this.result = result;

            this.tx = context.getTransaction();
            this.block = context.getBlock();
        }

        @Override
        public Address avm_getSender() {
            return new Address(tx.getFrom());
        }

        @Override
        public Address avm_getAddress() {
            // TODO: handle CREATE transaction
            return new Address(tx.getTo());
        }

        @Override
        public long avm_getEnergyLimit() {
            return tx.getEnergyLimit();
        }

        @Override
        public ByteArray avm_getData() {
            return new ByteArray(tx.getType() == Transaction.Type.CREATE ? Helpers.decodeCodeAndData(tx.getData())[1] : tx.getData());
        }

        @Override
        public ByteArray avm_getStorage(ByteArray key) {
            return new ByteArray(context.getStorage(tx.getTo(), key.getUnderlying()));
        }

        @Override
        public void avm_putStorage(ByteArray key, ByteArray value) {
            context.putStorage(tx.getTo(), key.getUnderlying(), value.getUnderlying());
        }

        @Override
        public void avm_updateCode(ByteArray newCode) {
            context.updateCode(tx.getTo(), newCode.getUnderlying());
        }

        @Override
        public void avm_selfDestruct(Address beneficiary) {
            context.selfdestruct(tx.getTo(), beneficiary.unwrap());
        }

        @Override
        public long avm_getBlockEpochSeconds() {
            return block.getTimestamp();
        }

        @Override
        public long avm_getBlockNumber() {
            return block.getNumber();
        }

        @Override
        public ByteArray avm_sha3(ByteArray data) {
            // For now, we just return the first 32 bytes of the input data since some tests want to call this and require 32 bytes but we eventually need a real implementation (issue-152).
            // TODO: we can implement this inside vm

            return new ByteArray(Arrays.copyOfRange(data.getUnderlying(), 0, 32));
        }

        @Override
        public ByteArray avm_call(Address targetAddress, long value, ByteArray data, long energyLimit) {
            // construct the internal transaction
            InternalTransaction internalTx = new InternalTransaction(Transaction.Type.CALL,
                    tx.getTo(),
                    targetAddress.unwrap(),
                    value,
                    data.getUnderlying(),
                    energyLimit,
                    tx);
            result.addInternalTransaction(internalTx);

            // execute the internal transaction
            TransactionResult newResult = context.call(internalTx);

            // merge the results
            result.merge(newResult);

            // reset the thread-local helper instance
            IHelper.currentContractHelper.set(helper);

            // charge energy consumed
            helper.externalChargeEnergy(newResult.getEnergyUsed());

            return new ByteArray(newResult.getReturnData());
        }

        @Override
        public void avm_log(ByteArray data) {
            Log log = new Log(tx.getTo(), List.of(), data.getUnderlying());
            result.addLog(log);
        }

        @Override
        public void avm_log(ByteArray topic1, ByteArray data) {
            Log log = new Log(tx.getTo(), List.of(topic1.getUnderlying()), data.getUnderlying());
            result.addLog(log);
        }

        @Override
        public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray data) {
            Log log = new Log(tx.getTo(), List.of(topic1.getUnderlying(), topic2.getUnderlying()), data.getUnderlying());
            result.addLog(log);
        }

        @Override
        public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray data) {
            Log log = new Log(tx.getTo(), List.of(topic1.getUnderlying(), topic2.getUnderlying(), topic3.getUnderlying()), data.getUnderlying());
            result.addLog(log);
        }

        @Override
        public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray topic4, ByteArray data) {
            Log log = new Log(tx.getTo(), List.of(topic1.getUnderlying(), topic2.getUnderlying(), topic3.getUnderlying(), topic4.getUnderlying()), data.getUnderlying());
            result.addLog(log);
        }
    }

    @Override
    public TransactionResult run(TransactionContext ctx) {

        Transaction tx = ctx.getTransaction();
        Block block = ctx.getBlock();

        // only one result (mutable) shall be created per transaction execution
        TransactionResult result = new TransactionResult();

        // TODO: charge basic transaction cost

        switch (tx.getType()) {
            case CREATE:
                create(tx, block, ctx, result);
                break;
            case CALL:
                call(tx, block, ctx, result);
                break;
            default:
                result.setStatusCode(TransactionResult.Code.INVALID_TX);
                result.setEnergyUsed(tx.getEnergyLimit());
                break;
        }

        return result;
    }

    public void create(Transaction tx, Block block, TransactionContext ctx, TransactionResult result) {
        try {
            // read dapp module
            //TODO: If we make dapp storage into two-level Key Value storage, we can detect duplicated dappAddress
            byte[] dappAddress = Helpers.randomBytes(Address.LENGTH);
            byte[] dappCode = Helpers.decodeCodeAndData(tx.getData())[0];

            RawDappModule rawDapp = RawDappModule.readFromJar(dappCode);
            if (rawDapp == null) {
                result.setStatusCode(TransactionResult.Code.INVALID_JAR);
                result.setEnergyUsed(tx.getEnergyLimit());
                return;
            }

            // validate dapp module
            if (!validateDapp(rawDapp)) {
                result.setStatusCode(TransactionResult.Code.INVALID_CODE);
                result.setEnergyUsed(tx.getEnergyLimit());
                return;
            }
            ClassHierarchyForest dappClassesForest = rawDapp.classHierarchyForest;

            // transform
            Map<String, byte[]> transformedClasses = transformClasses(rawDapp.classes, dappClassesForest);
            TransformedDappModule transformedDapp = TransformedDappModule.fromTransformedClasses(transformedClasses, rawDapp.mainClass);

            // As per usual, we need to get the special Helper class for each contract loader.
            Map<String, byte[]> allClasses = Helpers.mapIncludingHelperBytecode(transformedClasses);

            // Construct the per-contract class loader and access the per-contract IHelper instance.
            AvmClassLoader classLoader = NodeEnvironment.singleton.createInvocationClassLoader(allClasses);

            // We start the nextHashCode at 1.
            int nextHashCode = 1;
            IHelper helper = Helpers.instantiateHelper(classLoader, tx.getEnergyLimit(), nextHashCode);
            Helpers.attachBlockchainRuntime(classLoader, new BlockchainRuntimeImpl(ctx, helper, result));

            // billing the Processing cost, see {@linktourl https://github.com/aionnetworkp/aion_vm/wiki/Billing-the-Contract-Deployment}
            helper.externalChargeEnergy(BytecodeFeeScheduler.BytecodeEnergyLevels.PROCESS.getVal()
                    + BytecodeFeeScheduler.BytecodeEnergyLevels.PROCESSDATA.getVal() * rawDapp.bytecodeSize * (1 + rawDapp.numberOfClasses) / 10);

            // Create the immortal version of the transformed DApp code by stripping the <clinit>.
            Map<String, byte[]> immortalClasses = new HashMap<>();
            for (Map.Entry<String, byte[]> elt : transformedClasses.entrySet()) {
                String className = elt.getKey();
                byte[] transformedClass = elt.getValue();
                byte[] immortalClass = new ClassToolchain.Builder(transformedClass, 0)
                        .addNextVisitor(new ClinitStrippingVisitor())
                        .addWriter(new ClassWriter(0))
                        .build()
                        .runAndGetBytecode();
                immortalClasses.put(className, immortalClass);
            }
            ImmortalDappModule immortalDapp = ImmortalDappModule.fromImmortalClasses(immortalClasses, transformedDapp.mainClass);

            // store transformed dapp
            byte[] immortalDappJar = immortalDapp.createJar(dappAddress);
            ctx.putTransformedCode(dappAddress, VERSION, immortalDappJar);

            // billing the Storage cost, see {@linktourl https://github.com/aionnetworkp/aion_vm/wiki/Billing-the-Contract-Deployment}
            helper.externalChargeEnergy(BytecodeFeeScheduler.BytecodeEnergyLevels.CODEDEPOSIT.getVal() * tx.getData().length);

            // TODO: create invocation need further discussion

            String mappedUserMainClass = PackageConstants.kUserDotPrefix + transformedDapp.mainClass;
            Class<?> clazz = Class.forName(mappedUserMainClass, true, classLoader);

            // Save back the state before we return.
            // -first, save out the classes
            // TODO: Make this fully walk the graph
            long initialInstanceId = 1l;
            long nextInstanceId = new LoadedDApp(classLoader, dappAddress, Helpers.getAlphabeticalUserTransformedClasses(classLoader, allClasses.keySet())).saveClassStaticsToStorage(initialInstanceId, ctx);
            // -finally, save back the final state of the environment so we restore it on the next invocation.
            ContractEnvironmentState.saveToStorage(ctx, dappAddress, new ContractEnvironmentState(helper.externalGetNextHashCode(), nextInstanceId));

            // TODO: whether we should return the dapp address is subject to change
            result.setStatusCode(TransactionResult.Code.SUCCESS);
            result.setEnergyUsed(tx.getEnergyLimit() - helper.externalGetEnergyRemaining());
            result.setReturnData(dappAddress);
        } catch (FatalAvmError e) {
            // These are unrecoverable errors (either a bug in our code or a lower-level error reported by the JVM).
            // (for now, we System.exit(-1), since this is what ethereumj does, but we may want a more graceful shutdown in the future)
            e.printStackTrace();
            System.exit(-1);
        } catch (OutOfEnergyError e) {
            result.setStatusCode(TransactionResult.Code.OUT_OF_ENERGY);
            result.setEnergyUsed(tx.getEnergyLimit());
        } catch (AvmException e) {
            // We handle the generic AvmException as some failure within the contract.
            result.setStatusCode(TransactionResult.Code.FAILURE);
            result.setEnergyUsed(tx.getEnergyLimit());
        } catch (Throwable t) {
            // There should be no other reachable kind of exception.  If we reached this point, something very strange is happening so log
            // this and bring us down.
            t.printStackTrace();
            System.exit(1);
        }
    }

    public void call(Transaction tx, Block block, TransactionContext ctx, TransactionResult result) {
        // retrieve the transformed bytecode
        byte[] dappAddress = tx.getTo();
        ImmortalDappModule app;
        try {
            byte[] immortalDappJar = ctx.getTransformedCode(dappAddress);
            app = ImmortalDappModule.readFromJar(immortalDappJar);
        } catch (IOException e) {
            result.setStatusCode(TransactionResult.Code.INVALID_CALL);
            result.setEnergyUsed(tx.getEnergyLimit());
            return;
        }

        // As per usual, we need to get the special Helper class for each contract loader.
        Map<String, byte[]> allClasses = Helpers.mapIncludingHelperBytecode(app.classes);

        // Construct the per-contract class loader and access the per-contract IHelper instance.
        AvmClassLoader classLoader = NodeEnvironment.singleton.createInvocationClassLoader(allClasses);
        
        // Load all the user-defined classes (these are required for both loading and storing state).
        List<Class<?>> aphabeticalContractClasses = Helpers.getAlphabeticalUserTransformedClasses(classLoader, allClasses.keySet());

        // Load the initial state of the environment.
        ContractEnvironmentState initialState = ContractEnvironmentState.loadFromStorage(ctx, dappAddress);
        
        // TODO:  We might be able to move this setup of IHelper to later in the load once we get rid of the <clinit> (requires energy).
        IHelper helper = Helpers.instantiateHelper(classLoader, tx.getEnergyLimit(), initialState.nextHashCode);
        Helpers.attachBlockchainRuntime(classLoader, new BlockchainRuntimeImpl(ctx, helper, result));

        // Now that we can load classes for the contract, load and populate all their classes.
        LoadedDApp dapp = new LoadedDApp(classLoader, dappAddress, aphabeticalContractClasses);
        dapp.populateClassStaticsFromStorage(ctx);

        // load class
        try {
            String mappedUserMainClass = PackageConstants.kUserDotPrefix + app.mainClass;
            Class<?> clazz = classLoader.loadClass(mappedUserMainClass);

            Method method = clazz.getMethod("avm_main");
            ByteArray rawResult = (ByteArray) method.invoke(null);
            byte[] ret = (null != rawResult)
                    ? rawResult.getUnderlying()
                    : null;

            // Save back the state before we return.
            // -first, save out the classes
            // TODO: Make this fully walk the graph
            // TODO: Get the updated "nextInstanceId" after everything is written to storage.
            long nextInstanceId = dapp.saveClassStaticsToStorage(initialState.nextInstanceId, ctx);
            // -finally, save back the final state of the environment so we restore it on the next invocation.
            ContractEnvironmentState.saveToStorage(ctx, dappAddress, new ContractEnvironmentState(helper.externalGetNextHashCode(), nextInstanceId));

            result.setStatusCode(TransactionResult.Code.SUCCESS);
            result.setReturnData(ret);
            result.setEnergyUsed(tx.getEnergyLimit() - helper.externalGetEnergyRemaining());
        } catch (OutOfEnergyError e) {
            result.setStatusCode(TransactionResult.Code.OUT_OF_ENERGY);
            result.setEnergyUsed(tx.getEnergyLimit());
        } catch (Exception e) {
            result.setStatusCode(TransactionResult.Code.FAILURE);
            result.setEnergyUsed(tx.getEnergyLimit());
        }
    }
}
