package org.aion.avm.tooling;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.tooling.hash.HashUtils;
import org.aion.types.Address;
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
        return Address.wrap(hash);
    }
}
