package org.aion.avm.userlib.abi;

import org.aion.avm.api.Address;

import java.util.Map;
import org.aion.avm.userlib.AionMap;

/**
 * Description of the tokens the ABI uses to describe extends of data in the stream.
 *
 * The a reference in the stream can either be a leaf type, array type, or a null:
 * -a leaf type is just the leaf type token, potentially a 2-byte length, then the data.
 * -an array type is the ARRAY token, followed by the leaf token it is an array of, followed by a 2-byte length,
 *  followed by the elements (as tokens).
 * -a null type is the NULL token, followed by the leaf token it is an array of.
 *
 * Note that array types cannot contain other array types.  While it would be trivial to extend this model to cover
 * such cases, there is no reason to support/test them.
 */
public enum ABIToken {
    BYTE      ((byte)0x01, true, false, byte.class, Byte.class),
    // (boolean is encoded as a byte)
    BOOLEAN   ((byte)0x02, true, false, boolean.class, Boolean.class),
    CHAR      ((byte)0x03, true, false, char.class, Character.class),
    SHORT     ((byte)0x04, true, false, short.class, Short.class),
    INT       ((byte)0x05, true, false, int.class, Integer.class),
    LONG      ((byte)0x06, true, false, long.class, Long.class),
    FLOAT     ((byte)0x07, true, false, float.class, Float.class),
    DOUBLE    ((byte)0x08, true, false, double.class, Double.class),

    A_BYTE    ((byte)0x11, true, true, byte[].class, byte[].class),
    // (boolean is encoded as a byte)
    A_BOOLEAN ((byte)0x12, true, true, boolean[].class, boolean[].class),
    A_CHAR    ((byte)0x13, true, true, char[].class, char[].class),
    A_SHORT   ((byte)0x14, true, true, short[].class, short[].class),
    A_INT     ((byte)0x15, true, true, int[].class, int[].class),
    A_LONG    ((byte)0x16, true, true, long[].class, long[].class),
    A_FLOAT   ((byte)0x17, true, true, float[].class, float[].class),
    A_DOUBLE  ((byte)0x18, true, true, double[].class, double[].class),

    // Note that the string is described as number of UTF-8 encoded bytes, so each element is actually 1 byte.
    STRING    ((byte)0x21, true, true, String.class, String.class),
    ADDRESS   ((byte)0x22, true, false, Address.class, Address.class),

    // ARRAY and NULL can't have sizes since they only decorate other types or represent placeholders in the stream, respectively.
    ARRAY     ((byte)0x31, false, true, null, null),
    NULL      ((byte)0x32, false, false, null, null),
    ;

    /**
     * A map to lookup the ABIToken for a given class (leaf-type only).
     */
    public static final Map<Class<?>, ABIToken> STANDARD_LEAF_TYPE_MAP = new AionMap<>();

    static {
        for(ABIToken token : ABIToken.values()) {
            if(token.isLeafType) {
                STANDARD_LEAF_TYPE_MAP.put(token.standardType, token);
            }
        }
    }

    public final byte identifier;
    public final boolean isLeafType;
    public final boolean hasSizeField;
    public final Class<?> abiType;
    public final Class<?> standardType;

    private ABIToken(byte identifier, boolean isLeafType, boolean hasSizeField, Class<?> abiType, Class<?> standardType) {
        this.identifier = identifier;
        this.isLeafType = isLeafType;
        this.hasSizeField = hasSizeField;
        this.abiType = abiType;
        this.standardType = standardType;
    }
}
