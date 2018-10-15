package org.aion.avm.core.poc;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.AionBuffer;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;


/**
 * Multi-sig wallet demo.
 */
public class Wallet {

    private AionMap<Bytes32, PendingTransaction> pendingTxs = new AionMap<>();

    private AionSet<Address> owners = new AionSet<>();

    private int confirmationsRequired;

    /**
     * Creates a wallet instance.
     *
     * @param owners                The initial owners
     * @param confirmationsRequired The number of confirmations required
     */
    public Wallet(Address[] owners, int confirmationsRequired) {
        for (Address owner : owners) {
            this.owners.add(owner);
        }
        this.confirmationsRequired = confirmationsRequired;
    }

    /**
     * Proposes a transfer to the designated recipient.
     *
     * @param to          The recipient address
     * @param value       The amount of value to transfer
     * @param data        The data to pass
     * @param energyLimit The energy limit
     * @return an unique identifier of the pending transaction
     */
    public byte[] propose(Address to, long value, byte[] data, long energyLimit) {
        PendingTransaction pendingTx = new PendingTransaction(to, value, data, energyLimit);
        byte[] id = pendingTx.getId();

        pendingTxs.put(new Bytes32(id), pendingTx);
        return id;
    }

    /**
     * Confirms a pending transaction.
     *
     * @param id The transaction id.
     */
    public boolean confirm(byte[] id) {
        // check access
        Address sender = BlockchainRuntime.getCaller();
        if (!owners.contains(sender)) {
            return false;
        }

        PendingTransaction pendingTx = pendingTxs.get(new Bytes32(id));
        if (pendingTx == null) {
            return false;
        }

        pendingTx.confirmations.add(sender);
        BlockchainRuntime.log("Confirm".getBytes(), sender.unwrap(), new byte[0]);

        if (pendingTx.confirmations.size() >= confirmationsRequired) {
            // remove the transaction
            pendingTxs.remove(new Bytes32(id));

            // send the transaction
            BlockchainRuntime.call(pendingTx.to, pendingTx.value, pendingTx.data, pendingTx.energyLimit);
        }

        return true;
    }

    /**
     * Represents a pending transaction.
     */
    public static class PendingTransaction {
        private Address to;
        private long value;
        private byte[] data;
        private long energyLimit;

        private AionSet<Address> confirmations = new AionSet<>();

        public PendingTransaction(Address to, long value, byte[] data, long energyLimit) {
            this.to = to;
            this.value = value;
            this.data = data;
            this.energyLimit = energyLimit;
        }

        public byte[] getId() {
            AionBuffer buffer = AionBuffer.allocate(Address.LENGTH + Long.BYTES + data.length + Long.BYTES);
            buffer.put(to.unwrap());
            buffer.putLong(value);
            buffer.put(data);
            buffer.putLong(energyLimit);

            return BlockchainRuntime.blake2b(buffer.array());
        }
    }
}
