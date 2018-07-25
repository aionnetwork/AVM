package org.aion.avm.core.testWallet;


/**
 * This just exposes access to the Wallet for calls from the DirectProxy.  The reason for this is that the ABI expects to call with the
 * wrapped, contract-space types, while the DirectProxy is calling the pre-transformed Wallet code, meaning it expects real types.
 * This shim does the wrapping/unwrapping for the calls into the core Wallet code.
 */
public class WalletShim {
    public static byte[] main() {
        return Wallet.main();
    }
}
