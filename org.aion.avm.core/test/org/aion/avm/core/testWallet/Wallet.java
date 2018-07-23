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
    // Note that this key is really just a subset of uses of "Operation".
    private static AionMap<BytesKey, Transaction> transactions;

    public static void init(){

    }

    // The contract "Constructor".  Note that this should only be called once, when initially deployed (in Ethereum world, the arguments are
    // just pass as part of the deployment payload, after the code).
    public static void init(Address[] requestedOwners, int votesRequiredPerOperation, long daylimit) {
        // This is the contract entry-point so "construct" the contract fragments from which we are derived.
        Address sender = BlockchainRuntime.getSender();
        long nowInSeconds = BlockchainRuntime.getBlockEpochSeconds();
        long nowInDays = nowInSeconds / (24 * 60 * 60);
        Multiowned.init(sender, requestedOwners, votesRequiredPerOperation);
        Daylimit.init(daylimit, nowInDays);
        
        Wallet.transactions = new AionMap<>();
    }

    /**
     * The generic start symbol which processes the input using the ABI to calls out to other helpers.
     *
     * @return The output of running the invoke (null for void methods).
     */
    public static byte[] main() {
        // Most of our paths return nothing so just default to the empty byte array.
        byte[] result = new byte[0];
        byte[] input = BlockchainRuntime.getData();
        Abi.Decoder decoder = Abi.buildDecoder(input);
        byte methodByte = decoder.decodeByte();
        
        switch (methodByte) {
        case Abi.kWallet_init : {
            // We know that this is int (length), Address(*length), int, long.
            int addressCount = decoder.decodeInt();
            Address[] requestedOwners = new Address[addressCount];
            for (int i = 0; i < addressCount; ++i) {
                requestedOwners[i] = decoder.decodeAddress();
            }
            int votesRequiredPerOperation = decoder.decodeInt();
            long daylimit = decoder.decodeLong();
            Wallet.init(requestedOwners, votesRequiredPerOperation, daylimit);
            break;
        }
        case Abi.kWallet_payable : {
            Address from = decoder.decodeAddress();
            long value = decoder.decodeLong();
            Wallet.payable(from, value);
            break;
        }
        case Abi.kWallet_addOwner : {
            Address owner = decoder.decodeAddress();
            Wallet.addOwner(owner);
            break;
        }
        case Abi.kWallet_execute : {
            Address to = decoder.decodeAddress();
            long value = decoder.decodeLong();
            byte[] data = decoder.decodeRemainder();
            result = Wallet.execute(to, value, data);
            break;
        }
        case Abi.kWallet_confirm : {
            byte[] data = decoder.decodeRemainder();
            boolean bool = Wallet.confirm(data);
            result = new byte[] { (byte)(bool ? 0x1 : 0x0) };
            break;
        }
        case Abi.kWallet_changeRequirement : {
            int newRequired = decoder.decodeInt();
            Wallet.changeRequirement(newRequired);
            break;
        }
        case Abi.kWallet_getOwner : {
            int ownerIndex = decoder.decodeInt();
            Address owner = Wallet.getOwner(ownerIndex);
            // We need to encode this so allocate a buffer and write it with the encoder.
            byte[] onto = new byte[Address.LENGTH];
            Abi.buildEncoder(onto).encodeAddress(owner);
            result = onto;
            break;
        }
        case Abi.kWallet_changeOwner : {
            Address from = decoder.decodeAddress();
            Address to = decoder.decodeAddress();
            Wallet.changeOwner(from, to);
            break;
        }
        case Abi.kWallet_removeOwner : {
            Address owner = decoder.decodeAddress();
            Wallet.removeOwner(owner);
            break;
        }
        case Abi.kWallet_revoke : {
            byte[] transactionBytes = decoder.decodeRemainder();
            Wallet.revoke(transactionBytes);
            break;
        }
        default:
            throw new AssertionError("No method for byte: " + methodByte);
        }
        return result;
    }

    // EXTERNAL - composed
    public static void revoke(byte[] transactionBytes) {
        Multiowned.revoke(transactionBytes);
    }

    // EXTERNAL - composed
    public static void addOwner(Address owner) {
        Multiowned.addOwner(owner);
    }

    // EXTERNAL - composed
    public static void removeOwner(Address owner) {
        Multiowned.removeOwner(owner);
    }

    // EXTERNAL - composed
    public static void changeRequirement(int newRequired) {
        Multiowned.changeRequirement(newRequired);
    }

    // EXTERNAL - composed
    public static Address getOwner(int ownerIndex) {
        return Multiowned.getOwner(ownerIndex);
    }

    // EXTERNAL - composed
    public static void setDailyLimit(long value) {
        Daylimit.setDailyLimit(value);
    }

    // EXTERNAL - composed
    public static void resetSpentToday() {
        Daylimit.resetSpentToday();
    }

    // EXTERNAL
    public static void kill(Address to) {
        // (modifier)
        Multiowned.onlyManyOwners(BlockchainRuntime.getSender(), Operation.fromMessage());
        
        BlockchainRuntime.selfDestruct(to);
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
    public static byte[] execute(Address to, long value, byte[] data) {
        // (modifier)
        Multiowned.onlyOwner(BlockchainRuntime.getSender());
        
        byte[] result = null;
        // first, take the opportunity to check that we're under the daily limit.
        if (Daylimit.underLimit(value)) {
            EventLogger.singleTransact(BlockchainRuntime.getSender(), value, to, data);
            // yes - just execute the call.
            byte[] response = BlockchainRuntime.call(to, 0, data, value);
            if (null == response) {
                throw new RequireFailedException();
            }
            // Returns nothing special.
            result = null;
        } else {
            // determine our operation hash.
            result = Operation.rawOperationForCurrentMessageAndBlock();
            BytesKey transactionKey = BytesKey.from(result);
            if (!safeConfirm(result) && (null == Wallet.transactions.get(transactionKey))) {
                Transaction transaction = new Transaction();
                transaction.to = to;
                transaction.value = value;
                transaction.data = data;
                Wallet.transactions.put(transactionKey, transaction);
                EventLogger.confirmationNeeded(Operation.fromHashedBytes(result), BlockchainRuntime.getSender(), value, to, data);
            }
        }
        return result;
    }

    // TODO:  Determine if this is the correct emulation of semantics.  The Solidity test seems to act like the exception is the same as "return false".
    private static boolean safeConfirm(byte[] h) {
        boolean result = false;
        try {
            result = confirm(h);
        } catch (RequireFailedException e) {
            result = false;
        }
        return result;
    }

    public static void changeOwner(Address from, Address to) {
        Multiowned.changeOwner(from, to);
    }

    // confirm a transaction through just the hash. we use the previous transactions map, m_txs, in order
    // to determine the body of the transaction from the hash provided.
    public static boolean confirm(byte[] h) {
        // (modifier)
        Operation operationToConfirm = Operation.fromRawBytes(h);
        Multiowned.onlyManyOwners(BlockchainRuntime.getSender(), operationToConfirm);
        
        boolean result = false;
        BytesKey key = BytesKey.from(h);
        if (null != Wallet.transactions.get(key).to) {
            Transaction transaction = Wallet.transactions.get(key);
            byte[] response = BlockchainRuntime.call(transaction.to, 0, transaction.data, transaction.value);
            if (null == response) {
                throw new RequireFailedException();
            }
            EventLogger.multiTransact(BlockchainRuntime.getSender(), operationToConfirm, transaction.value, transaction.to, transaction.data);
            Wallet.transactions.remove(BytesKey.from(h));
            result = true;
        }
        return result;
    }


    // Transaction structure to remember details of transaction lest it need be saved for a later call.
    // (this is public just for easy referencing in the Deployer's loader logic).
    private static class Transaction {
        public Address to;
        public long value;
        public byte[] data;
    }
}
