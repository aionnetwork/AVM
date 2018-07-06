package org.aion.avm.core.testWallet;

import java.util.function.Supplier;

import org.aion.avm.api.Address;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;


/**
 * This exposes the interface of the Wallet class in a way which easily called by the Deployer but internally proxies into the
 * transformed contract space.
 */
public class CallProxy {
    public static void init(Supplier<Class<?>> loader, Address extra1, Address extra2, int requiredVotes, long dailyLimit) throws Exception {
        byte[] onto = CallEncoder.init(extra1, extra2, requiredVotes, dailyLimit);
        callDecode(loader, onto);
    }

    public static void payable(Supplier<Class<?>> loader, Address from, long value) throws Exception {
        byte[] onto = CallEncoder.payable(from, value);
        callDecode(loader, onto);
    }

    public static void addOwner(Supplier<Class<?>> loader, Address owner) throws Exception {
        byte[] onto = CallEncoder.addOwner(owner);
        callDecode(loader, onto);
    }

    public static byte[] execute(Supplier<Class<?>> loader, Address to, long value, byte[] data) throws Exception {
        byte[] onto = CallEncoder.execute(to, value, data);
        return callDecode(loader, onto);
    }

    public static boolean confirm(Supplier<Class<?>> loader, byte[] data) throws Exception {
        byte[] onto = CallEncoder.confirm(data);
        byte[] result = callDecode(loader, onto);
        return (0x1 == result[0]);
    }

    public static void changeRequirement(Supplier<Class<?>> loader, int newRequired) throws Exception {
        byte[] onto = CallEncoder.changeRequirement(newRequired);
        callDecode(loader, onto);
    }

    public static Address getOwner(Supplier<Class<?>> loader, int ownerIndex) throws Exception {
        byte[] onto = CallEncoder.getOwner(ownerIndex);
        byte[] result = callDecode(loader, onto);
        return new Address(result);
    }

    public static void changeOwner(Supplier<Class<?>> loader, Address from, Address to) throws Exception {
        byte[] onto = CallEncoder.changeOwner(from, to);
        callDecode(loader, onto);
    }

    public static void removeOwner(Supplier<Class<?>> loader, Address owner) throws Exception {
        byte[] onto = CallEncoder.removeOwner(owner);
        callDecode(loader, onto);
    }

    public static void revoke(Supplier<Class<?>> loader) throws Exception {
        byte[] onto = CallEncoder.revoke();
        callDecode(loader, onto);
    }


    private static byte[] callDecode(Supplier<Class<?>> loader, byte[] input) throws Exception {
        Class<?> walletClass = loader.get();
        ByteArray inputWrapper = new ByteArray(input);
        ByteArray output = (ByteArray)walletClass
            .getMethod(UserClassMappingVisitor.mapMethodName("decode"), ByteArray.class)
            .invoke(null,  inputWrapper);
        return (null != output)
                ? output.getUnderlying()
                : null;
    }
}
