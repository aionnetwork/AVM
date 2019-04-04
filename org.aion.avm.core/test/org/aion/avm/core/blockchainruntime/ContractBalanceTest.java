package org.aion.avm.core.blockchainruntime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import avm.Blockchain;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.RedirectContract;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.Block;
import org.aion.kernel.TestingKernel;
import org.aion.kernel.Transaction;
import org.aion.types.Address;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the {@link Blockchain#getBalanceOfThisContract()} method for retrieving the balance
 * of a deployed contract from within that contract.
 */
public class ContractBalanceTest {
    private static Address from = TestingKernel.PREMINED_ADDRESS;
    private static long energyLimit = 10_000_000L;
    private static long energyPrice = 5;
    private static Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    private static KernelInterface kernel;
    private static AvmImpl avm;

    @BeforeClass
    public static void setup() {
        kernel = new TestingKernel(block);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void testClinitBalanceWhenTransferringZero() {
        Address contract = deployContract(BigInteger.ZERO);
        BigInteger actualBalance = callContractToGetClinitBalance(contract);
        assertEquals(BigInteger.ZERO, actualBalance);
    }

    @Test
    public void testClinitBalanceWhenTransferringPositiveAmount() {
        BigInteger transferAmount = BigInteger.valueOf(1234567);
        Address contract = deployContract(transferAmount);
        BigInteger actualBalance = callContractToGetClinitBalance(contract);
        assertEquals(transferAmount, actualBalance);
    }

    @Test
    public void testContractBalance() {
        Address contract = deployContract(BigInteger.ZERO);

        // Contract currently has no balance.
        BigInteger balance = callContractToGetItsBalance(contract);
        assertEquals(BigInteger.ZERO, balance);

        // Increase the contract balance and check the amount.
        BigInteger delta1 = BigInteger.TWO.pow(250);
        kernel.adjustBalance(contract, delta1);
        balance = callContractToGetItsBalance(contract);
        assertEquals(delta1, balance);

        // Decrease the contract balance and check the amount.
        BigInteger delta2 = BigInteger.TWO.pow(84).negate();
        kernel.adjustBalance(contract, delta2);
        balance = callContractToGetItsBalance(contract);
        assertEquals(delta1.add(delta2), balance);
    }

    @Test
    public void testContractBalanceViaInternalTransaction() {
        Address balanceContract = deployContract(BigInteger.ZERO);
        Address redirectContract = deployRedirectContract();

        // We give the redirect contract some balance to ensure we aren't querying the wrong contract.
        kernel.adjustBalance(redirectContract, BigInteger.valueOf(2938752));

        // Contract currently has no balance.
        BigInteger balance = callContractToGetItsBalanceViaRedirectContract(redirectContract, balanceContract);
        assertEquals(BigInteger.ZERO, balance);
    }

    /**
     * Deploys the contract and transfers value amount of Aion into it.
     */
    private Address deployContract(BigInteger value) {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(ContractBalanceTarget.class);
        jar = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        Transaction transaction = Transaction.create(from, kernel.getNonce(from), value, jar, energyLimit, energyPrice);
        TransactionResult result = avm.run(ContractBalanceTest.kernel, new Transaction[] {transaction})[0].get();
        assertTrue(result.getResultCode().isSuccess());
        return Address.wrap(result.getReturnData());
    }

    private BigInteger callContractToGetItsBalance(Address contract) {
        byte[] callData = ABIUtil.encodeMethodArguments("getBalanceOfThisContract");
        Transaction transaction = Transaction.call(from, contract, kernel.getNonce(from), BigInteger.ZERO, callData, energyLimit, energyPrice);
        TransactionResult result = avm.run(ContractBalanceTest.kernel, new Transaction[] {transaction})[0].get();
        assertTrue(result.getResultCode().isSuccess());
        return new BigInteger((byte[]) ABIUtil.decodeOneObject(result.getReturnData()));
    }

    private BigInteger callContractToGetClinitBalance(Address contract) {
        byte[] callData = ABIUtil.encodeMethodArguments("getBalanceOfThisContractDuringClinit");
        Transaction transaction = Transaction.call(from, contract, kernel.getNonce(from), BigInteger.ZERO, callData, energyLimit, energyPrice);
        TransactionResult result = avm.run(ContractBalanceTest.kernel, new Transaction[] {transaction})[0].get();
        assertTrue(result.getResultCode().isSuccess());
        return new BigInteger((byte[]) ABIUtil.decodeOneObject(result.getReturnData()));
    }

    private Address deployRedirectContract() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(RedirectContract.class);
        jar = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        Transaction transaction = Transaction.create(from, kernel.getNonce(from), BigInteger.ZERO, jar, energyLimit, energyPrice);
        TransactionResult result = avm.run(ContractBalanceTest.kernel, new Transaction[] {transaction})[0].get();
        assertTrue(result.getResultCode().isSuccess());
        return Address.wrap(result.getReturnData());
    }

    private BigInteger callContractToGetItsBalanceViaRedirectContract(Address redirectContract, Address balanceContract) {
        avm.Address contract = getContractAsAbiAddress(balanceContract);
        byte[] args = ABIUtil.encodeMethodArguments("getBalanceOfThisContract");
        byte[] callData = ABIUtil.encodeMethodArguments("callOtherContractAndRequireItIsSuccess", contract, 0L, args);
        return runTransactionAndInterpretOutputAsBigInteger(redirectContract, callData);
    }

    private BigInteger runTransactionAndInterpretOutputAsBigInteger(Address contract, byte[] callData) {
        Transaction transaction = Transaction.call(from, contract, kernel.getNonce(from), BigInteger.ZERO, callData, energyLimit, energyPrice);
        TransactionResult result = avm.run(ContractBalanceTest.kernel, new Transaction[] {transaction})[0].get();
        assertTrue(result.getResultCode().isSuccess());
        return new BigInteger((byte[]) ABIUtil.decodeOneObject(result.getReturnData()));
    }

    private avm.Address getContractAsAbiAddress(Address contract) {
        return new avm.Address(contract.toBytes());
    }

}
