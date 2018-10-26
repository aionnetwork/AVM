package org.aion.avm.core.bootstrapmethods;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.Avm;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;
import org.junit.Test;

/**
 * Tests that a contract that uses string concatenation and lambdas charges the same amount when
 * invoked multiple times.
 */
public class TestBootstrappingEnergyChargeConsistency {

    @Test
    public void testConsistencyOverMultipleInvocations() {
        KernelInterface kernel = new KernelInterfaceImpl();
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);
        Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
        byte[] deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
        long nonce = kernel.getNonce(deployer);

        // Deploy the contract.
        byte[] contractAddress = deployContract(avm, block, deployer, nonce);

        // Run the contract multiple times.
        TransactionResult result1 = runContract(avm, block, deployer, contractAddress, nonce + 1);
        TransactionResult result2 = runContract(avm, block, deployer, contractAddress, nonce + 2);
        TransactionResult result3 = runContract(avm, block, deployer, contractAddress, nonce + 3);
        avm.shutdown();

        // Compare the energy charges.
        long energy1 = result1.getEnergyUsed();
        long energy2 = result2.getEnergyUsed();
        long energy3 = result3.getEnergyUsed();

        assertEquals(energy1, energy2);
        assertEquals(energy2, energy3);
    }

    private byte[] deployContract(Avm avm, Block block, byte[] deployer, long nonce) {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(EnergyChargeConsistencyTarget.class);
        byte[] createData = new CodeAndArguments(jar, null).encodeToBytes();
        Transaction transaction = Transaction.create(
            deployer,
            nonce,
            BigInteger.ZERO,
            createData,
            6_000_000,
            1);
        TransactionContext context = new TransactionContextImpl(transaction, block);
        return avm.run(new TransactionContext[] {context})[0].get().getReturnData();
    }

    private TransactionResult runContract(Avm avm, Block block, byte[] sender, byte[] contract, long nonce) {
        byte[] callData = ABIEncoder.encodeMethodArguments("run");
        Transaction transaction = Transaction.call(
            sender,
            contract,
            nonce,
            BigInteger.ZERO,
            callData,
            6_000_000,
            1);
        TransactionContext context = new TransactionContextImpl(transaction, block);
        return avm.run(new TransactionContext[] {context})[0].get();
    }

}
