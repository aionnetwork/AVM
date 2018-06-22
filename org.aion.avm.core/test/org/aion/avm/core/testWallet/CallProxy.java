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
    public static void payable(Class<?> walletClass, Address from, long value) throws Exception {
        walletClass
            .getMethod(UserClassMappingVisitor.mapMethodName("payable"), Address.class, long.class)
            .invoke(null, from, value);
    }

    public static void addOwner(Class<?> walletClass, BlockchainRuntime runtime, Address owner) throws Exception {
        walletClass
            .getMethod(UserClassMappingVisitor.mapMethodName("addOwner"), BlockchainRuntime.class, Address.class)
            .invoke(null, runtime, owner);
    }

    public static byte[] execute(Class<?> walletClass, BlockchainRuntime runtime, Address to, long value, byte[] data) throws Exception {
        ByteArray result = (ByteArray) walletClass
            .getMethod(UserClassMappingVisitor.mapMethodName("execute"), BlockchainRuntime.class, Address.class, long.class, ByteArray.class)
            .invoke(null, runtime, to, value, new ByteArray(data));
        return (null != result)
             ? result.getUnderlying()
             : null;
    }

    public static boolean confirm(Class<?> walletClass, BlockchainRuntime runtime, byte[] data) throws Exception {
        return (Boolean)walletClass
            .getMethod(UserClassMappingVisitor.mapMethodName("confirm"), BlockchainRuntime.class, ByteArray.class)
            .invoke(null, runtime, new ByteArray(data));
    }

    public static void changeRequirement(Class<?> walletClass, BlockchainRuntime runtime, int newRequired) throws Exception {
        walletClass
            .getMethod(UserClassMappingVisitor.mapMethodName("changeRequirement"), BlockchainRuntime.class, int.class)
            .invoke(null, runtime, newRequired);
    }

    public static Address getOwner(Class<?> walletClass, BlockchainRuntime runtime, int ownerIndex) throws Exception {
        return (Address) walletClass
            .getMethod(UserClassMappingVisitor.mapMethodName("getOwner"), BlockchainRuntime.class, int.class)
            .invoke(null, runtime, ownerIndex);
    }

    public static void changeOwner(Class<?> walletClass, BlockchainRuntime runtime, Address from, Address to) throws Exception {
        walletClass
            .getMethod(UserClassMappingVisitor.mapMethodName("changeOwner"), BlockchainRuntime.class, Address.class, Address.class)
            .invoke(null, runtime, from, to);
    }

    public static void removeOwner(Class<?> walletClass, BlockchainRuntime runtime, Address owner) throws Exception {
        walletClass
            .getMethod(UserClassMappingVisitor.mapMethodName("removeOwner"), BlockchainRuntime.class, Address.class)
            .invoke(null, runtime, owner);
    }

    public static void revoke(Class<?> walletClass, BlockchainRuntime runtime) throws Exception {
        walletClass
            .getMethod(UserClassMappingVisitor.mapMethodName("revoke"), BlockchainRuntime.class)
            .invoke(null, runtime);
    }
}
