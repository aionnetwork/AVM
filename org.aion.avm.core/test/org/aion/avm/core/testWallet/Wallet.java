package org.aion.avm.core.testWallet;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.AionMap;


/**
 * In the original, Wallet "inherited" from multisig, multiowned, and daylimit but the Solidity concept of "inheritance" probably makes more
 * sense as strict composition so we will just depend on someone creating this object with a pre-configured instances and implementing the
 * interfaces.
 */
public class Wallet implements IMultisig {
    private final EventLogger logger;
    private final Multiowned owners;
    private final Daylimit limit;
    // Note that this key is really just a subset of uses of "Operation".
    private final AionMap<BytesKey, Transaction> transactions;

    public Wallet(EventLogger logger, Multiowned owners, Daylimit limit) {
        this.logger = logger;
        this.owners = owners;
        this.limit = limit;
        this.transactions = new AionMap<>();
    }

    // EXTERNAL - composed
    public void revoke(BlockchainRuntime runtime) {
        this.owners.revoke(runtime);
    }

    // EXTERNAL - composed
    public void addOwner(BlockchainRuntime runtime, Address owner) {
        this.owners.addOwner(runtime, owner);
    }

    // EXTERNAL - composed
    public void removeOwner(BlockchainRuntime runtime, Address owner) {
        this.owners.removeOwner(runtime, owner);
    }

    // EXTERNAL - composed
    public void changeRequirement(BlockchainRuntime runtime, int newRequired) {
        this.owners.changeRequirement(runtime, newRequired);
    }

    // EXTERNAL - composed
    public Address getOwner(BlockchainRuntime runtime, int ownerIndex) {
        return this.owners.getOwner(ownerIndex);
    }

    // EXTERNAL - composed
    public void setDailyLimit(BlockchainRuntime runtime, long value) {
        this.limit.setDailyLimit(runtime, value);
    }

    // EXTERNAL - composed
    public void resetSpentToday(BlockchainRuntime runtime) {
        this.limit.resetSpentToday(runtime);
    }

    // EXTERNAL
    public void kill(BlockchainRuntime runtime, Address to) {
        // (modifier)
        this.owners.onlyManyOwners(runtime.getSender(), Operation.fromMessage(runtime));
        
        runtime.selfDestruct(to);
    }

    // gets called when no other function matches
    public void payable(Address from, long value) {
        if (value > 0) {
            this.logger.deposit();
        }
    }

    // EXTERNAL
    // Outside-visible transact entry point. Executes transaction immediately if below daily spend limit.
    // If not, goes into multisig process. We provide a hash on return to allow the sender to provide
    // shortcuts for the other confirmations (allowing them to avoid replicating the _to, _value
    // and _data arguments). They still get the option of using them if they want, anyways.
    @Override
    public byte[] execute(BlockchainRuntime runtime, Address to, long value, byte[] data) {
        // (modifier)
        this.owners.onlyOwner(runtime.getSender());
        
        byte[] result = null;
        // first, take the opportunity to check that we're under the daily limit.
        if (this.limit.underLimit(runtime, value)) {
            this.logger.singleTransact();
            // yes - just execute the call.
            byte[] response = runtime.call(to, value, data);
            if (null == response) {
                throw new RequireFailedException();
            }
            // Returns nothing special.
            result = null;
        } else {
            // determine our operation hash.
            result = Operation.rawOperationForCurrentMessageAndBlock(runtime);
            BytesKey transactionKey = BytesKey.from(result);
            if (!safeConfirm(runtime, result) && (null == this.transactions.get(transactionKey))) {
                Transaction transaction = new Transaction();
                transaction.to = to;
                transaction.value = value;
                transaction.data = data;
                this.transactions.put(transactionKey, transaction);
                this.logger.confirmationNeeded();
            }
        }
        return result;
    }

    // TODO:  Determine if this is the correct emulation of semantics.  The Solidity test seems to act like the exception is the same as "return false".
    private boolean safeConfirm(BlockchainRuntime runtime, byte[] h) {
        boolean result = false;
        try {
            result = confirm(runtime, h);
        } catch (RequireFailedException e) {
            result = false;
        }
        return result;
    }

    @Override
    public void changeOwner(BlockchainRuntime runtime, Address from, Address to) {
        this.owners.changeOwner(runtime, from, to);
    }

    // confirm a transaction through just the hash. we use the previous transactions map, m_txs, in order
    // to determine the body of the transaction from the hash provided.
    @Override
    public boolean confirm(BlockchainRuntime runtime, byte[] h) {
        // (modifier)
        this.owners.onlyManyOwners(runtime.getSender(), Operation.fromBytes(h));
        
        boolean result = false;
        BytesKey key = BytesKey.from(h);
        if (null != this.transactions.get(key).to) {
            Transaction transaction = this.transactions.get(key);
            byte[] response = runtime.call(transaction.to, transaction.value, transaction.data);
            if (null == response) {
                throw new RequireFailedException();
            }
            this.logger.multiTransact();
            this.transactions.remove(BytesKey.from(h));
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
