package org.aion.avm.core.rejection;


/**
 * This is safe since it extends 31 variables but adds none of its own.
 */
public class RejectClassExtend31VariablesSafe extends RejectClass31Variables {
    public static byte[] main() {
        return new byte[] { 1 };
    }
}
