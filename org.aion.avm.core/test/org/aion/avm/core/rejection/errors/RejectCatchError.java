package org.aion.avm.core.rejection.errors;


/**
 * Proves that user code isn't allowed to catch VirtualMachineError (or anything in that hierarchy).
 */
public class RejectCatchError {
    public static byte[] main() {
        byte[] result = null;
        try {
            result = new byte[] { 0x0 };
        } catch (VirtualMachineError e) {
            result = new byte[] { 0x1 };
        } finally {
            result = new byte[] { 0x2 };
        }
        return result;
    }
}
