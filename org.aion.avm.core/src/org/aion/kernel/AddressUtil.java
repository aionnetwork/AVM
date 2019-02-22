package org.aion.kernel;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.util.HashUtils;
import org.aion.vm.api.interfaces.Address;
import org.aion.vm.api.interfaces.TransactionInterface;


/**
 * This is a temporary helper class until the contract address generation logic can be moved
 * into the calling kernel (since it depends on the blockchain design, not the VM).
 */
public class AddressUtil {
    public static Address generateContractAddress(TransactionInterface tx) {
        Address sender = tx.getSenderAddress();
        long nonce = new BigInteger(tx.getNonce()).longValue();
        ByteBuffer buffer = ByteBuffer.allocate(32 + 8).put(sender.toBytes()).putLong(nonce);
        byte[] hash = HashUtils.sha256(buffer.array());
        hash[0] = NodeEnvironment.CONTRACT_PREFIX;
        return AvmAddress.wrap(hash);
    }
}
