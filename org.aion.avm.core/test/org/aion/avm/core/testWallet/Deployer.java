package org.aion.avm.core.testWallet;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.util.Assert;
import org.aion.avm.rt.Address;


/**
 * This is the first cut at a real application we can put on the AVM, based on the Solidity testWallet.
 * This is the entry-point we would register as our "main" class.  It is responsible for decoding the input
 * and determining what routine on the other components must be called to satisfy the request.
 * 
 * Original:  https://github.com/aionnetwork/aion_fastvm/blob/master/solidity/tests/contracts/testWallet.sol
 * 
 * NOTE:  This is not even ready to be used with in the AVM.  It is currently being built as a stand-alone
 * test application, just to make sure that the basics of the core algorithm are correct.
 * After that, it will be moved into a test on AvmImpl, where its special restrictions can be slowly removed
 * as that implementation becomes fleshed out.
 */
public class Deployer {
    public static void main(String[] args) {
        // This is eventually just a test harness to invoke the decode() but, for now, it will actually invoke the calls, directly.
        
        // First thing we do is create the Wallet (which requires its components).
        Address sender = buildAddress(1);
        Address extra1 = buildAddress(2);
        Address extra2 = buildAddress(3);
        TestLogger logger = new TestLogger();
        int requiredVotes = 2;
        Multiowned owners = new Multiowned(logger, sender, new Address[] {extra1, extra2}, requiredVotes);
        long dailyLimit = 5000;
        long startInDays = 1;
        Daylimit limit = new Daylimit(owners, dailyLimit, startInDays);
        Wallet wallet = new Wallet(logger, owners, limit);
        
        // First of all, just prove that we can send them some energy.
        Address paymentFrom = buildAddress(4);
        long paymendValue = 5;
        wallet.payable(paymentFrom, paymendValue);
        Assert.assertTrue(1 == logger.deposit);
        
        // Try to add an owner - we need to call this twice to see the event output: sender and extra1.
        Address newOwner = buildAddress(5);
        try {
            wallet.addOwner(new TestingRuntime(sender, new byte[] {5,6,42}), newOwner);
        } catch (RequireFailedException e) {
            // Expected.
        }
        Assert.assertTrue(0 == logger.ownerAdded);
        wallet.addOwner(new TestingRuntime(extra1, new byte[] {5,6,42}), newOwner);
        Assert.assertTrue(1 == logger.ownerAdded);
        
        // Send a normal transaction, which is under the limit, and observe that it goes through.
        Address transactionTo = buildAddress(6);
        long transactionSize = dailyLimit - 1;
        Assert.assertTrue(0 == logger.transactionUnderLimit);
        wallet.execute(new TestingRuntime(sender, new byte[] {5,6,42}), transactionTo, transactionSize, new byte[] {1});
        Assert.assertTrue(1 == logger.transactionUnderLimit);
        
        // Now, send another transaction, observe that it requires multisig confirmation, and confirm it with our new owner.
        Address confirmTransactionTo = buildAddress(7);
        Assert.assertTrue(0 == logger.confirmationNeeded);
        byte[] toConfirm = wallet.execute(new TestingRuntime(sender, new byte[] {5,6,42}), confirmTransactionTo, transactionSize, new byte[] {1});
        Assert.assertTrue(1 == logger.transactionUnderLimit);
        Assert.assertTrue(1 == logger.confirmationNeeded);
        boolean didConfirm = wallet.confirm(new TestingRuntime(newOwner, new byte[] {5,6,42}), toConfirm);
        Assert.assertTrue(didConfirm);
        
        // Change the count of required confirmations.
        try {
            wallet.changeRequirement(new TestingRuntime(sender, new byte[] {5,6,42}), 3);
        } catch (RequireFailedException e) {
            // Expected.
        }
        Assert.assertTrue(0 == logger.requirementChanged);
        wallet.changeRequirement(new TestingRuntime(extra1, new byte[] {5,6,42}), 3);
        Assert.assertTrue(1 == logger.requirementChanged);
        
        // Change the owner.
        Address lateOwner = buildAddress(8);
        Assert.assertTrue(wallet.getOwner(new TestingRuntime(lateOwner, new byte[] {5,6,42}), 0) == sender);
        try {
            wallet.changeOwner(new TestingRuntime(sender, new byte[] {5,6,42}), sender, lateOwner);
            Assert.assertTrue(false);
        } catch (RequireFailedException e) {
            // Expected.
        }
        try {
            wallet.changeOwner(new TestingRuntime(extra1, new byte[] {5,6,42}), sender, lateOwner);
            Assert.assertTrue(false);
        } catch (RequireFailedException e) {
            // Expected.
        }
        Assert.assertTrue(0 == logger.ownerChanged);
        wallet.changeOwner(new TestingRuntime(extra2, new byte[] {5,6,42}), sender, lateOwner);
        Assert.assertTrue(1 == logger.ownerChanged);
        
        // Try to remove an owner, but have someone revoke that so that it can't happen.
        try {
            wallet.removeOwner(new TestingRuntime(lateOwner, new byte[] {5,6,42}), extra1);
            Assert.assertTrue(false);
        } catch (RequireFailedException e) {
            // Expected.
        }
        try {
            wallet.removeOwner(new TestingRuntime(extra2, new byte[] {5,6,42}), extra1);
            Assert.assertTrue(false);
        } catch (RequireFailedException e) {
            // Expected.
        }
        Assert.assertTrue(0 == logger.revoke);
        wallet.revoke(new TestingRuntime(lateOwner, new byte[] {5,6,42}));
        Assert.assertTrue(1 == logger.revoke);
        try {
            // This fails since one of the owners revoked.
            wallet.removeOwner(new TestingRuntime(extra1, new byte[] {5,6,42}), extra1);
            Assert.assertTrue(false);
        } catch (RequireFailedException e) {
            // Expected.
        }
        Assert.assertTrue(0 == logger.ownerRemoved);
        // But this succeeds when they re-agree.
        wallet.removeOwner(new TestingRuntime(lateOwner, new byte[] {5,6,42}), extra1);
        Assert.assertTrue(1 == logger.ownerRemoved);
        Assert.assertTrue(wallet.getOwner(new TestingRuntime(extra1, new byte[] {5,6,42}), 0) == extra2);
    }

    public static byte[] decode(IFutureRuntime runtime, byte[] input) {
        Assert.unimplemented("Switch to this when we know the ABI the test should use");
        return null;
    }


    private static Address buildAddress(int fillByte) {
        byte[] raw = new byte[32];
        for (int i = 0; i < raw.length; ++ i) {
            raw[i] = (byte)fillByte;
        }
        return new Address(raw);
    }


    private static class TestingRuntime implements IFutureRuntime {
        private final Address sender;
        private final byte[] data;
        
        public TestingRuntime(Address sender, byte[] data) {
            this.sender = sender;
            this.data = data;
        }
        
        @Override
        public Address avm_getSender() {
            return this.sender;
        }
        @Override
        public Address avm_getAddress() {
            Assert.unimplemented("TODO");
            return null;
        }
        @Override
        public long avm_getEnergyLimit() {
            Assert.unimplemented("TODO");
            return 0;
        }
        @Override
        public ByteArray avm_getData() {
            Assert.unimplemented("TODO");
            return null;
        }
        @Override
        public ByteArray avm_getStorage(ByteArray key) {
            Assert.unimplemented("TODO");
            return null;
        }
        @Override
        public void avm_putStorage(ByteArray key, ByteArray value) {
            Assert.unimplemented("TODO");
        }
        @Override
        public long getBlockEpochSeconds() {
            // For now, always say it is day 1:  seconds per day.
            return 60 * 60 * 24;
        }
        @Override
        public byte[] getMessageData() {
            return data;
        }
        @Override
        public long getBlockNumber() {
            // For now, say that this is block 1.
            return 1;
        }
        @Override
        public byte[] sha3(byte[] data) {
            // For tests, we just return the initial data with a prefix.
            byte[] result = new byte[data.length + 1];
            result[0] = (byte)255;
            System.arraycopy(data, 0, result, 1, data.length);
            return result;
        }
        @Override
        public void selfDestruct(Address beneficiary) {
            Assert.unimplemented("TODO");
        }
        @Override
        public byte[] call(Address targetAddress, long energyToSend, byte[] payload) {
            // We probably want to capture/verify this more concretely but, for now, just return the payload to synthesize "something".
            return payload;
        }
    }


    /**
     * This is mostly just to give us a minimal representation of the defined events of the application and allow for counting events to verify tests.
     * I suspect that we will need to create a more generic logging system (event name and varargs) but there may be value in the user wrapping it in
     * something like this (depending on the debugging case, we may be able to sufficiently generalize the implementation).
     */
    private static class TestLogger implements IEventLogger {
        public int revoke;
        public int ownerChanged;
        public int ownerAdded;
        public int ownerRemoved;
        public int requirementChanged;
        public int deposit;
        public int transactionUnderLimit;
        public int confirmationNeeded;
        
        @Override
        public void revoke() {
            System.out.println("revoke");
            this.revoke += 1;
        }
        @Override
        public void ownerChanged() {
            System.out.println("ownerChanged");
            this.ownerChanged += 1;
        }
        @Override
        public void ownerAdded() {
            System.out.println("ownerAdded");
            this.ownerAdded += 1;
        }
        @Override
        public void ownerRemoved() {
            System.out.println("ownerRemoved");
            this.ownerRemoved += 1;
        }
        @Override
        public void requirementChanged() {
            System.out.println("requirementChanged");
            this.requirementChanged += 1;
        }
        @Override
        public void deposit() {
            System.out.println("deposit");
            this.deposit += 1;
        }
        @Override
        public void transactionUnderLimit() {
            System.out.println("transactionUnderLimit");
            this.transactionUnderLimit += 1;
        }
        @Override
        public void confirmationNeeded() {
            System.out.println("confirmationNeeded");
            this.confirmationNeeded += 1;
        }
    }
}
