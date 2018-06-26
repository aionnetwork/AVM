package org.aion.avm.core.testWallet;

import java.util.function.Supplier;

import org.aion.avm.api.Address;
import org.aion.avm.api.IBlockchainRuntime;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;


/**
 * This exposes the interface of the Wallet class in a way which easily called by the Deployer but internally proxies into the
 * transformed contract space.
 */
public class CallProxy {
    public static void payable(Supplier<Class<?>> loader, IBlockchainRuntime runtime, Address from, long value) throws Exception {
        byte[] onto = CallEncoder.payable(from, value);
        callDecode(loader, runtime, onto);
    }

    public static void addOwner(Supplier<Class<?>> loader, IBlockchainRuntime runtime, Address owner) throws Exception {
        byte[] onto = CallEncoder.addOwner(owner);
        callDecode(loader, runtime, onto);
    }

    public static byte[] execute(Supplier<Class<?>> loader, IBlockchainRuntime runtime, Address to, long value, byte[] data) throws Exception {
        byte[] onto = CallEncoder.execute(to, value, data);
        return callDecode(loader, runtime, onto);
    }

    public static boolean confirm(Supplier<Class<?>> loader, IBlockchainRuntime runtime, byte[] data) throws Exception {
        byte[] onto = CallEncoder.confirm(data);
        byte[] result = callDecode(loader, runtime, onto);
        return (0x1 == result[0]);
    }

    public static void changeRequirement(Supplier<Class<?>> loader, IBlockchainRuntime runtime, int newRequired) throws Exception {
        byte[] onto = CallEncoder.changeRequirement(newRequired);
        callDecode(loader, runtime, onto);
    }

    public static Address getOwner(Supplier<Class<?>> loader, IBlockchainRuntime runtime, int ownerIndex) throws Exception {
        byte[] onto = CallEncoder.getOwner(ownerIndex);
        byte[] result = callDecode(loader, runtime, onto);
        return new Address(result);
    }

    public static void changeOwner(Supplier<Class<?>> loader, IBlockchainRuntime runtime, Address from, Address to) throws Exception {
        byte[] onto = CallEncoder.changeOwner(from, to);
        callDecode(loader, runtime, onto);
    }

    public static void removeOwner(Supplier<Class<?>> loader, IBlockchainRuntime runtime, Address owner) throws Exception {
        byte[] onto = CallEncoder.removeOwner(owner);
        callDecode(loader, runtime, onto);
    }

    public static void revoke(Supplier<Class<?>> loader, IBlockchainRuntime runtime) throws Exception {
        byte[] onto = CallEncoder.revoke();
        callDecode(loader, runtime, onto);
    }


    private static byte[] callDecode(Supplier<Class<?>> loader, IBlockchainRuntime runtime, byte[] input) throws Exception {
        Class<?> walletClass = loader.get();
        ByteArray inputWrapper = new ByteArray(input);
        ByteArray output = (ByteArray)walletClass
            .getMethod(UserClassMappingVisitor.mapMethodName("decode"), IBlockchainRuntime.class, ByteArray.class)
            .invoke(null, runtime, inputWrapper);
        return (null != output)
                ? output.getUnderlying()
                : null;
    }
}
