package org.aion.avm.rt;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class TxDataDecoder {
    /*
     * ABI arguments descriptor symbols and the elementary type sizes.
     */
    public static final char BYTE = 'B';
    public static final int  BYTE_SIZE = 1;

    public static final char BOOLEAN = 'Z';
    public static final int  BOOLEAN_SIZE = 1;

    public static final char CHAR = 'C';
    public static final int  CHAR_SIZE = 2;

    public static final char SHORT = 'S';
    public static final int  SHORT_SIZE = 2;

    public static final char INT = 'I';
    public static final int  INT_SIZE = 4;

    public static final char FLOAT = 'F';
    public static final int  FLOAT_SIZE = 4;

    public static final char LONG = 'L';
    public static final int  LONG_SIZE = 8;

    public static final char DOUBLE = 'D';
    public static final int  DOUBLE_SIZE = 8;

    public static final char ARRAY_S = '[';
    public static final char ARRAY_E = ']';

    public static class MethodCaller {
        String methodName;
        List<Object> arguments;

        MethodCaller(String methodName, List<Object> arguments) {
            this.methodName = methodName;
            this.arguments  = arguments;
        }
    }

    public MethodCaller decode(byte[] txData) throws InvalidTxDataException, UnsupportedEncodingException {
        String decoded = new String(txData, "UTF-8");

        int m1 = decoded.indexOf('<');
        int m2 = decoded.indexOf('>');
        if (m1 == -1 || m2 == -1 || txData[m1] != 0x3C || txData[m2] != 0x3E) {
            throw new InvalidTxDataException();
        }

        String methodName = decoded.substring(0, m1);
        String argsDescriptor = decoded.substring(m1+1, m2);

        List<Object> arguments = getArguments(txData, m2+1, argsDescriptor);

        MethodCaller mc = new MethodCaller(methodName, arguments);
        return mc;
    }

    private List<Object> getArguments(byte[] txData, int start, String argsDescriptor) throws InvalidTxDataException, UnsupportedEncodingException {
        List<Object> args = new ArrayList<>();
        int arrayDimension = 0;
        int m = 0;
        int n = 0;
        int remainingDataSize = txData.length - start;

        for (int idx = 0; idx < argsDescriptor.length(); idx++) {
            char c = argsDescriptor.charAt(idx);

            switch (c) {
                case BYTE:
                    args.add(txData[start]);
                    start += BYTE_SIZE;
                    remainingDataSize -= BYTE_SIZE;
                    break;
                case BOOLEAN:
                    boolean b = (txData[start] != 0);
                    args.add(b);
                    start += BOOLEAN_SIZE;
                    remainingDataSize -= BOOLEAN_SIZE;
                    break;
                case CHAR:
                    byte[] bytes = {txData[start], txData[start + 1]};
                    char c1 = (new String(bytes, "UTF-16")).charAt(0);
                    args.add(c1);
                    start += CHAR_SIZE;
                    remainingDataSize -= CHAR_SIZE;
                    break;
                case SHORT:
                    short s = (short)((txData[start]<<8)&0xFF00 | txData[start+1]&0xFF);
                    args.add(s);
                    start += SHORT_SIZE;
                    remainingDataSize -= SHORT_SIZE;
                    break;
                case INT:
                    int i = (int)((txData[start]<<24)&0xFF000000 | (txData[start+1]<<16)&0xFF0000 |
                                  (txData[start+2]<<8)&0xFF00 | txData[start+3]&0xFF);
                    args.add(i);
                    start += INT_SIZE;
                    remainingDataSize -= INT_SIZE;
                    break;
                case FLOAT:
                    float f = (float) ((txData[start]<<24)&0xFF000000 | (txData[start+1]<<16)&0xFF0000 |
                                       (txData[start+2]<<8)&0xFF00 | txData[start+3]&0xFF);
                    args.add(f);
                    start += FLOAT_SIZE;
                    remainingDataSize -= FLOAT_SIZE;
                    break;
                case LONG:
                    long l = (long) ((txData[start]<<24)&0xFF000000 | (txData[start+1]<<16)&0xFF0000 |
                                     (txData[start+2]<<8)&0xFF00 | txData[start+3]&0xFF);
                    args.add(l);
                    start += LONG_SIZE;
                    remainingDataSize -= LONG_SIZE;
                    break;
                case DOUBLE:
                    double d = (double) ((txData[start]<<24)&0xFF000000 | (txData[start+1]<<16)&0xFF0000 |
                                         (txData[start+2]<<8)&0xFF00 | txData[start+3]&0xFF);
                    args.add(d);
                    start += DOUBLE_SIZE;
                    remainingDataSize -= DOUBLE_SIZE;
                    break;
                case ARRAY_S:
                    break;
                default:
                    throw new InvalidTxDataException();
            }
        }

        return args;
    }
}
