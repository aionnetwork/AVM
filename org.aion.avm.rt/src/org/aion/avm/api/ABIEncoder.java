package org.aion.avm.api;

import org.aion.avm.arraywrapper.*;
import org.aion.avm.internal.IObject;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ABIEncoder{
    public enum ABITypes{
        avm_BYTE    ('B', 1, new String[]{"byte", "java.lang.Byte", "org.aion.avm.shadow.java.lang.Byte", "org.aion.avm.arraywrapper.ByteArray"}) {
            @Override
            public byte[] encode(Object data) {
                return new byte[]{(byte)data};
            }
        },
        avm_BOOLEAN ('Z', 1, new String[]{"boolean", "java.lang.Boolean", "org.aion.avm.shadow.java.lang.Boolean"}) {
            @Override
            public byte[] encode(Object data) {
                return new byte[]{(byte) (((boolean)data) ? 1 : 0)};
            }
        },
        avm_CHAR    ('C', 0, new String[]{"char", "java.lang.Character", "org.aion.avm.shadow.java.lang.Character", "org.aion.avm.arraywrapper.CharArray"}) { // variable length
            @Override
            public byte[] encode(Object data) {
                return Character.toString((char)data).getBytes();
            }
        },
        avm_SHORT   ('S', 2, new String[]{"short", "java.lang.Short", "org.aion.avm.shadow.java.lang.Short", "org.aion.avm.arraywrapper.ShortArray"}) {
            @Override
            public byte[] encode(Object data) {
                return ByteBuffer.allocate(2).putShort((short)data).array();
            }
        },
        avm_INT     ('I', 4, new String[]{"int", "java.lang.Integer", "org.aion.avm.shadow.java.lang.Integer", "org.aion.avm.arraywrapper.IntArray"}) {
            @Override
            public byte[] encode(Object data) {
                return ByteBuffer.allocate(4).putInt((int)data).array();
            }
        },
        avm_LONG    ('L', 8, new String[]{"long", "java.lang.Long", "org.aion.avm.shadow.java.lang.Long", "org.aion.avm.arraywrapper.LongArray"}) {
            @Override
            public byte[] encode(Object data) {
                return ByteBuffer.allocate(8).putLong((long)data).array();
            }
        },
        avm_FLOAT   ('F', 4, new String[]{"float", "java.lang.Float", "org.aion.avm.shadow.java.lang.Float", "org.aion.avm.arraywrapper.FloatArray"}) {
            @Override
            public byte[] encode(Object data) {
                return ByteBuffer.allocate(4).putFloat((float)data).array();
            }
        },
        avm_DOUBLE  ('D', 8, new String[]{"double", "java.lang.Double", "org.aion.avm.shadow.java.lang.Double", "org.aion.avm.arraywrapper.DoubleArray"}) {
            @Override
            public byte[] encode(Object data) {
                return ByteBuffer.allocate(8).putDouble((double)data).array();
            }
        };

        private final char symbol;
        private final int  bytes;
        private final String[] identifiers;

        ABITypes(char symbol, int bytes, String[] identifiers) {
            this.symbol = symbol;
            this.bytes  = bytes;
            this.identifiers = identifiers;
        }

        public abstract byte[] encode(Object data);
    }

    private static Map<String, ABITypes> ABITypesMap = null;

    /*
     * Runtime-facing implementation.
     */
    public static ByteArray avm_encodeMethodArguments(org.aion.avm.shadow.java.lang.String methodAPI, ObjectArray arguments)  throws InvalidTxDataException {
        return new ByteArray(encodeMethodArguments(methodAPI.toString(), arguments));
    }

    public static ByteArray avm_encodeOneObject(IObject data) throws InvalidTxDataException {
        return new ByteArray(encodeOneObject(data)[1]);
    }


    /*
     * Underlying implementation.
     */
    public static byte[] encodeMethodArguments(String methodName, ObjectArray arguments)  throws InvalidTxDataException {
        // encode each argument
        byte[][][] encodedData = new byte[arguments.length()][][];
        int numOfBytes = 0;
        for (int idx = 0; idx < arguments.length(); idx++) {
            encodedData[idx] = encodeOneObject(arguments.get(idx));
            numOfBytes += encodedData[idx][0].length + encodedData[idx][1].length;
        }

        byte[] ret = new byte[(methodName + "<>").getBytes().length + numOfBytes];

        // copy the method name
        int pos = (methodName + "<").getBytes().length;
        System.arraycopy((methodName + "<").getBytes(), 0, ret, 0, pos);

        // copy the descriptors
        for (int idx = 0; idx < arguments.length(); pos += encodedData[idx][0].length, idx ++) {
            System.arraycopy(encodedData[idx][0], 0, ret, pos, encodedData[idx][0].length);
        }
        System.arraycopy(">".getBytes(), 0, ret, pos, ">".getBytes().length);
        pos += ">".getBytes().length;

        // copy the encoded data
        for (int idx = 0; idx < arguments.length(); pos += encodedData[idx][1].length, idx ++) {
            System.arraycopy(encodedData[idx][1], 0, ret, pos, encodedData[idx][1].length);
        }
        return ret;
    }

    public static byte[][] encodeOneObject(Object data) throws InvalidTxDataException {
        String className = data.getClass().getName();

        if (className.equals("org.aion.avm.arraywrapper.ObjectArray")) {
            // data is a 2D array
            return encode2DArray((ObjectArray)data,
                    mapABITypes(((ObjectArray)data).getUnderlying().getClass().getName()));
        }
        else if (className.startsWith("org.aion.avm.arraywrapper.")) {
            // data is an 1D array
            return encode1DArray((Array)data, mapABITypes(className));
        }
        else {
            // data should not be an array
            byte[][] ret = new byte[2][];
            ABITypes type = mapABITypes(className);
            if (type != null) {
                ret[0] = Character.toString(type.symbol).getBytes();
                if (className.startsWith("org.aion.avm.shadow.java.lang.")) {
                    switch (className.substring(30)) {
                        case "Byte":
                            ret[1] = type.encode(((org.aion.avm.shadow.java.lang.Byte)data).avm_byteValue());
                            break;
                        case "Boolean":
                            ret[1] = type.encode(((org.aion.avm.shadow.java.lang.Boolean)data).avm_booleanValue());
                            break;
                        case "Char":
                            ret[1] = type.encode(((org.aion.avm.shadow.java.lang.Character)data).avm_charValue());
                            break;
                        case "Short":
                            ret[1] = type.encode(((org.aion.avm.shadow.java.lang.Short)data).avm_shortValue());
                            break;
                        case "Integer":
                            ret[1] = type.encode(((org.aion.avm.shadow.java.lang.Integer)data).avm_intValue());
                            break;
                        case "Long":
                            ret[1] = type.encode(((org.aion.avm.shadow.java.lang.Long)data).avm_longValue());
                            break;
                        case "Float":
                            ret[1] = type.encode(((org.aion.avm.shadow.java.lang.Float)data).avm_floatValue());
                            break;
                        case "Double":
                            ret[1] = type.encode(((org.aion.avm.shadow.java.lang.Double)data).avm_doubleValue());
                            break;
                        default:
                            throw new InvalidTxDataException();
                    }
                } else {
                    ret[1] = type.encode(data);
                }
                return ret;
            }
            else {
                throw new InvalidTxDataException();
            }
        }
    }

    public static byte[][] encode1DArray(Array data, ABITypes type) {
        byte[][] ret = new byte[2][]; // [0]: descriptor; [1]: encoded data

        // descriptor bytes
        ret[0] = ("[" + type.symbol + String.valueOf(data.length()) + "]").getBytes();

        // encoded data bytes
        if (type == ABITypes.avm_CHAR) {
            ret[1] = String.valueOf(((CharArray)data).getUnderlying()).getBytes();
        }
        else {
            ret[1] = new byte[data.length() * type.bytes];
            for (int i = 0, idx = 0; idx < data.length(); i += type.bytes, idx ++) {
                System.arraycopy(type.encode(data.getAsObject(idx)), 0, ret[1], i, type.bytes);
            }
        }

        return ret;
    }

    public static byte[][] encode2DArray(ObjectArray data, ABITypes type) throws InvalidTxDataException {
        byte[][] ret = new byte[2][]; // [0]: descriptor; [1]: encoded data

        Object[] underlying = data.getUnderlying();
        int totalSize = 0;

        // descriptor bytes
        String argumentDescriptor = "[[" + type.symbol + "]" + String.valueOf(underlying.length) + "]";
        for (int i = 0; i < underlying.length; i ++) {
            argumentDescriptor += "(" + String.valueOf(((Array)underlying[i]).length()) + ")";
            totalSize += ((Array)underlying[i]).length();
        }
        ret[0] = argumentDescriptor.getBytes();

        // encoded data bytes
        if (type == ABITypes.avm_CHAR) {
            String dataS = "";
            for (Object charArray : underlying) {
                dataS += String.valueOf(((CharArray)charArray).getUnderlying());
            }
            ret[1] = dataS.getBytes();
        }
        else {
            ret[1] = new byte[totalSize * type.bytes];
            int i = 0;
            for (Object array : underlying) {
                for (int idx = 0; idx < ((Array)array).length(); i += type.bytes, idx ++) {
                    System.arraycopy(type.encode(((Array)array).getAsObject(idx)), 0, ret[1], i, type.bytes);
                }
            }
        }

        return ret;
    }

    private static ABITypes mapABITypes(String identifier) {
        if (ABITypesMap == null) {
            ABITypesMap = new HashMap<>();
            for (ABITypes abiTypes : ABITypes.values()) {
                for (String id : abiTypes.identifiers) {
                    ABITypesMap.put(id, abiTypes);
                }
            }
        }
        return ABITypesMap.get(identifier);
    }
}