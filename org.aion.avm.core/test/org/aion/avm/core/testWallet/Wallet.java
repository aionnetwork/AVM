package org.aion.avm.core.testWallet;

import java.util.HashMap;
import java.util.Map;

import org.aion.avm.rt.Address;


/**
 * In the original, Wallet "inherited" from multisig, multiowned, and daylimit but the Solidity concept of "inheritance" probably makes more
 * sense as strict composition so we will just depend on someone creating this object with a pre-configured instances and implementing the
 * interfaces.
 */
public class Wallet implements IMultisig {
    private final IEventLogger logger;
    private final Multiowned owners;
    private final Daylimit limit;
    private final Map<BytesKey, Transaction> transactions;

    public Wallet(IEventLogger logger, Multiowned owners, Daylimit limit) {
        this.logger = logger;
        this.owners = owners;
        this.limit = limit;
        this.transactions = new HashMap<>();
    }

    // EXTERNAL - composed
    public void revoke(IFutureRuntime runtime) {
        this.owners.revoke(runtime);
    }

    // EXTERNAL - composed
    public void addOwner(IFutureRuntime runtime, Address owner) {
        this.owners.addOwner(runtime, owner);
    }

    // EXTERNAL - composed
    public void removeOwner(IFutureRuntime runtime, Address owner) {
        this.owners.removeOwner(runtime, owner);
    }

    // EXTERNAL - composed
    public void changeRequirement(IFutureRuntime runtime, int newRequired) {
        this.owners.changeRequirement(runtime, newRequired);
    }

    // EXTERNAL - composed
    public Address getOwner(IFutureRuntime runtime, int ownerIndex) {
        return this.owners.getOwner(ownerIndex);
    }

    // EXTERNAL - composed
    public void setDailyLimit(IFutureRuntime runtime, long value) {
        this.limit.setDailyLimit(runtime, value);
    }

    // EXTERNAL - composed
    public void resetSpentToday(IFutureRuntime runtime) {
        this.limit.resetSpentToday(runtime);
    }

    // EXTERNAL
    public void kill(IFutureRuntime runtime, Address to) {
        // (modifier)
        this.owners.onlyManyOwners(runtime.avm_getSender(), Operation.from(runtime));
        
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
    public byte[] execute(IFutureRuntime runtime, Address to, long value, byte[] data) {
        // (modifier)
        this.owners.onlyOwner(runtime.avm_getSender());
        
        byte[] result = null;
        // first, take the opportunity to check that we're under the daily limit.
        if (this.limit.underLimit(runtime, value)) {
            this.logger.transactionUnderLimit();
            // yes - just execute the call.
            byte[] response = runtime.call(to, value, data);
            if (null == response) {
                throw new RequireFailedException();
            }
            // Returns nothing special.
            result = null;
        } else {
            // determine our operation hash.
            result = runtime.sha3(ByteArrayHelpers.appendLong(runtime.getMessageData(), runtime.getBlockNumber()));
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
    private boolean safeConfirm(IFutureRuntime runtime, byte[] h) {
        boolean result = false;
        try {
            result = confirm(runtime, h);
        } catch (RequireFailedException e) {
            result = false;
        }
        return result;
    }

    @Override
    public void changeOwner(IFutureRuntime runtime, Address from, Address to) {
        this.owners.changeOwner(runtime, from, to);
    }

    // confirm a transaction through just the hash. we use the previous transactions map, m_txs, in order
    // to determine the body of the transaction from the hash provided.
    @Override
    public boolean confirm(IFutureRuntime runtime, byte[] h) {
        // (modifier)
        this.owners.onlyManyOwners(runtime.avm_getSender(), Operation.from(runtime));
        
        boolean result = false;
        BytesKey key = BytesKey.from(h);
        if (null != this.transactions.get(key).to) {
            Transaction transaction = this.transactions.get(key);
            byte[] response = runtime.call(transaction.to, transaction.value, transaction.data);
            if (null == response) {
                throw new RequireFailedException();
            }
            this.transactions.remove(BytesKey.from(h));
            result = true;
        }
        return result;
    }


    // Transaction structure to remember details of transaction lest it need be saved for a later call.
    private static class Transaction {
        public Address to;
        public long value;
        public byte[] data;
    }
}
