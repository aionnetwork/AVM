package org.aion.avm.core;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.poc.AionBufferPerfContract;
import org.aion.avm.core.poc.TRS;
import org.aion.avm.core.testBlake2b.Blake2b;
import org.aion.avm.core.testBlake2b.Main;
import org.aion.avm.core.testExchange.CoinController;
import org.aion.avm.core.testExchange.ERC20;
import org.aion.avm.core.testExchange.ERC20Token;
import org.aion.avm.core.testExchange.Exchange;
import org.aion.avm.core.testExchange.ExchangeController;
import org.aion.avm.core.testExchange.ExchangeTransaction;
import org.aion.avm.core.testWallet.ByteArrayHelpers;
import org.aion.avm.core.testWallet.ByteArrayWrapper;
import org.aion.avm.core.testWallet.BytesKey;
import org.aion.avm.core.testWallet.Daylimit;
import org.aion.avm.core.testWallet.EventLogger;
import org.aion.avm.core.testWallet.Multiowned;
import org.aion.avm.core.testWallet.Operation;
import org.aion.avm.core.testWallet.RequireFailedException;
import org.aion.avm.core.testWallet.Wallet;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.userlib.AionBuffer;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.vm.api.interfaces.Address;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.VirtualMachine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This suite is really just for viewing the deployment costs of some of our various Dapp examples.
 * Nothing explicitly is actually verified by these 'tests'.
 * The purpose is more to give us an idea about how our deployment costs look for different Dapps.
 */
public class DeploymentCostTest {
    private static final long ENERGY_LIMIT = 100_000_000_000L;
    private static final long ENERGY_PRICE = 1L;
    private static final Block BLOCK = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
    private static final Address DEPLOYER = KernelInterfaceImpl.PREMINED_ADDRESS;
    
    private KernelInterfaceImpl kernel;
    private VirtualMachine avm;

    // NOTE: To add a new dApp to this test simply do the following:
    // 1. Create a Contract enum for it and put a String representation into contractsAsStrings
    // 2. Inside getDeploymentJarBytesForContract() create a new case for this enum and grab the
    //    jar bytes for all of the classes involved in creating the dApp.
    //    There are examples for dApps with & without clinit args.

    /**
     * The contracts/dApps we use in the deployment cost test.
     */
    private enum Contract {
        BLAKE2B, AION_BUFFER_PERF, POC_WALLET, BASIC_PERF, POC_EXCHANGE, ERC20, BASIC_APP, TRS;

        private static Map<Contract, String> contractsAsStrings = new HashMap<>();

        static {
            contractsAsStrings.put(BLAKE2B, "Blake2b");
            contractsAsStrings.put(AION_BUFFER_PERF, "AionBufferPerfContract");
            contractsAsStrings.put(POC_WALLET, "PocWallet");
            contractsAsStrings.put(BASIC_PERF, "BasicPerfContract");
            contractsAsStrings.put(POC_EXCHANGE, "PocExchange");
            contractsAsStrings.put(ERC20, "ERC20");
            contractsAsStrings.put(BASIC_APP, "BasicAppTestTarget");
            contractsAsStrings.put(TRS, "TRS");
        }

        @Override
        public String toString() {
            return contractsAsStrings.get(this);
        }

    }

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstance(this.kernel);
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
        this.avm = null;
        this.kernel = null;
    }

    /**
     * Displays the size of the dApp as well as the cost to deploy it.
     */
    @Test
    public void testCostToDeployDapps() {
        for (Contract contract : Contract.values()) {
            System.out.println("-------------------------------------------------------");
            System.out.println("Results for deploying dApp: " + contract);
            AvmTransactionResult result = deployContract(contract);
            System.out.println("\tCost to deploy dApp = " + NumberFormat.getNumberInstance().format(result.getEnergyUsed()));
        }
    }

    //<-----------------------------------------helpers-------------------------------------------->

    /**
     * Returns the bytes that are to be deployed for the given contract.
     *
     * @param contract The contract whose bytes are to be returned.
     * @return The deployment bytes of the specified contract.
     */
    private byte[] getDeploymentJarBytesForContract(Contract contract) {
        byte[] jarBytes = null;
        switch (contract) {
            case BLAKE2B:
                jarBytes = classesToJarBytes(
                    Main.class,
                    Blake2b.class);
                break;
            case AION_BUFFER_PERF:
                jarBytes = classesToJarBytes(
                    AionBufferPerfContract.class,
                    AionBuffer.class);
                break;
            case POC_WALLET:
                jarBytes = classesToJarBytes(
                    Wallet.class,
                    Multiowned.class,
                    AionMap.class,
                    AionSet.class,
                    AionList.class,
                    ByteArrayWrapper.class,
                    Operation.class,
                    ByteArrayHelpers.class,
                    BytesKey.class,
                    RequireFailedException.class,
                    Daylimit.class,
                    EventLogger.class);
                break;
            case BASIC_PERF:
                jarBytes = classesToJarBytes(
                    BasicPerfContract.class,
                    AionList.class,
                    AionMap.class,
                    AionSet.class);
                break;
            case POC_EXCHANGE:
                jarBytes = classesToJarBytes(
                    ExchangeController.class,
                    Exchange.class,
                    ExchangeTransaction.class,
                    ByteArrayHelpers.class,
                    ERC20.class,
                    ERC20Token.class,
                    AionList.class,
                    AionSet.class,
                    AionMap.class);
                break;
            case ERC20:
                byte[] clinitArgs = ABIEncoder.encodeMethodArguments("", "Pepe".toCharArray(), "PEPE".toCharArray(), 8);
                jarBytes = classesToJarBytesWithClinitArgs(
                    clinitArgs,
                    CoinController.class,
                    ERC20.class,
                    ERC20Token.class,
                    AionList.class,
                    AionSet.class,
                    AionMap.class);
                break;
            case BASIC_APP:
                jarBytes = classesToJarBytes(
                    BasicAppTestTarget.class,
                    AionMap.class,
                    AionSet.class,
                    AionList.class);
                break;
            case TRS:
                jarBytes = classesToJarBytes(
                    TRS.class,
                    AionMap.class);
                break;
            default: RuntimeAssertionError.unreachable("This should never be reached.");
        }

        System.out.println("\tSize of dApp = " + NumberFormat.getNumberInstance().format(jarBytes.length) + " bytes");
        return jarBytes;
    }

    private AvmTransactionResult deployContract(Contract contract) {
        byte[] jar = getDeploymentJarBytesForContract(contract);
        Transaction transaction = Transaction.create(DEPLOYER, kernel.getNonce(DEPLOYER).longValue(), BigInteger.ZERO, jar,
            ENERGY_LIMIT, ENERGY_PRICE);
        TransactionContext txContext = new TransactionContextImpl(transaction, BLOCK);
        return (AvmTransactionResult) avm.run(new TransactionContext[] {txContext})[0].get();
    }

    private byte[] classesToJarBytes(Class<?> main, Class<?>... others) {
        return new CodeAndArguments(JarBuilder.buildJarForMainAndClasses(main, others), null).encodeToBytes();
    }

    private byte[] classesToJarBytesWithClinitArgs(byte[] clinitArgs, Class<?> main, Class<?>... others) {
        return new CodeAndArguments(JarBuilder.buildJarForMainAndClasses(main, others), clinitArgs).encodeToBytes();
    }

}
