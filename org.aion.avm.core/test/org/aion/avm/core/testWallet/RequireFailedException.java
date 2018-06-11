package org.aion.avm.core.testWallet;


/**
 * We use this exception in place of how the existing Solidity world uses the "require" statement.
 * We need to determine if leaving this in user-space is more correct of if we need to make this a kernel-level intrinsic.
 */
public class RequireFailedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
}
