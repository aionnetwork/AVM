package org.aion.avm.core.persistence;


/**
 * We provide this to make the in-memory size accounting for issue-195 coexist with the logic we use to serialize it for disk.
 * All values are in bytes.
 */
public class ByteSizes {
    public static final int BOOLEAN = 1;
    public static final int BYTE = 1;
    public static final int SHORT = 2;
    public static final int CHAR = 2;
    public static final int INT = 4;
    public static final int FLOAT = 4;
    public static final int LONG = 8;
    public static final int DOUBLE = 8;
}
