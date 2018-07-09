package org.aion.avm.core.testWallet;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.aion.avm.api.Address;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;


/**
 * This exposes the interface of the Wallet class in a way which easily called by the Deployer but internally proxies into the
 * transformed contract space.
 */
public class CallProxy {
    public static void init(Consumer<byte[]> inputConsumer, Supplier<Class<?>> loader, Address extra1, Address extra2, int requiredVotes, long dailyLimit) throws Exception {
        byte[] onto = CallEncoder.init(extra1, extra2, requiredVotes, dailyLimit);
        inputConsumer.accept(onto);
        callDecode(loader);
    }

    public static void payable(Consumer<byte[]> inputConsumer, Supplier<Class<?>> loader, Address from, long value) throws Exception {
        byte[] onto = CallEncoder.payable(from, value);
        inputConsumer.accept(onto);
        callDecode(loader);
    }

    public static void addOwner(Consumer<byte[]> inputConsumer, Supplier<Class<?>> loader, Address owner) throws Exception {
        byte[] onto = CallEncoder.addOwner(owner);
        inputConsumer.accept(onto);
        callDecode(loader);
    }

    public static byte[] execute(Consumer<byte[]> inputConsumer, Supplier<Class<?>> loader, Address to, long value, byte[] data) throws Exception {
        byte[] onto = CallEncoder.execute(to, value, data);
        inputConsumer.accept(onto);
        return callDecode(loader);
    }

    public static boolean confirm(Consumer<byte[]> inputConsumer, Supplier<Class<?>> loader, byte[] data) throws Exception {
        byte[] onto = CallEncoder.confirm(data);
        inputConsumer.accept(onto);
        byte[] result = callDecode(loader);
        return (0x1 == result[0]);
    }

    public static void changeRequirement(Consumer<byte[]> inputConsumer, Supplier<Class<?>> loader, int newRequired) throws Exception {
        byte[] onto = CallEncoder.changeRequirement(newRequired);
        inputConsumer.accept(onto);
        callDecode(loader);
    }

    public static Address getOwner(Consumer<byte[]> inputConsumer, Supplier<Class<?>> loader, int ownerIndex) throws Exception {
        byte[] onto = CallEncoder.getOwner(ownerIndex);
        inputConsumer.accept(onto);
        byte[] result = callDecode(loader);
        return new Address(result);
    }

    public static void changeOwner(Consumer<byte[]> inputConsumer, Supplier<Class<?>> loader, Address from, Address to) throws Exception {
        byte[] onto = CallEncoder.changeOwner(from, to);
        inputConsumer.accept(onto);
        callDecode(loader);
    }

    public static void removeOwner(Consumer<byte[]> inputConsumer, Supplier<Class<?>> loader, Address owner) throws Exception {
        byte[] onto = CallEncoder.removeOwner(owner);
        inputConsumer.accept(onto);
        callDecode(loader);
    }

    public static void revoke(Consumer<byte[]> inputConsumer, Supplier<Class<?>> loader, byte[] transactionBytes) throws Exception {
        byte[] onto = CallEncoder.revoke(transactionBytes);
        inputConsumer.accept(onto);
        callDecode(loader);
    }


    private static byte[] callDecode(Supplier<Class<?>> loader) throws Exception {
        Class<?> walletClass = loader.get();
        ByteArray output = (ByteArray)walletClass
            .getMethod(UserClassMappingVisitor.mapMethodName("decode"))
            .invoke(null);
        return (null != output)
                ? output.getUnderlying()
                : null;
    }
}
