package org.aion.avm.tooling;

import org.aion.avm.userlib.abi.ABIEncoder;
import avm.Blockchain;


public class ShadowRuntimeFailureTarget {
    static {
        if (Blockchain.getData().length > 0) {
            runTest();
        }
    }

    // The point of this test is to fail to call deprecated methods so we need that here.
    @SuppressWarnings("deprecation")
    private static Object runTest() {
        Object result = null;
        byte option = Blockchain.getData()[0];
        switch(option) {
            case 0:
                result = new Boolean(true);
                break;
            case 1:
                result = new Byte((byte)1);
                break;
            case 2:
                result = new Character('c');
                break;
            case 3:
                result = new Short((short)1);
                break;
            case 4:
                result = new Integer(1);
                break;
            case 5:
                result = new Long(1);
                break;
            case 6:
                result = new Float(1.0);
                break;
            case 7:
                result = new Double(1.0);
                break;
            case 8:
                // The last case is a success (to make sure we aren't just failing to do anything).
                result = Boolean.valueOf(true);
                break;
            default:
                Blockchain.invalid();
        }
        return result;
    }

    public static byte[] main() {
        runTest();
        return ABIEncoder.encodeOneBoolean((boolean) runTest());
    }
}
