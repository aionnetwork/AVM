package org.aion.avm.core.util;

import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.*;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.math.BigInteger;
import java.util.List;

/**
 * TestRule to handle the boilerplate operations of testing with an embedded avm.
 * If declared with @Rule annotation, the kernel and avm are instantiated for each for each test.
 * Otherwise, when declared with @ClassRule annotation, the kernel and avm are instantiated once for the test class.
 */
public final class AvmRule implements TestRule {

    private boolean debugMode;
    public KernelInterfaceImpl kernel;
    public VirtualMachine avm;
    public Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    /**
     * @param debugMode enable/disable the debugging features
     */
    public AvmRule(boolean debugMode) {
        this.debugMode = debugMode;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                kernel = new KernelInterfaceImpl();
                avm = debugMode ? CommonAvmFactory.buildAvmInstanceInDebugMode(kernel) : CommonAvmFactory.buildAvmInstance(kernel);
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
     * @return Byte array corresponding to the deployable Dapp jar and arguments.
     */
    public byte[] getDappBytes(Class<?> mainClass, byte[] arguments, Class<?>... otherClasses) {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(mainClass, otherClasses);
        return new CodeAndArguments(jar, arguments).encodeToBytes();
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
     * @param energyPrice Energy price to be used for the transfer
     * @return Result of the operation
     */
    public ResultWrapper balanceTransfer(Address from, Address to, BigInteger value, long energyPrice){
        Transaction tx = Transaction.balanceTransfer(from, to, kernel.getNonce(from), value, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        return new ResultWrapper (avm.run(new TransactionContext[] {context})[0].get(), context.getSideEffects());
    }

    /**
     * Creates an account with an initial balance in the kernel
     * @param initialBalance Initial balance of the created account
     * @return Address of the newly created account
     */
    public Address getRandomAddress(BigInteger initialBalance){
        Address account = Helpers.randomAddress();
        kernel.adjustBalance(account, initialBalance);
        return account;
    }

    /**
     * @return Address of the account with initial (pre-mined) balance in the kernel
     */
    public Address getPreminedAccount(){
        return  KernelInterfaceImpl.PREMINED_ADDRESS;
    }

    private ResultWrapper callDapp(Address from, Address dappAddress, BigInteger value, byte[] transactionData, long energyLimit, long energyPrice) {
        Transaction tx = Transaction.call(from, dappAddress, kernel.getNonce(from), value, transactionData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        return new ResultWrapper(avm.run(new TransactionContext[]{context})[0].get(), context.getSideEffects());
    }

    private ResultWrapper deployDapp(Address from, BigInteger value, byte[] dappBytes, long energyLimit, long energyPrice) {
        Transaction tx = Transaction.create(from, kernel.getNonce(from), value, dappBytes, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        return new ResultWrapper(avm.run(new TransactionContext[]{context})[0].get(), context.getSideEffects());
    }

    public static class ResultWrapper {
        TransactionResult result;
        TransactionSideEffects sideEffects;

        ResultWrapper(TransactionResult result, TransactionSideEffects sideEffects) {
            this.result = result;
            this.sideEffects = sideEffects;
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
            return result.getResultCode().isSuccess()? AvmAddress.wrap(result.getReturnData()) : null;
        }

        /**
         * @return Decoded returned data of the call
         */
        public Object getDecodedReturnData() {
            return TestingHelper.decodeResult(result);
        }

        /**
         * @return Transaction execution result code, which can be SUCCESS, REJECTED, or FAILED.
         */
        public ResultCode getReceiptStatus() {
            return result.getResultCode();
        }

        /**
         * @return List of log objects
         */
        public List<IExecutionLog> getLogs(){
            return sideEffects.getExecutionLogs();
        }
    }
}