package org.aion.avm.core.rejection.errors;


/**
 * Proves that user code isn't allowed to instantiate VirtualMachineError (or anything in that hierarchy).
 * (note that it is Abstract so we just picked OutOfMemoryError).
 */
public class RejectInstantiateError {
    public static byte[] main() {
        Object t = new OutOfMemoryError();
        byte fromObject = (byte) t.hashCode();
        return new byte[] { fromObject };
    }
}
