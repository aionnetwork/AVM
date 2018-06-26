package org.aion.avm.core.testWallet;

import org.aion.avm.api.Address;
import org.aion.avm.api.IBlockchainRuntime;
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
    public static void avoidArrayWrappingFactory(IBlockchainRuntime runtime, Address owner1, Address owner2, int votesRequiredPerOperation, long daylimit) {
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
    public static void init(IBlockchainRuntime runtime, Address[] requestedOwners, int votesRequiredPerOperation, long daylimit) {
        // This is the contract entry-point so "construct" the contract fragments from which we are derived.
        Address sender = runtime.getSender();
        long nowInSeconds = runtime.getBlockEpochSeconds();
        long nowInDays = nowInSeconds / (24 * 60 * 60);
        Multiowned.init(runtime, sender, requestedOwners, votesRequiredPerOperation);
        Daylimit.init(runtime, daylimit, nowInDays);
        
        Wallet.transactions = new AionMap<>();
    }

    /**
     * The generic start symbol which processes the input using the ABI to calls out to other helpers.
     * 
     * @param runtime The context of the invocation.
     * @param input The ABI-encoded input.
     * @return The output of running the invoke (null for void methods).
     */
    public static byte[] decode(IBlockchainRuntime runtime, byte[] input) {
        byte[] result = null;
        Abi.Decoder decoder = Abi.buildDecoder(input);
        byte methodByte = decoder.decodeByte();
        
        switch (methodByte) {
        case Abi.kWallet_payable : {
            Address from = decoder.decodeAddress();
            long value = decoder.decodeLong();
            Wallet.payable(runtime, from, value);
            break;
        }
        case Abi.kWallet_addOwner : {
            Address owner = decoder.decodeAddress();
            Wallet.addOwner(runtime, owner);
            break;
        }
        case Abi.kWallet_execute : {
            Address to = decoder.decodeAddress();
            long value = decoder.decodeLong();
            byte[] data = decoder.decodeRemainder();
            result = Wallet.execute(runtime, to, value, data);
            break;
        }
        case Abi.kWallet_confirm : {
            byte[] data = decoder.decodeRemainder();
            boolean bool = Wallet.confirm(runtime, data);
            result = new byte[] { (byte)(bool ? 0x1 : 0x0) };
            break;
        }
        case Abi.kWallet_changeRequirement : {
            int newRequired = decoder.decodeInt();
            Wallet.changeRequirement(runtime, newRequired);
            break;
        }
        case Abi.kWallet_getOwner : {
            int ownerIndex = decoder.decodeInt();
            Address owner = Wallet.getOwner(runtime, ownerIndex);
            // We need to encode this so allocate a buffer and write it with the encoder.
            byte[] onto = new byte[Address.LENGTH];
            Abi.buildEncoder(onto).encodeAddress(owner);
            result = onto;
            break;
        }
        case Abi.kWallet_changeOwner : {
            Address from = decoder.decodeAddress();
            Address to = decoder.decodeAddress();
            Wallet.changeOwner(runtime, from, to);
            break;
        }
        case Abi.kWallet_removeOwner : {
            Address owner = decoder.decodeAddress();
            Wallet.removeOwner(runtime, owner);
            break;
        }
        case Abi.kWallet_revoke : {
            Wallet.revoke(runtime);
            break;
        }
        default:
            throw new AssertionError(methodByte);
        }
        return result;
    }

    // EXTERNAL - composed
    public static void revoke(IBlockchainRuntime runtime) {
        Multiowned.revoke(runtime);
    }

    // EXTERNAL - composed
    public static void addOwner(IBlockchainRuntime runtime, Address owner) {
        Multiowned.addOwner(runtime, owner);
    }

    // EXTERNAL - composed
    public static void removeOwner(IBlockchainRuntime runtime, Address owner) {
        Multiowned.removeOwner(runtime, owner);
    }

    // EXTERNAL - composed
    public static void changeRequirement(IBlockchainRuntime runtime, int newRequired) {
        Multiowned.changeRequirement(runtime, newRequired);
    }

    // EXTERNAL - composed
    public static Address getOwner(IBlockchainRuntime runtime, int ownerIndex) {
        return Multiowned.getOwner(runtime, ownerIndex);
    }

    // EXTERNAL - composed
    public static void setDailyLimit(IBlockchainRuntime runtime, long value) {
        Daylimit.setDailyLimit(runtime, value);
    }

    // EXTERNAL - composed
    public static void resetSpentToday(IBlockchainRuntime runtime) {
        Daylimit.resetSpentToday(runtime);
    }

    // EXTERNAL
    public static void kill(IBlockchainRuntime runtime, Address to) {
        // (modifier)
        Multiowned.onlyManyOwners(runtime, runtime.getSender(), Operation.fromMessage(runtime));
        
        runtime.selfDestruct(to);
    }

    // gets called when no other function matches
    public static void payable(IBlockchainRuntime runtime, Address from, long value) {
        if (value > 0) {
            EventLogger.deposit(runtime, from, value);
        }
    }

    // EXTERNAL
    // Outside-visible transact entry point. Executes transaction immediately if below daily spend limit.
    // If not, goes into multisig process. We provide a hash on return to allow the sender to provide
    // shortcuts for the other confirmations (allowing them to avoid replicating the _to, _value
    // and _data arguments). They still get the option of using them if they want, anyways.
    public static byte[] execute(IBlockchainRuntime runtime, Address to, long value, byte[] data) {
        // (modifier)
        Multiowned.onlyOwner(runtime, runtime.getSender());
        
        byte[] result = null;
        // first, take the opportunity to check that we're under the daily limit.
        if (Daylimit.underLimit(runtime, value)) {
            EventLogger.singleTransact(runtime, runtime.getSender(), value, to, data);
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
                EventLogger.confirmationNeeded(runtime, Operation.fromBytes(result), runtime.getSender(), value, to, data);
            }
        }
        return result;
    }

    // TODO:  Determine if this is the correct emulation of semantics.  The Solidity test seems to act like the exception is the same as "return false".
    private static boolean safeConfirm(IBlockchainRuntime runtime, byte[] h) {
        boolean result = false;
        try {
            result = confirm(runtime, h);
        } catch (RequireFailedException e) {
            result = false;
        }
        return result;
    }

    public static void changeOwner(IBlockchainRuntime runtime, Address from, Address to) {
        Multiowned.changeOwner(runtime, from, to);
    }

    // confirm a transaction through just the hash. we use the previous transactions map, m_txs, in order
    // to determine the body of the transaction from the hash provided.
    public static boolean confirm(IBlockchainRuntime runtime, byte[] h) {
        // (modifier)
        Multiowned.onlyManyOwners(runtime, runtime.getSender(), Operation.fromBytes(h));
        
        boolean result = false;
        BytesKey key = BytesKey.from(h);
        if (null != Wallet.transactions.get(key).to) {
            Transaction transaction = Wallet.transactions.get(key);
            byte[] response = runtime.call(transaction.to, null, transaction.data, transaction.value);
            if (null == response) {
                throw new RequireFailedException();
            }
            EventLogger.multiTransact(runtime, runtime.getSender(), Operation.fromBytes(h), transaction.value, transaction.to, transaction.data);
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
