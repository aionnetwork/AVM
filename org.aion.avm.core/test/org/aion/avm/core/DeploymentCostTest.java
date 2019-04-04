package org.aion.avm.core;

import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.testBlake2b.Blake2b;
import org.aion.avm.core.testBlake2b.Main;
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
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.TestingKernel;
import org.aion.kernel.Transaction;
import org.aion.types.Address;
import org.aion.vm.api.interfaces.TransactionResult;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
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
    private static final Address DEPLOYER = TestingKernel.PREMINED_ADDRESS;

    private Block block;
    private TestingKernel kernel;
    private AvmImpl avm;

    @Before
    public void setup() {
        this.block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        this.kernel = new TestingKernel(this.block);
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }


    // NOTE: To add a new dApp to this test simply do the following:
    // 1. Create a Contract enum for it and put a String representation into contractsAsStrings
    // 2. Inside getDeploymentJarBytesForContract() create a new case for this enum and grab the
    //    jar bytes for all of the classes involved in creating the dApp.
    //    There are examples for dApps with & without clinit args.

    /**
     * The contracts/dApps we use in the deployment cost test.
     */
    private enum Contract {
        BLAKE2B,
        POC_WALLET,
        ;

        private static Map<Contract, String> contractsAsStrings = new HashMap<>();

        static {
            contractsAsStrings.put(BLAKE2B, "Blake2b");
            contractsAsStrings.put(POC_WALLET, "PocWallet");
        }

        @Override
        public String toString() {
            return contractsAsStrings.get(this);
        }

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
            case POC_WALLET:
                jarBytes = classesToJarBytes(
                    Wallet.class,
                    Multiowned.class,
                    ByteArrayWrapper.class,
                    Operation.class,
                    ByteArrayHelpers.class,
                    BytesKey.class,
                    RequireFailedException.class,
                    Daylimit.class,
                    EventLogger.class);
                break;
            default: RuntimeAssertionError.unreachable("This should never be reached.");
        }

        System.out.println("\tSize of dApp = " + NumberFormat.getNumberInstance().format(jarBytes.length) + " bytes");
        return jarBytes;
    }

    private AvmTransactionResult deployContract(Contract contract) {
        byte[] jar = getDeploymentJarBytesForContract(contract);

        //deploy in normal Mode
        Transaction create = Transaction.create(DEPLOYER, this.kernel.getNonce(DEPLOYER), BigInteger.ZERO, jar, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult createResult = this.avm.run(this.kernel, new Transaction[] {create})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        return (AvmTransactionResult)createResult;
    }

    private byte[] classesToJarBytes(Class<?> main, Class<?>... others) {
        return new CodeAndArguments(JarBuilder.buildJarForMainAndClassesAndUserlib(main, others), null).encodeToBytes();
    }
}
