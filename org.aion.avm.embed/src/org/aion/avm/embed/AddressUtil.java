package org.aion.avm.embed;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.aion.types.AionAddress;
import org.aion.avm.embed.hash.HashUtils;
import org.aion.kernel.TestingState;


/**
 * This is a temporary helper class until the contract address generation logic can be moved
 * into the calling kernel (since it depends on the blockchain design, not the VM).
 */
public class AddressUtil {
    public static AionAddress generateContractAddress(AionAddress deployerAddress, BigInteger nonce) {
        long nonceAsLong = new BigInteger(nonce.toByteArray()).longValue();
        ByteBuffer buffer = ByteBuffer.allocate(32 + 8).put(deployerAddress.toByteArray()).putLong(nonceAsLong);
        byte[] hash = HashUtils.sha256(buffer.array());
        // NOTE: This implemenation assumes are being used on the testing kernel.
        hash[0] = TestingState.AVM_CONTRACT_PREFIX;
        return new AionAddress(hash);
    }
}
