package org.aion.avm.core;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.kernel.TransactionResult;


/**
 * This is currently just to contain some helpers methods commonly used across tests.
 * TODO:  Make this into a real IHelper implementation once the core VM is applying the balance requirements.
 */
public class TestingHelper {
    public static Address buildAddress(byte[] raw) {
        Address data = new Address(raw);
        return data;
    }
    public static Object decodeResult(TransactionResult result) {
        Object data = ABIDecoder.decodeOneObject(result.getReturnData());
        return data;
    }
}
