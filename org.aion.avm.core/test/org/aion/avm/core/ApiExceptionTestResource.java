package org.aion.avm.core;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.internal.InvalidTxDataException;

public class ApiExceptionTestResource {

    public byte[] main() {
        try {
            ABIDecoder.decode(new byte[]{(byte) 0xff, (byte) 0xee});
        } catch (InvalidTxDataException ex) {
            return "EXCEPTION".getBytes();
        }

        return "SUCCESS".getBytes();
    }
}
