package org.aion.kernel;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.rt.Address;
import org.aion.avm.rt.BlockchainRuntime;

import java.io.File;

public class TransactionExecutor {

    public static void main(String[] args) {
        // NOTE: not ready yet!

        byte[] from = new byte[32];
        byte[] to = new byte[32];
        byte[] payload = new byte[512];
        long energyLimit = 100000;
        Transaction tx = new Transaction(Transaction.Type.CREATE, from, to, payload, energyLimit);

        BlockchainRuntime rt = new BlockchainRuntime() {
            // We can't eagerly create these addresses, since the IHelper isn't yet installed, but we do want to reuse the same instance, once we create it.
            private Address cachedSender;
            private Address cachedAddress;

            @Override
            public Address avm_getSender() {
                if (null == this.cachedSender) {
                    this.cachedSender = new Address(tx.getFrom());
                }
                return this.cachedSender;
            }

            @Override
            public Address avm_getAddress() {
                if (null == this.cachedAddress) {
                    this.cachedAddress = new Address(tx.getTo());
                }
                return this.cachedAddress;
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

            @Override
            public void avm_storeTransformedDapp(File transformedJar) {
            }

            @Override
            public File avm_loadTransformedDapp(Address address) {
                return null;
            }
        };

        // Note that the creator of the AvmImpl needs to provide the shared class loader.
        AvmImpl avm = new AvmImpl(new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers()));
        avm.run(rt);
    }
}
