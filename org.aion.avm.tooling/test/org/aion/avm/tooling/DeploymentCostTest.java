package org.aion.avm.tooling;

import avm.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.tooling.poc.AionBufferPerfContract;
import org.aion.avm.tooling.poc.TRS;
import org.aion.avm.tooling.testExchange.*;
import org.aion.avm.userlib.AionBuffer;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.kernel.AvmTransactionResult;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * This suite is really just for viewing the deployment costs of some of our various Dapp examples.
 * Nothing explicitly is actually verified by these 'tests'.
 * The purpose is more to give us an idea about how our deployment costs look for different Dapps.
 */
public class DeploymentCostTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static final long ENERGY_LIMIT = 100_000_000_000L;
    private static final long ENERGY_PRICE = 1L;
    private static final Address DEPLOYER = avmRule.getPreminedAccount();


    // NOTE: To add a new dApp to this test simply do the following:
    // 1. Create a Contract enum for it and put a String representation into contractsAsStrings
    // 2. Inside getDeploymentJarBytesForContract() create a new case for this enum and grab the
    //    jar bytes for all of the classes involved in creating the dApp.
    //    There are examples for dApps with & without clinit args.

    /**
     * The contracts/dApps we use in the deployment cost test.
     */
    private enum Contract {
        AION_BUFFER_PERF,
        BASIC_PERF,
        POC_EXCHANGE,
        ERC20,
        BASIC_APP,
        TRS,
        ;

        private static Map<Contract, String> contractsAsStrings = new HashMap<>();

        static {
            contractsAsStrings.put(AION_BUFFER_PERF, "AionBufferPerfContract");
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
            case AION_BUFFER_PERF:
                jarBytes = classesToJarBytes(
                    AionBufferPerfContract.class,
                    AionBuffer.class);
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
                    ERC20Token.class,
                    AionList.class,
                    AionSet.class,
                    AionMap.class);
                break;
            case ERC20:
                byte[] clinitArgs = ABIUtil.encodeMethodArguments("", "Pepe".toCharArray(), "PEPE".toCharArray(), 8);
                jarBytes = classesToJarBytesWithClinitArgs(
                    clinitArgs,
                    CoinController.class,
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
            default: throw new AssertionError("This should never be reached.");
        }

        System.out.println("\tSize of dApp = " + NumberFormat.getNumberInstance().format(jarBytes.length) + " bytes");
        return jarBytes;
    }

    private AvmTransactionResult deployContract(Contract contract) {
        byte[] jar = getDeploymentJarBytesForContract(contract);
        return (AvmTransactionResult) avmRule.deploy(DEPLOYER, BigInteger.ZERO, jar, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
    }

    private byte[] classesToJarBytes(Class<?> main, Class<?>... others) {
        return new CodeAndArguments(JarBuilder.buildJarForMainAndClasses(main, others), null).encodeToBytes();
    }

    private byte[] classesToJarBytesWithClinitArgs(byte[] clinitArgs, Class<?> main, Class<?>... others) {
        return new CodeAndArguments(JarBuilder.buildJarForMainAndClasses(main, others), clinitArgs).encodeToBytes();
    }

}
