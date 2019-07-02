package org.aion.avm.embed;

import avm.Address;
import org.aion.avm.core.*;
import org.aion.avm.tooling.deploy.renamer.Renamer;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.abi.ABICompiler;
import org.aion.avm.tooling.deploy.JarOptimizer;
import org.aion.avm.tooling.deploy.eliminator.UnreachableMethodRemover;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.types.TransactionResult;
import org.aion.types.TransactionStatus;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.math.BigInteger;

/**
 * TestRule to handle the boilerplate operations of testing with an embedded avm.
 * If declared with @Rule annotation, the kernel and avm are instantiated for each for each test.
 * Otherwise, when declared with @ClassRule annotation, the kernel and avm are instantiated once for the test class.
 */
public final class AvmRule implements TestRule {

    private boolean debugMode;
    private final JarOptimizer jarOptimizer;
    private boolean automaticBlockGenerationEnabled;
    public TestingState kernel;
    public AvmImpl avm;

    /**
     * @param debugMode enable/disable the debugging features
     */
    public AvmRule(boolean debugMode) {
        this.debugMode = debugMode;
        this.kernel = new TestingState();
        jarOptimizer = new JarOptimizer(debugMode);
        automaticBlockGenerationEnabled = true;
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
                avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new StandardCapabilities(), config);
                try {
                    statement.evaluate();
                } finally {
                    avm.shutdown();
                }
            }
        };
    }

    /**
     * Retrieves bytes corresponding to the in-memory representation of Dapp jar.
     * @param mainClass Main class of the Dapp to include and list in manifest (can be null).
     * @param arguments Constructor arguments
     * @param otherClasses Other classes to include (main is already included).
     * @return Byte array corresponding to the optimized deployable Dapp jar and arguments, where unreachable classes and methods are removed from the jar
     */
    public byte[] getDappBytes(Class<?> mainClass, byte[] arguments, Class<?>... otherClasses) {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(mainClass, otherClasses);
        ABICompiler compiler = ABICompiler.compileJarBytes(jar);
        byte[] optimizedDappBytes = jarOptimizer.optimize(compiler.getJarFileBytes());
        try {
            optimizedDappBytes = UnreachableMethodRemover.optimize(optimizedDappBytes);
        } catch (Exception exception) {
            System.err.println("UnreachableMethodRemover crashed, packaging code without this optimization");
        }

        // renaming is disabled in debug mode.
        // This is because only field and method renaming can work correctly in debug mode, but the new names in those cases can cause confusion.
        if(!debugMode) {
            try {
                optimizedDappBytes = Renamer.rename(optimizedDappBytes);
            } catch (Exception exception) {
                System.err.println("Renaming crashed, packaging code without this optimization");
            }
        }
        return new CodeAndArguments(optimizedDappBytes, arguments).encodeToBytes();
    }

    /**
     * Retrieves bytes corresponding to the in-memory representation of Dapp jar.
     * @param mainClass Main class of the Dapp to include and list in manifest (can be null).
     * @param arguments Constructor arguments
     * @param otherClasses Other classes to include (main is already included).
     * @return Byte array corresponding to the deployable Dapp jar and arguments.
     */
    public byte[] getDappBytesWithoutOptimization(Class<?> mainClass, byte[] arguments, Class<?>... otherClasses) {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(mainClass, otherClasses);
        ABICompiler compiler = ABICompiler.compileJarBytes(jar);
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
        long energyLimit = 10_000_000L;
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
    }
}
