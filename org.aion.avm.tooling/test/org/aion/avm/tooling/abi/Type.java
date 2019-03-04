package org.aion.avm.tooling.abi;

public enum Type {
    BYTE            (1),
    BOOLEAN         (2),
    CHAR            (3),
    SHORT           (4),
    INT             (5),
    LONG            (6),
    FLOAT           (7),
    DOUBLE          (8),
    BYTE_ARRAY      (9),
    BOOL_ARRAY      (10),
    CHAR_ARRAY      (11),
    SHORT_ARRAY     (12),
    INT_ARRAY       (13),
    LONG_ARRAY      (14),
    FLOAT_ARRAY     (15),
    DOUBLE_ARRAY    (16),
    STRING          (17),
    INT_ARRAY_2D    (18);

    private final int val;

    Type(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    private static Type[] allValues = values();
    public static Type fromOrdinal(int n) {
        assert n > 0 && n <= 18;
        return allValues[n-1];
    }
}
