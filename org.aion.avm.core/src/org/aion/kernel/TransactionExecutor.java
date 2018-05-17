package org.aion.kernel;

import org.aion.avm.core.AvmImpl;
import org.aion.avm.rt.BlockchainRuntime;
import org.aion.avm.rt.Storage;

public class TransactionExecutor {

    public static void main(String[] args) {
        // NOTE: not ready yet!

        byte[] codeHash = new byte[32];

        byte[] from = new byte[32];
        byte[] to = new byte[32];
        byte[] payload = new byte[512];
        long energyLimit = 100000;
        Transaction tx = new Transaction(Transaction.Type.CREATE, from, to, payload, energyLimit);

        BlockchainRuntime rt = new BlockchainRuntime() {

            private Storage storage = new Storage() {
                @Override
                public byte[] get(byte[] key) {
                    return new byte[0];
                }

                @Override
                public void put(byte[] key, byte[] value) {

                }
            };

            @Override
            public byte[] getSender() {
                return tx.getFrom();
            }

            @Override
            public byte[] getAddress() {
                return tx.getTo();
            }

            @Override
            public Storage getStorage() {
                return storage;
            }

            @Override
            public long getEnergyLimit() {
                return 1000000;
            }
        };

        AvmImpl avm = new AvmImpl();
        avm.run(codeHash, rt);
    }
}
