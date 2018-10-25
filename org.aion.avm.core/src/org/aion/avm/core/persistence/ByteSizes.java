package org.aion.avm.core.persistence;


/**
 * We provide this to make the in-memory size accounting for issue-195 coexist with the logic we use to serialize it for disk.
 * All values are in bytes.
 */
public class ByteSizes {
    public static final int BYTE = 1;
    public static final int SHORT = 2;
    public static final int CHAR = 2;
    public static final int INT = 4;
    public static final int LONG = 8;

    // For issue-267, we want to describe a fixed-size of a reference in a serialized object in a way not dependent on representation,
    // meaning that we want an abstract constant.
    // We represent that as a reference being the same cost as 32 bytes of primitive data.
    public static final int REFERENCE = 32;
}
