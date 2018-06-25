package org.aion.avm.core.testWallet;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;


/**
 * This exposes the interface of the Wallet class in a way which easily called by the Deployer but internally proxies into the
 * transformed contract space.
 */
public class CallProxy {
    public static void payable(Class<?> walletClass, BlockchainRuntime runtime, Address from, long value) throws Exception {
        byte[] onto = CallEncoder.payable(from, value);
        callDecode(walletClass, runtime, onto);
    }

    public static void addOwner(Class<?> walletClass, BlockchainRuntime runtime, Address owner) throws Exception {
        byte[] onto = CallEncoder.addOwner(owner);
        callDecode(walletClass, runtime, onto);
    }

    public static byte[] execute(Class<?> walletClass, BlockchainRuntime runtime, Address to, long value, byte[] data) throws Exception {
        byte[] onto = CallEncoder.execute(to, value, data);
        return callDecode(walletClass, runtime, onto);
    }

    public static boolean confirm(Class<?> walletClass, BlockchainRuntime runtime, byte[] data) throws Exception {
        byte[] onto = CallEncoder.confirm(data);
        byte[] result = callDecode(walletClass, runtime, onto);
        return (0x1 == result[0]);
    }

    public static void changeRequirement(Class<?> walletClass, BlockchainRuntime runtime, int newRequired) throws Exception {
        byte[] onto = CallEncoder.changeRequirement(newRequired);
        callDecode(walletClass, runtime, onto);
    }

    public static Address getOwner(Class<?> walletClass, BlockchainRuntime runtime, int ownerIndex) throws Exception {
        byte[] onto = CallEncoder.getOwner(ownerIndex);
        byte[] result = callDecode(walletClass, runtime, onto);
        return new Address(result);
    }

    public static void changeOwner(Class<?> walletClass, BlockchainRuntime runtime, Address from, Address to) throws Exception {
        byte[] onto = CallEncoder.changeOwner(from, to);
        callDecode(walletClass, runtime, onto);
    }

    public static void removeOwner(Class<?> walletClass, BlockchainRuntime runtime, Address owner) throws Exception {
        byte[] onto = CallEncoder.removeOwner(owner);
        callDecode(walletClass, runtime, onto);
    }

    public static void revoke(Class<?> walletClass, BlockchainRuntime runtime) throws Exception {
        byte[] onto = CallEncoder.revoke();
        callDecode(walletClass, runtime, onto);
    }


    private static byte[] callDecode(Class<?> walletClass, BlockchainRuntime runtime, byte[] input) throws Exception {
        ByteArray inputWrapper = new ByteArray(input);
        ByteArray output = (ByteArray)walletClass
            .getMethod(UserClassMappingVisitor.mapMethodName("decode"), BlockchainRuntime.class, ByteArray.class)
            .invoke(null, runtime, inputWrapper);
        return (null != output)
                ? output.getUnderlying()
                : null;
    }
}
