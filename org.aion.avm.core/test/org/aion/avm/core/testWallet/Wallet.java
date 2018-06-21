package org.aion.avm.core.testWallet;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.AionMap;


/**
 * In the original, Wallet "inherited" from multisig, multiowned, and daylimit but the Solidity concept of "inheritance" probably makes more
 * sense as strict composition so we will just depend on someone creating this object with a pre-configured instances and implementing the
 * interfaces.
 */
public class Wallet {
    /**
     * Calling a reflection routine which has an array in it is a problem with how our arraywrapping works.
     * This might not matter, in the future, but may be something we need to make easier to manage (maybe the arraywrappers have factories, or something).
     */
    public static void avoidArrayWrappingFactory(BlockchainRuntime runtime, Address owner1, Address owner2, int votesRequiredPerOperation, long daylimit) {
        Wallet.init(runtime, new Address[] {owner1, owner2}, votesRequiredPerOperation, daylimit);
    }

    /**
     * This is a helper we use to invoke the init() method given that our arraywrapping makes creating the Address[] problematic.
     * This method acts as a factory for us.
     */
    public static Address[] wrapTwoAddresses(Address owner1, Address owner2) {
        return new Address[] {owner1, owner2};
    }


    // Note that this key is really just a subset of uses of "Operation".
    private static AionMap<BytesKey, Transaction> transactions;

    // The contract "Constructor".  Note that this should only be called once, when initially deployed (in Ethereum world, the arguments are
    // just pass as part of the deployment payload, after the code).
    public static void init(BlockchainRuntime runtime, Address[] requestedOwners, int votesRequiredPerOperation, long daylimit) {
        // This is the contract entry-point so "construct" the contract fragments from which we are derived.
        Address sender = runtime.getSender();
        long nowInSeconds = runtime.getBlockEpochSeconds();
        long nowInDays = nowInSeconds / (24 * 60 * 60);
        Multiowned.init(sender, requestedOwners, votesRequiredPerOperation);
        Daylimit.init(daylimit, nowInDays);
        
        Wallet.transactions = new AionMap<>();
    }

    // EXTERNAL - composed
    public static void revoke(BlockchainRuntime runtime) {
        Multiowned.revoke(runtime);
    }

    // EXTERNAL - composed
    public static void addOwner(BlockchainRuntime runtime, Address owner) {
        Multiowned.addOwner(runtime, owner);
    }

    // EXTERNAL - composed
    public static void removeOwner(BlockchainRuntime runtime, Address owner) {
        Multiowned.removeOwner(runtime, owner);
    }

    // EXTERNAL - composed
    public static void changeRequirement(BlockchainRuntime runtime, int newRequired) {
        Multiowned.changeRequirement(runtime, newRequired);
    }

    // EXTERNAL - composed
    public static Address getOwner(BlockchainRuntime runtime, int ownerIndex) {
        return Multiowned.getOwner(ownerIndex);
    }

    // EXTERNAL - composed
    public static void setDailyLimit(BlockchainRuntime runtime, long value) {
        Daylimit.setDailyLimit(runtime, value);
    }

    // EXTERNAL - composed
    public static void resetSpentToday(BlockchainRuntime runtime) {
        Daylimit.resetSpentToday(runtime);
    }

    // EXTERNAL
    public static void kill(BlockchainRuntime runtime, Address to) {
        // (modifier)
        Multiowned.onlyManyOwners(runtime.getSender(), Operation.fromMessage(runtime));
        
        runtime.selfDestruct(to);
    }

    // gets called when no other function matches
    public static void payable(Address from, long value) {
        if (value > 0) {
            EventLogger.deposit(from, value);
        }
    }

    // EXTERNAL
    // Outside-visible transact entry point. Executes transaction immediately if below daily spend limit.
    // If not, goes into multisig process. We provide a hash on return to allow the sender to provide
    // shortcuts for the other confirmations (allowing them to avoid replicating the _to, _value
    // and _data arguments). They still get the option of using them if they want, anyways.
    public static byte[] execute(BlockchainRuntime runtime, Address to, long value, byte[] data) {
        // (modifier)
        Multiowned.onlyOwner(runtime.getSender());
        
        byte[] result = null;
        // first, take the opportunity to check that we're under the daily limit.
        if (Daylimit.underLimit(runtime, value)) {
            EventLogger.singleTransact(runtime.getSender(), value, to, data);
            // yes - just execute the call.
            byte[] response = runtime.call(to, null, data, value);
            if (null == response) {
                throw new RequireFailedException();
            }
            // Returns nothing special.
            result = null;
        } else {
            // determine our operation hash.
            result = Operation.rawOperationForCurrentMessageAndBlock(runtime);
            BytesKey transactionKey = BytesKey.from(result);
            if (!safeConfirm(runtime, result) && (null == Wallet.transactions.get(transactionKey))) {
                Transaction transaction = new Transaction();
                transaction.to = to;
                transaction.value = value;
                transaction.data = data;
                Wallet.transactions.put(transactionKey, transaction);
                EventLogger.confirmationNeeded(Operation.fromBytes(result), runtime.getSender(), value, to, data);
            }
        }
        return result;
    }

    // TODO:  Determine if this is the correct emulation of semantics.  The Solidity test seems to act like the exception is the same as "return false".
    private static boolean safeConfirm(BlockchainRuntime runtime, byte[] h) {
        boolean result = false;
        try {
            result = confirm(runtime, h);
        } catch (RequireFailedException e) {
            result = false;
        }
        return result;
    }

    public static void changeOwner(BlockchainRuntime runtime, Address from, Address to) {
        Multiowned.changeOwner(runtime, from, to);
    }

    // confirm a transaction through just the hash. we use the previous transactions map, m_txs, in order
    // to determine the body of the transaction from the hash provided.
    public static boolean confirm(BlockchainRuntime runtime, byte[] h) {
        // (modifier)
        Multiowned.onlyManyOwners(runtime.getSender(), Operation.fromBytes(h));
        
        boolean result = false;
        BytesKey key = BytesKey.from(h);
        if (null != Wallet.transactions.get(key).to) {
            Transaction transaction = Wallet.transactions.get(key);
            byte[] response = runtime.call(transaction.to, null, transaction.data, transaction.value);
            if (null == response) {
                throw new RequireFailedException();
            }
            EventLogger.multiTransact(runtime.getSender(), Operation.fromBytes(h), transaction.value, transaction.to, transaction.data);
            Wallet.transactions.remove(BytesKey.from(h));
            result = true;
        }
        return result;
    }


    // Transaction structure to remember details of transaction lest it need be saved for a later call.
    // (this is public just for easy referencing in the Deployer's loader logic).
    public static class Transaction {
        public Address to;
        public long value;
        public byte[] data;
    }
}
