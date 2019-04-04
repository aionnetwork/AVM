package org.aion.avm.tooling;

import avm.Address;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.tooling.abi.ABICompiler;
import org.aion.avm.tooling.deploy.JarOptimizer;
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
    private final ABICompiler compiler;
    private final JarOptimizer jarOptimizer;
    public TestingKernel kernel;
    public AvmImpl avm;
    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    /**
     * @param debugMode enable/disable the debugging features
     */
    public AvmRule(boolean debugMode) {
        this.debugMode = debugMode;
        this.kernel = new TestingKernel(block);
        compiler = new ABICompiler();
        jarOptimizer = new JarOptimizer(debugMode);
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
     * @return Byte array corresponding to the deployable Dapp jar and arguments.
     */
    public byte[] getDappBytes(Class<?> mainClass, byte[] arguments, Class<?>... otherClasses) {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(mainClass, otherClasses);
        compiler.compile(jar);
        byte[] optimizedDappBytes = jarOptimizer.optimize(compiler.getJarFileBytes());
        return new CodeAndArguments(optimizedDappBytes, arguments).encodeToBytes();
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
        Transaction tx = Transaction.call(org.aion.types.Address.wrap(from.unwrap()), org.aion.types.Address.wrap(to.unwrap()), kernel.getNonce(org.aion.types.Address.wrap(from.unwrap())), value, new byte[0], energyLimit, energyPrice);

        return new ResultWrapper(avm.run(this.kernel, new Transaction[]{tx})[0].get());
    }

    /**
     * Creates an account with an initial balance in the kernel
     * @param initialBalance Initial balance of the created account
     * @return Address of the newly created account
     */
    public Address getRandomAddress(BigInteger initialBalance) {
        org.aion.types.Address account = Helpers.randomAddress();
        kernel.adjustBalance(account, initialBalance);
        return new Address(account.toBytes());
    }

    /**
     * @return Address of the account with initial (pre-mined) balance in the kernel
     */
    public Address getPreminedAccount() {
        return new Address(TestingKernel.PREMINED_ADDRESS.toBytes());
    }

    /**
     * @return Address of the account with huge initial (pre-mined) balance in the kernel
     */
    public Address getBigPreminedAccount() {
        return new Address(TestingKernel.BIG_PREMINED_ADDRESS.toBytes());
    }

    private ResultWrapper callDapp(Address from, Address dappAddress, BigInteger value, byte[] transactionData, long energyLimit, long energyPrice) {
        Transaction tx = Transaction.call(org.aion.types.Address.wrap(from.unwrap()), org.aion.types.Address.wrap(dappAddress.unwrap()), kernel.getNonce(org.aion.types.Address.wrap(from.unwrap())), value, transactionData, energyLimit, energyPrice);
        return new ResultWrapper(avm.run(this.kernel, new Transaction[]{tx})[0].get());
    }

    private ResultWrapper deployDapp(Address from, BigInteger value, byte[] dappBytes, long energyLimit, long energyPrice) {
        Transaction tx = Transaction.create(org.aion.types.Address.wrap(from.unwrap()), kernel.getNonce(org.aion.types.Address.wrap(from.unwrap())), value, dappBytes, energyLimit, energyPrice);
        return new ResultWrapper(avm.run(this.kernel, new Transaction[]{tx})[0].get());
    }

    public Block getBlock() {
        return block;
    }

    public void updateBlock(Block b) {
        this.block = b;
        this.kernel.updateBlock(b);
    }

    public static class ResultWrapper {
        TransactionResult result;
        TransactionSideEffects sideEffects;

        ResultWrapper(TransactionResult result) {
            this.result = result;
            this.sideEffects = result.getSideEffects();
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
            if (!result.getResultCode().isSuccess()) {
                System.out.println("Contract deployment failed with error " + result.getResultCode());
                return null;
            }
            return new Address(result.getReturnData());
        }

        /**
         * @return Decoded returned data of the call
         */
        public Object getDecodedReturnData() {
            return ABIUtil.decodeOneObject(result.getReturnData());
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
