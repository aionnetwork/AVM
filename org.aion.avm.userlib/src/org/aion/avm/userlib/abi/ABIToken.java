package org.aion.avm.userlib.abi;

import org.aion.avm.api.Address;

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
    BYTE      ((byte)0x01, true, false, Byte.class),
    // (boolean is encoded as a byte)
    BOOLEAN   ((byte)0x02, true, false, Boolean.class),
    CHAR      ((byte)0x03, true, false, Character.class),
    SHORT     ((byte)0x04, true, false, Short.class),
    INT       ((byte)0x05, true, false, Integer.class),
    LONG      ((byte)0x06, true, false, Long.class),
    FLOAT     ((byte)0x07, true, false, Float.class),
    DOUBLE    ((byte)0x08, true, false, Double.class),

    A_BYTE    ((byte)0x11, true, true, byte[].class),
    // (boolean is encoded as a byte)
    A_BOOLEAN ((byte)0x12, true, true, boolean[].class),
    A_CHAR    ((byte)0x13, true, true, char[].class),
    A_SHORT   ((byte)0x14, true, true, short[].class),
    A_INT     ((byte)0x15, true, true, int[].class),
    A_LONG    ((byte)0x16, true, true, long[].class),
    A_FLOAT   ((byte)0x17, true, true, float[].class),
    A_DOUBLE  ((byte)0x18, true, true, double[].class),

    // Note that the string is described as number of UTF-8 encoded bytes, so each element is actually 1 byte.
    STRING    ((byte)0x21, true, true, String.class),
    ADDRESS   ((byte)0x22, true, false, Address.class),

    // ARRAY and NULL can't have sizes since they only decorate other types or represent placeholders in the stream, respectively.
    ARRAY     ((byte)0x31, false, true, null),
    NULL      ((byte)0x32, false, false, null),
    ;

    public static ABIToken getTokenFromIdentifier(byte identifier) {
        if(identifier == BYTE.identifier) {
            return BYTE;
        } else if (identifier == BOOLEAN.identifier) {
            return BOOLEAN;
        } else if (identifier == CHAR.identifier) {
            return CHAR;
        } else if (identifier == SHORT.identifier) {
            return SHORT;
        } else if (identifier == INT.identifier) {
            return INT;
        } else if (identifier == LONG.identifier) {
            return LONG;
        } else if (identifier == FLOAT.identifier) {
            return FLOAT;
        } else if (identifier == DOUBLE.identifier) {
            return DOUBLE;
        } else if (identifier == A_BYTE.identifier) {
            return A_BYTE;
        } else if (identifier == A_BOOLEAN.identifier) {
            return A_BOOLEAN;
        } else if (identifier == A_CHAR.identifier) {
            return A_CHAR;
        } else if (identifier == A_SHORT.identifier) {
            return A_SHORT;
        } else if (identifier == A_INT.identifier) {
            return A_INT;
        } else if (identifier == A_LONG.identifier) {
            return A_LONG;
        } else if (identifier == A_FLOAT.identifier) {
            return A_FLOAT;
        } else if (identifier == A_DOUBLE.identifier) {
            return A_DOUBLE;
        } else if (identifier == STRING.identifier) {
            return STRING;
        } else if (identifier == ADDRESS.identifier) {
            return ADDRESS;
        } else if (identifier == ARRAY.identifier) {
            return ARRAY;
        } else if (identifier == NULL.identifier) {
            return NULL;
        } else {
            return null;
        }
    }

    public static byte getIdentifierOfLeafType(Class<?> clazz) {
        if(clazz == Byte.class) {
            return BYTE.identifier;
        } else if (clazz == Boolean.class) {
            return BOOLEAN.identifier;
        } else if (clazz == Character.class) {
            return CHAR.identifier;
        } else if (clazz == Short.class) {
            return SHORT.identifier;
        } else if (clazz == Integer.class) {
            return INT.identifier;
        } else if (clazz == Long.class) {
            return LONG.identifier;
        } else if (clazz == Float.class) {
            return FLOAT.identifier;
        } else if (clazz == Double.class) {
            return DOUBLE.identifier;
        } else if (clazz == byte[].class) {
            return A_BYTE.identifier;
        } else if (clazz == boolean[].class) {
            return A_BOOLEAN.identifier;
        } else if (clazz == char[].class) {
            return A_CHAR.identifier;
        } else if (clazz == short[].class) {
            return A_SHORT.identifier;
        } else if (clazz == int[].class) {
            return A_INT.identifier;
        } else if (clazz == long[].class) {
            return A_LONG.identifier;
        } else if (clazz == float[].class) {
            return A_FLOAT.identifier;
        } else if (clazz == double[].class) {
            return A_DOUBLE.identifier;
        } else if (clazz == String.class) {
            return STRING.identifier;
        } else if (clazz == Address.class) {
            return ADDRESS.identifier;
        } else {
            return 0x00;
        }
    }

    public final byte identifier;
    public final boolean isLeafType;
    public final boolean hasSizeField;
    public final Class<?> type;

    private ABIToken(byte identifier, boolean isLeafType, boolean hasSizeField, Class<?> type) {
        this.identifier = identifier;
        this.isLeafType = isLeafType;
        this.hasSizeField = hasSizeField;
        this.type = type;
    }
}
