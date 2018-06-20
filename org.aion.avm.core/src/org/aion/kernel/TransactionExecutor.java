package org.aion.kernel;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.java.lang.String;
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
        TransformedDappStorage codeStorage = new TransformedDappStorage();

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
            public void avm_updateCode(ByteArray newCode, String codeVersion) {
            }

            @Override
            public void avm_selfDestruct(Address beneficiary) {
            }

            @Override
            public long avm_getBlockEpochSeconds() {
                return 1l;
            }

            @Override
            public ByteArray avm_getMessageData() {
                return null;
            }

            @Override
            public long avm_getBlockNumber() {
                return 1l;
            }

            @Override
            public ByteArray avm_sha3(ByteArray data) {
                return null;
            }

            @Override
            public ByteArray avm_call(Address targetAddress, long energyToSend, ByteArray payload) {
                return null;
            }
        };

        // Note that the creator of the AvmImpl needs to provide the shared class loader.
        AvmImpl avm = new AvmImpl(new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers()), codeStorage);
        avm.run(rt);
    }
}
