package org.aion.kernel;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.rt.Address;
import org.aion.avm.rt.BlockchainRuntime;

public class TransactionExecutor {

    public static void main(String[] args) {
        // NOTE: not ready yet!

        byte[] from = new byte[32];
        byte[] to = new byte[32];
        byte[] payload = new byte[512];
        long energyLimit = 100000;
        Transaction tx = new Transaction(Transaction.Type.CREATE, from, to, payload, energyLimit);
        // We will create these up-front since our implementation should return the same instance on every call, where possible.
        Address sender = new Address(tx.getFrom());
        Address address = new Address(tx.getTo());

        BlockchainRuntime rt = new BlockchainRuntime() {
            @Override
            public Address avm_getSender() {
                return sender;
            }

            @Override
            public Address avm_getAddress() {
                return address;
            }

            @Override
            public long avm_getEnergyLimit() {
                return 1000000;
            }

            @Override
            public ByteArray avm_getData() {
                return null;
            }

            @Override
            public ByteArray avm_getStorage(ByteArray key) {
                return new ByteArray(new byte[0]);
            }

            @Override
            public void avm_putStorage(ByteArray key, ByteArray value) {
            }
        };

        // Note that the creator of the AvmImpl needs to provide the shared class loader.
        AvmImpl avm = new AvmImpl(new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers()));
        avm.run(rt);
    }
}
