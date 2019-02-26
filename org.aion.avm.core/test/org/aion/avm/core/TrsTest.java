package org.aion.avm.core;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.poc.TRS;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionMap;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.Address;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class TrsTest {
    private static final Address DEPLOYER = KernelInterfaceImpl.PREMINED_ADDRESS;
    private static final long ENERGY_LIMIT = 100_000_000_000L;
    private static final long ENERGY_PRICE = 1;
    private static final int NUM_PERIODS = 12;

    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    private KernelInterfaceImpl kernel;
    private AvmImpl avm;
    private Address contract;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new StandardCapabilities(), new AvmConfiguration());
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
        this.avm = null;
        this.kernel = null;
    }

    @Test
    public void testDeployTrs() {
        assertTrue(deployContract().getResultCode().isSuccess());
    }

    /**
     * Tests the typical basic use case of the TRS contract:
     *
     * 1. deploy contract
     * 2. initialize contract
     * 3. fund the contract
     * 4. mint an account to the contract
     * 5. lock the contract
     * 6. start the contract
     * 7. make withdrawals in each period
     */
    @Test
    public void testTrs() {
        BigInteger basicBalance = BigInteger.valueOf(1_000_000_000_000L);
        BigInteger trsFunds = BigInteger.valueOf(100_000);
        Address account = Helpers.randomAddress();

        // Deploy and initialize the contract.
        assertTrue(deployContract().getResultCode().isSuccess());
        assertTrue(initializeTrs().getResultCode().isSuccess());

        // Send funds to the contract and verify they were received.
        assertTrue(sendFundsToTrs(trsFunds).getResultCode().isSuccess());
        assertEquals(trsFunds, kernel.getBalance(contract));

        // Mint an account into the contract so that it gets all of the funds.
        assertTrue(mintAccountToTrs(account, trsFunds).getResultCode().isSuccess());

        // Lock and start the contract.
        assertTrue(lockTrs().getResultCode().isSuccess());
        assertTrue(startTrs().getResultCode().isSuccess());

        // Give account some basic balance so that it is able to make withdrawals.
        assertTrue(sendFundsTo(account, basicBalance).getResultCode().isSuccess());
        assertEquals(basicBalance, kernel.getBalance(account));

        // Step through each period in the contract and withdraw the funds.
        BigInteger accountBalance = basicBalance;

        for (int i = 0; i < NUM_PERIODS; i++) {
            TransactionResult result = withdrawFromTrs(account);

            // Check the transaction was successful.
            assertTrue(result.getResultCode().isSuccess());

            // Check the return value is true, indicating a non-zero withdrawal amount.
            assertTrue((boolean) ABIDecoder.decodeOneObject(result.getReturnData()));

            // Update the account balance by deducting the transaction cost from the previous balance.
            long callCost = ((AvmTransactionResult) result).getEnergyUsed() * ENERGY_PRICE;
            accountBalance = accountBalance.subtract(BigInteger.valueOf(callCost));

            // Move into next period.
            moveIntoNextPeriod();
        }

        // Check that the total amount of funds from the trs contract have been claimed.
        assertEquals(accountBalance.add(trsFunds), kernel.getBalance(account));
        assertEquals(BigInteger.ZERO, kernel.getBalance(contract));
    }

    /**
     * The period is determined by the current block timestamp. This method replaces block with a
     * new block whose timestamp has been moved ahead in time so that it is now in the next period.
     */
    private void moveIntoNextPeriod() {
        long previousBlockTime = block.getTimestamp();
        long secondsPerPeriod = TRS.intervalSecs;
        long blocktimeForNextPeriod = previousBlockTime + secondsPerPeriod;

        block = new Block(block.getPrevHash(), block.getNumber(), block.getCoinbase(), blocktimeForNextPeriod, block.getData());
    }

    private TransactionResult sendFundsToTrs(BigInteger amount) {
        return sendFundsTo(contract, amount);
    }

    private TransactionResult sendFundsTo(Address recipient, BigInteger amount) {
        Transaction callTransaction = Transaction.call(DEPLOYER, recipient, kernel.getNonce(DEPLOYER), amount, new byte[0], ENERGY_LIMIT, ENERGY_PRICE);
        TransactionContext callContext = TransactionContextImpl.forExternalTransaction(callTransaction, block);
        return avm.run(this.kernel, new TransactionContext[] {callContext})[0].get();
    }

    private TransactionResult mintAccountToTrs(Address account, BigInteger amount) {
        return callContract("mint", new org.aion.avm.api.Address(account.toBytes()), amount.longValue());
    }

    private TransactionResult withdrawFromTrs(Address recipient) {
        return callContract(recipient, "withdraw");
    }

    private TransactionResult startTrs() {
        return callContract("start", block.getTimestamp());
    }

    private TransactionResult lockTrs() {
        return callContract("lock");
    }

    private TransactionResult initializeTrs() {
        return callContract("init", NUM_PERIODS, 0);
    }

    private TransactionResult callContract(String method, Object... parameters) {
        return callContract(DEPLOYER, method, parameters);
    }

    private TransactionResult callContract(Address sender, String method, Object... parameters) {
        byte[] callData = ABIEncoder.encodeMethodArguments(method, parameters);
        Transaction callTransaction = Transaction.call(sender, contract, kernel.getNonce(sender), BigInteger.ZERO, callData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionContext callContext = TransactionContextImpl.forExternalTransaction(callTransaction, block);
        return avm.run(this.kernel, new TransactionContext[] {callContext})[0].get();
    }

    private TransactionResult deployContract() {
        byte[] jarBytes = new CodeAndArguments(JarBuilder.buildJarForMainAndClasses(TRS.class, AionMap.class), null).encodeToBytes();
        Transaction transaction = Transaction.create(DEPLOYER, kernel.getNonce(DEPLOYER), BigInteger.ZERO, jarBytes, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionContext context = TransactionContextImpl.forExternalTransaction(transaction, block);
        TransactionResult result = avm.run(this.kernel, new TransactionContext[] {context})[0].get();
        contract = AvmAddress.wrap(result.getReturnData());
        return result;
    }

}
