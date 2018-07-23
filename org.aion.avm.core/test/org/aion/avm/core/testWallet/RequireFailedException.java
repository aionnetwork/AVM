package org.aion.avm.core.testWallet;


/**
 * We use this in the cases where the testWallet.sol original was using the "require" statement, but it seems like this was a bug in this original
 * implementation since require is supposed to revert, on failure, whereas that contract is clearly assuming that the require would commit yet
 * terminate execution.
 * To emulate this behaviour, we catch this exception, at the top-level of the contract code, and return a null result (returning byte[0] in other
 * cases with no obvious return value).
 */
public class RequireFailedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
}
