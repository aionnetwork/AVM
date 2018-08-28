package org.aion.avm.core;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;


/**
 * The test class loaded by SubclassPersistenceIntegrationTest.
 * This defines an operation which should be normally fine but is illegal when loaded as a DApp since it sub-classes a rejected exception type.
 */
public class SubclassPersistenceIntegrationTestFailException {
    private static SubNullPointerException npe;

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(SubclassPersistenceIntegrationTestFailException.class, BlockchainRuntime.getData());
    }

    public static int setup_nep() {
        // We just need some kind of random number.
        npe = new SubNullPointerException((int)BlockchainRuntime.getBlockTimestamp());
        return npe.number;
    }
    public static int check_npe() {
        return npe.number;
    }


    private static class SubNullPointerException extends NullPointerException {
        private static final long serialVersionUID = 1L;
        public final int number;
        public SubNullPointerException(int number) {
            super();
            this.number = number;
        }
    }
}
