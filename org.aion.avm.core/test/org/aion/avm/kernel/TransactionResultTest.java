package org.aion.avm.kernel;

import org.aion.kernel.AvmTransactionResult;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TransactionResultTest {

    @Test
    public void testStatusCodeNaming() {
        for (AvmTransactionResult.Code code : AvmTransactionResult.Code.values()) {
            System.out.println(code);
            if (code.name().startsWith("REJECTED")) {
                assertTrue(code.isRejected());
            } else if (code.name().startsWith("FAILED")) {
                assertTrue(code.isFailed());
            } else {
                assertTrue(code.isSuccess());
            }
        }
    }
}
