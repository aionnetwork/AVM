package org.aion.avm.embed;

import avm.Address;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.embed.poc.AionBufferPerfContract;
import org.aion.avm.embed.poc.TRS;
import org.aion.avm.embed.testExchange.*;
import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.userlib.AionBuffer;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.types.TransactionResult;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;


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
    }

    /**
     * Verifies the DApp deployment costs don't change unexpectedly.
     */
    @Test
    public void testCostToDeployDapps() {
        TransactionResult result = deployContract(Contract.AION_BUFFER_PERF);
        Assert.assertEquals(100_000_000_000L, result.energyUsed);
        
        result = deployContract(Contract.BASIC_PERF);
        Assert.assertEquals(2_062_419L, result.energyUsed);
        
        result = deployContract(Contract.POC_EXCHANGE);
        Assert.assertEquals(100_000_000_000L, result.energyUsed);
        
        result = deployContract(Contract.ERC20);
        Assert.assertEquals(100_000_000_000L, result.energyUsed);
        
        result = deployContract(Contract.BASIC_APP);
        Assert.assertEquals(100_000_000_000L, result.energyUsed);
        
        result = deployContract(Contract.TRS);
        Assert.assertEquals(100_000_000_000L, result.energyUsed);
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
                // Verify that this size doesn't unexpectedly change.
                Assert.assertEquals(7_764L, jarBytes.length);
                break;
            case BASIC_PERF:
                jarBytes = classesToJarBytes(
                    BasicPerfContract.class,
                    AionList.class,
                    AionMap.class,
                    AionSet.class);
                // Verify that this size doesn't unexpectedly change.
                Assert.assertEquals(28_732L, jarBytes.length);
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
                // Verify that this size doesn't unexpectedly change.
                Assert.assertEquals(32_573L, jarBytes.length);
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
                // Verify that this size doesn't unexpectedly change.
                Assert.assertEquals(28_012L, jarBytes.length);
                break;
            case BASIC_APP:
                jarBytes = classesToJarBytes(
                    BasicAppTestTarget.class,
                    AionMap.class,
                    AionSet.class,
                    AionList.class);
                // Verify that this size doesn't unexpectedly change.
                Assert.assertEquals(26_106L, jarBytes.length);
                break;
            case TRS:
                jarBytes = classesToJarBytes(
                    TRS.class,
                    AionMap.class);
                // Verify that this size doesn't unexpectedly change.
                Assert.assertEquals(16_090L, jarBytes.length);
                break;
            default: throw new AssertionError("This should never be reached.");
        }

        return jarBytes;
    }

    private TransactionResult deployContract(Contract contract) {
        byte[] jar = getDeploymentJarBytesForContract(contract);
        return avmRule.deploy(DEPLOYER, BigInteger.ZERO, jar, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
    }

    private byte[] classesToJarBytes(Class<?> main, Class<?>... others) {
        return new CodeAndArguments(UserlibJarBuilder.buildJarForMainAndClasses(main, others), null).encodeToBytes();
    }

    private byte[] classesToJarBytesWithClinitArgs(byte[] clinitArgs, Class<?> main, Class<?>... others) {
        return new CodeAndArguments(UserlibJarBuilder.buildJarForMainAndClasses(main, others), clinitArgs).encodeToBytes();
    }

}
