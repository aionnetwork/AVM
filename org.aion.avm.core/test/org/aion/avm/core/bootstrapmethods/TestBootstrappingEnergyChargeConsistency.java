package org.aion.avm.core.bootstrapmethods;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.Avm;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.AvmTransactionResult.Code;
import org.aion.vm.api.interfaces.KernelInterface;
import org.junit.Test;

/**
 * Tests that a contract that uses string concatenation and lambdas charges the same amount when
 * invoked multiple times.
 */
public class TestBootstrappingEnergyChargeConsistency {

    @Test
    public void testConsistencyOverMultipleInvocations() {
        KernelInterface kernel = new KernelInterfaceImpl();
        Avm avm = CommonAvmFactory.buildAvmInstance(kernel);
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        org.aion.vm.api.interfaces.Address deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
        long nonce = kernel.getNonce(deployer).longValue();

        // Deploy the contract.
        org.aion.vm.api.interfaces.Address contractAddress = deployContract(avm, block, deployer, nonce);

        // Run the contract multiple times.
        AvmTransactionResult result1 = runContract(avm, block, deployer, contractAddress, nonce + 1);
        AvmTransactionResult result2 = runContract(avm, block, deployer, contractAddress, nonce + 2);
        AvmTransactionResult result3 = runContract(avm, block, deployer, contractAddress, nonce + 3);
        avm.shutdown();

        // Ensure the calls were all successful.
        assertEquals(Code.SUCCESS, result1.getResultCode());
        assertEquals(Code.SUCCESS, result2.getResultCode());
        assertEquals(Code.SUCCESS, result3.getResultCode());

        // Compare the energy charges.
        long energy1 = result1.getEnergyUsed();
        long energy2 = result2.getEnergyUsed();
        long energy3 = result3.getEnergyUsed();

        assertEquals(energy1, energy2);
        assertEquals(energy2, energy3);
    }

    private org.aion.vm.api.interfaces.Address deployContract(Avm avm, Block block, org.aion.vm.api.interfaces.Address deployer, long nonce) {
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
        return AvmAddress.wrap(avm.run(new TransactionContext[] {context})[0].get().getReturnData());
    }

    private AvmTransactionResult runContract(Avm avm, Block block, org.aion.vm.api.interfaces.Address sender, org.aion.vm.api.interfaces.Address contract, long nonce) {
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
