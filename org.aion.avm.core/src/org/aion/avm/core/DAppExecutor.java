package org.aion.avm.core;

import org.aion.avm.arraywrapper.*;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.persistence.ContractEnvironmentState;
import org.aion.avm.core.persistence.LoadedDApp;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.*;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionResult;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;


public class DAppExecutor {
    public static void call(KernelInterface kernel, Avm avm, Transaction tx, TransactionContext ctx, TransactionResult result) {
        // retrieve the transformed bytecode
        byte[] dappAddress = tx.getTo();
        ImmortalDappModule app;
        try {
            byte[] immortalDappJar = kernel.getTransformedCode(dappAddress);
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
        ContractEnvironmentState initialState = ContractEnvironmentState.loadFromStorage(kernel, dappAddress);
        
        // TODO:  We might be able to move this setup of IHelper to later in the load once we get rid of the <clinit> (requires energy).
        IHelper helper = Helpers.instantiateHelper(classLoader, tx.getEnergyLimit(), initialState.nextHashCode);
        Helpers.attachBlockchainRuntime(classLoader, new BlockchainRuntimeImpl(kernel, avm, ctx, helper, result));

        // Now that we can load classes for the contract, load and populate all their classes.
        LoadedDApp dapp = new LoadedDApp(classLoader, dappAddress, aphabeticalContractClasses);
        dapp.populateClassStaticsFromStorage(kernel);

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
            long nextInstanceId = dapp.saveClassStaticsToStorage(initialState.nextInstanceId, kernel);
            // -finally, save back the final state of the environment so we restore it on the next invocation.
            ContractEnvironmentState.saveToStorage(kernel, dappAddress, new ContractEnvironmentState(helper.externalGetNextHashCode(), nextInstanceId));

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
