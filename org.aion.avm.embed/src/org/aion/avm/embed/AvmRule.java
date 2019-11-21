package org.aion.avm.embed;

import avm.Address;
import org.aion.avm.core.*;
import org.aion.avm.tooling.deploy.OptimizedJarBuilder;
import org.aion.kernel.TestingState;
import org.aion.types.*;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.abi.ABICompiler;
import org.aion.avm.tooling.abi.ABIConfig;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.utilities.JarBuilder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

/**
 * TestRule to handle the boilerplate operations of testing with an embedded avm.
 * <p>
 * If declared with @Rule annotation, the kernel and avm are instantiated for each for each test.
 * <p>
 * Otherwise, when declared with @ClassRule annotation, the kernel and avm are instantiated once for the test class.
 */
public final class AvmRule implements TestRule {

    private boolean debugMode;
    private boolean automaticBlockGenerationEnabled;
    private boolean enableBlockchainPrintln;
    public TestingState kernel;
    public AvmImpl avm;
    // Note that the base version is used for the ABICompiler temporarily, mainly to allow external tools to update to the latest version
    private int compilerDefaultVersion = ABICompiler.getDefaultVersionNumber();

    /**
     * @param debugMode enable/disable the debugging features
     */
    public AvmRule(boolean debugMode) {
        this.debugMode = debugMode;
        this.kernel = new TestingState();
        automaticBlockGenerationEnabled = true;
        // By default, we assume that the user wants to see this output.
        this.enableBlockchainPrintln = true;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                AvmConfiguration config = new AvmConfiguration();
                if (AvmRule.this.debugMode) {
                    config.preserveDebuggability = true;
                    config.enableVerboseContractErrors = true;
                }
                config.enableBlockchainPrintln = AvmRule.this.enableBlockchainPrintln;
                avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new StandardCapabilities(), config);
                try {
                    statement.evaluate();
                } finally {
                    avm.shutdown();
                    avm = null;
                }
            }
        };
    }

    /**
     * Retrieves bytes corresponding to the in-memory representation of Dapp jar.
     * This uses the base ABI compiler version.
     * @param mainClass Main class of the Dapp to include and list in manifest (can be null).
     * @param arguments Constructor arguments
     * @param otherClasses Other classes to include (main is already included).
     * @return Byte array corresponding to the optimized deployable Dapp jar and arguments, where unreachable classes and methods are removed from the jar
     */
    public byte[] getDappBytes(Class<?> mainClass, byte[] arguments, Class<?>... otherClasses) {
        return compileAndOptimizeDapp(mainClass, arguments, compilerDefaultVersion, otherClasses);
    }

    /**
     * Retrieves bytes corresponding to the in-memory representation of Dapp jar.
     * @param mainClass Main class of the Dapp to include and list in manifest (can be null).
     * @param arguments Constructor arguments
     * @param abiVersion Version of ABI compiler to use
     * @param otherClasses Other classes to include (main is already included).
     * @return Byte array corresponding to the optimized deployable Dapp jar and arguments, where unreachable classes and methods are removed from the jar
     */
    public byte[] getDappBytes(Class<?> mainClass, byte[] arguments, int abiVersion, Class<?>... otherClasses) {
        return compileAndOptimizeDapp(mainClass, arguments, abiVersion, otherClasses);
    }

    /**
     * Retrieves bytes corresponding to the in-memory representation of Dapp jar.
     * This is always using the latest ABI version.
     * @param mainClass Main class of the Dapp to include and list in manifest (can be null).
     * @param arguments Constructor arguments
     * @param otherClasses Other classes to include (main is already included).
     * @return Byte array corresponding to the deployable Dapp jar and arguments.
     */
    public byte[] getDappBytesWithoutOptimization(Class<?> mainClass, byte[] arguments, Class<?>... otherClasses) {
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(mainClass, Collections.emptyMap(), otherClasses);
        ABICompiler compiler = ABICompiler.compileJarBytes(jar, ABIConfig.LATEST_VERSION);
        return new CodeAndArguments(compiler.getJarFileBytes(), arguments).encodeToBytes();
    }

    /**
     * Deploys the Dapp.
     * @param from Address of the deployer account
     * @param value Value to transfer to the Dapp
     * @param dappBytes Byte array corresponding to the Dapp jar
     * @param energyLimit Maximum energy to be used for deployment
     * @param energyPrice Energy price to be used for deployment
     * @return Result of the operation
     */
    public ResultWrapper deploy(Address from, BigInteger value, byte[] dappBytes, long energyLimit, long energyPrice) {
        return deployDapp(from, value, dappBytes, energyLimit, energyPrice);
    }

    /**
     * Deploys the Dapp.
     * Energy limit is set to the maximum allowed in the kernel for deploying contracts
     * @param from Address of the deployer account
     * @param value Value to transfer to the Dapp
     * @param dappBytes Byte array corresponding to the Dapp jar
     * @return Result of the operation
     */
    public ResultWrapper deploy(Address from, BigInteger value, byte[] dappBytes) {
        long energyLimit = 5_000_000L;
        long energyPrice = 1L;
        return deployDapp(from, value, dappBytes, energyLimit, energyPrice);
    }

    /**
     * Makes a call transaction to the Dapp.
     * @param from Address of the account calling the Dapp
     * @param dappAddress Address of the Dapp
     * @param value Value to transfer with the call
     * @param transactionData The encoded byte array that contains the method descriptor, argument descriptor and encoded arguments, according the Aion ABI format.
     * @param energyLimit Maximum energy to be used for the call
     * @param energyPrice Energy price to be used for the call
     * @return Result of the operation
     */
    public ResultWrapper call(Address from, Address dappAddress, BigInteger value, byte[] transactionData, long energyLimit, long energyPrice) {
        return callDapp(from, dappAddress, value, transactionData, energyLimit, energyPrice);
    }

    /**
     * Makes a call transaction to the Dapp.
     * Energy limit is set to the maximum allowed in the kernel for a call transaction
     * @param from Address of the account calling the Dapp
     * @param dappAddress Address of the Dapp
     * @param value Value to transfer with the call
     * @param transactionData The encoded byte array that contains the method descriptor, argument descriptor and encoded arguments, according the Aion ABI format.
     * @return Result of the operation
     */
    public ResultWrapper call(Address from, Address dappAddress, BigInteger value, byte[] transactionData) {
        long energyLimit = 2_000_000L;
        long energyPrice = 1L;
        return callDapp(from, dappAddress, value, transactionData, energyLimit, energyPrice);
    }

    /**
     * Makes a balance transfer.
     * @param from Address of sender
     * @param to Address of the receiver
     * @param value Transfer amount
     * @param energyLimit Maximum energy to be used for the call
     * @param energyPrice Energy price to be used for the transfer
     * @return Result of the operation
     */
    public ResultWrapper balanceTransfer(Address from, Address to, BigInteger value, long energyLimit, long energyPrice) {
        Transaction tx = AvmTransactionUtil.call(new AionAddress(from.toByteArray()), new AionAddress(to.toByteArray()), kernel.getNonce(new AionAddress(from.toByteArray())), value, new byte[0], energyLimit, energyPrice);

        return new ResultWrapper(avm.run(this.kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult());
    }

    /**
     * Creates an account with an initial balance in the kernel
     * @param initialBalance Initial balance of the created account
     * @return Address of the newly created account
     */
    public Address getRandomAddress(BigInteger initialBalance) {
        AionAddress account = Helpers.randomAddress();
        kernel.adjustBalance(new AionAddress(account.toByteArray()), initialBalance);
        return new Address(account.toByteArray());
    }

    /**
     * @return Address of the account with initial (pre-mined) balance in the kernel
     */
    public Address getPreminedAccount() {
        return new Address(TestingState.PREMINED_ADDRESS.toByteArray());
    }

    /**
     * Disables automatic generation of blocks for each transaction
     */
    public void disableAutomaticBlockGeneration(){
        automaticBlockGenerationEnabled = false;
    }

    /**
     * Used to set whether or not the Blockchain.println statements of any AVM instances created by the rule should be enabled.
     * When enabled, these write to stdout but, when disabled, safely have no effect.
     * 
     * @param enableBlockchainPrintln True if the Blockchain.println statements should write to stdout (default is true).
     * @return The receiver, for chaining.
     */
    public AvmRule setBlockchainPrintlnEnabled(boolean enableBlockchainPrintln) {
        if (null != this.avm) {
            throw new IllegalStateException("AVM already created");
        }
        this.enableBlockchainPrintln = enableBlockchainPrintln;
        return this;
    }

    private ResultWrapper callDapp(Address from, Address dappAddress, BigInteger value, byte[] transactionData, long energyLimit, long energyPrice) {
        if (automaticBlockGenerationEnabled) {
            this.kernel.generateBlock();
        }Transaction tx = AvmTransactionUtil.call(new AionAddress(from.toByteArray()), new AionAddress(dappAddress.toByteArray()), kernel.getNonce(new AionAddress(from.toByteArray())), value, transactionData, energyLimit, energyPrice);
        return new ResultWrapper(avm.run(this.kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult());
    }

    private ResultWrapper deployDapp(Address from, BigInteger value, byte[] dappBytes, long energyLimit, long energyPrice) {
        if (automaticBlockGenerationEnabled) {
            this.kernel.generateBlock();
        }Transaction tx = AvmTransactionUtil.create(new AionAddress(from.toByteArray()), kernel.getNonce(new AionAddress(from.toByteArray())), value, dappBytes, energyLimit, energyPrice);
        return new ResultWrapper(avm.run(this.kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult());
    }

    /**
     * An abstract container of the result of running a transaction or call on {@link AvmRule}.
     * <p>
     * This provides high-level accessors for interpreting the consequences of the transaction or call.
     */
    public static class ResultWrapper {
        TransactionResult result;

        ResultWrapper(TransactionResult result) {
            this.result = result;
        }

        /**
         * @return Result of the transaction execution
         */
        public TransactionResult getTransactionResult(){
            return result;
        }

        /**
         * @return Address of the Dapp deployed in this transaction, Null if the deploy transaction failed
         */
        public Address getDappAddress() {
            if (!result.transactionStatus.isSuccess()) {
                System.out.println("Contract deployment failed with error " + result.transactionStatus.causeOfError);
                return null;
            }
            return new Address(result.copyOfTransactionOutput().orElseThrow());
        }

        /**
         * @return Decoded returned data of the call
         */
        public Object getDecodedReturnData() {
            return ABIUtil.decodeOneObject(result.copyOfTransactionOutput().orElseThrow());
        }

        /**
         * @return Transaction execution result code, which can be SUCCESS, REJECTED, FAILED, REVERT, or FATAL.
         */
        public TransactionStatus getReceiptStatus() {
            return result.transactionStatus;
        }

        /**
         * @return List of execution log objects
         */
        public List<Log> getLogs() {
            return result.logs;
        }
    }

    private byte[] compileAndOptimizeDapp(Class<?> mainClass, byte[] arguments, int abiVersion, Class<?>[] otherClasses){
        byte[] jar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(mainClass, Collections.emptyMap(), otherClasses);
        byte[] optimizedJar = new OptimizedJarBuilder(debugMode, jar, abiVersion)
                .withUnreachableMethodRemover()
                .withRenamer()
                .withConstantRemover()
                .getOptimizedBytes();
        return new CodeAndArguments(optimizedJar, arguments).encodeToBytes();

    }
}
