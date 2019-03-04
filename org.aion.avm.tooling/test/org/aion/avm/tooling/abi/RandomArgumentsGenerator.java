package org.aion.avm.tooling.abi;

import org.aion.avm.abi.internal.ABICodec;
import org.junit.Assert;

import java.util.Arrays;
import java.util.Random;

public class RandomArgumentsGenerator {

    private Random random;

    private int numberOfArgs;
    private Object[] varArgs;
    private ABICodec.Tuple[] argTuples;
    private Type[] argTypes;
    private String[] argDescriptors;
    private int upperBoundOfNumOfArgs;

    private static final int UpperBoundOfNumOfArgsTypes = 18;

    public RandomArgumentsGenerator() {
    }

    public String getRandomString(int max) {
        int length = random.nextInt(max) + 5;
        String allChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(allChars.charAt(random.nextInt(62)));
        }
        return sb.toString();
    }

    public int getNumberOfArgs() {
        return numberOfArgs;
    }

    public Object[] getVarArgs() {
        return varArgs;
    }

    public ABICodec.Tuple[] getArgTuples() {
        return argTuples;
    }

    public Type[] getArgTypes() {
        return argTypes;
    }

    public String[] getArgDescriptors() {
        return argDescriptors;
    }

    public void addRandomTypeAndRandomValues(int upperBoundOfNumOfArguments) {
        setupRandom();
        upperBoundOfNumOfArgs = upperBoundOfNumOfArguments;
        numberOfArgs = random.nextInt(upperBoundOfNumOfArgs);
        setupVariables(numberOfArgs);

        for (int i = 0; i < numberOfArgs; i++) {
            addRandomType(Type.fromOrdinal(random.nextInt(UpperBoundOfNumOfArgsTypes) + 1), i);
        }
    }

    public void addFixedTypesAndRandomValues(int numOfArgs) {
        setupRandom();
        numberOfArgs = numOfArgs < UpperBoundOfNumOfArgsTypes ? numOfArgs : UpperBoundOfNumOfArgsTypes;
        setupVariables(numberOfArgs);

        for (int i = 0; i < numberOfArgs; i++) {
            addRandomType(Type.fromOrdinal(i+1), i);
        }
    }

    private void setupRandom() {
        random = new Random();
        long seed = random.nextLong();
        System.out.println("Test seed is " + seed);
        random.setSeed(seed);
    }

    private void setupVariables(int numberOfArgs) {
        varArgs = new Object[numberOfArgs];
        argTuples = new ABICodec.Tuple[numberOfArgs];
        argTypes = new Type[numberOfArgs];
        argDescriptors = new String[numberOfArgs];
    }

    private void addByte(int i) {
        byte b = (byte) random.nextInt();
        varArgs[i] = b;
        argTuples[i] = new ABICodec.Tuple(Byte.class, b);
    }

    private void addBool(int i) {
        boolean b = random.nextBoolean();
        varArgs[i] = b;
        argTuples[i] = new ABICodec.Tuple(Boolean.class, b);
    }

    private void addChar(int i) {
        char b = (char) random.nextInt();
        varArgs[i] = b;
        argTuples[i] = new ABICodec.Tuple(Character.class, b);
    }

    private void addShort(int i) {
        short b = (short) random.nextInt();
        varArgs[i] = b;
        argTuples[i] = new ABICodec.Tuple(Short.class, b);
    }

    private void addInt(int i) {
        int b = random.nextInt();
        varArgs[i] = b;
        argTuples[i] = new ABICodec.Tuple(Integer.class, b);
    }

    private void addLong(int i) {
        long b = random.nextLong();
        varArgs[i] = b;
        argTuples[i] = new ABICodec.Tuple(Long.class, b);
    }

    private void addFloat(int i) {
        float b = random.nextFloat();
        varArgs[i] = b;
        argTuples[i] = new ABICodec.Tuple(Float.class, b);
    }

    private void addDouble(int i) {
        double b = random.nextDouble();
        varArgs[i] = b;
        argTuples[i] = new ABICodec.Tuple(Double.class, b);
    }

    private void addByteArray(int i) {
        byte[] b = new byte[random.nextInt(50)];
        random.nextBytes(b);
        varArgs[i] = b;
        argTuples[i] = new ABICodec.Tuple(byte[].class, b);
    }

    private void addBoolArray(int i) {
        int len = random.nextInt(50);
        boolean[] b = new boolean[len];
        for (int j = 0; j < len; j++) {
            b[j] = random.nextBoolean();
        }
        varArgs[i] = b;
        argTuples[i] = new ABICodec.Tuple(boolean[].class, b);
    }

    private void addCharArray(int i) {
        int len = random.nextInt(50);
        char[] b = new char[len];
        for (int j = 0; j < len; j++) {
            b[j] = (char) random.nextInt();
        }
        varArgs[i] = b;
        argTuples[i] = new ABICodec.Tuple(char[].class, b);
    }

    private void addShortArray(int i) {
        int len = random.nextInt(50);
        short[] b = new short[len];
        for (int j = 0; j < len; j++) {
            b[j] = (short) random.nextInt();
        }
        varArgs[i] = b;
        argTuples[i] = new ABICodec.Tuple(short[].class, b);
    }

    private void addIntArray(int i) {
        int[] b = getRandomIntArray();
        varArgs[i] = b;
        argTuples[i] = new ABICodec.Tuple(int[].class, b);
    }

    private void addLongArray(int i) {
        int len = random.nextInt(50);
        long[] b = new long[len];
        for (int j = 0; j < len; j++) {
            b[j] = random.nextLong();
        }
        varArgs[i] = b;
        argTuples[i] = new ABICodec.Tuple(long[].class, b);
    }

    private void addFloatArray(int i) {
        int len = random.nextInt(50);
        float[] b = new float[len];
        for (int j = 0; j < len; j++) {
            b[j] = random.nextFloat();
        }
        varArgs[i] = b;
        argTuples[i] = new ABICodec.Tuple(float[].class, b);
    }

    private void addDoubleArray(int i) {
        int len = random.nextInt(50);
        double[] b = new double[len];
        for (int j = 0; j < len; j++) {
            b[j] = random.nextDouble();
        }
        varArgs[i] = b;
        argTuples[i] = new ABICodec.Tuple(double[].class, b);
    }

    private void addString(int i) {
        String b = getRandomString(20);
        varArgs[i] = b;
        argTuples[i] = new ABICodec.Tuple(String.class, b);
    }

    private int[] getRandomIntArray() {
        int len = random.nextInt(50);
        int[] b = new int[len];
        for (int j = 0; j < len; j++) {
            if (random.nextInt(10) < 8) {
                b[j] = random.nextInt();
            }
        }
        return b;
    }

    private void addIntArray2D(int i) {
        int len = random.nextInt(50);
        int[][] b = new int[len][];

        for (int j = 0; j < len; j++) {
            b[j] = getRandomIntArray();
        }

        varArgs[i] = b;
        argTuples[i] = new ABICodec.Tuple(int[][].class, b);
    }

    private void addRandomType(Type type, int index) {
        switch (type) {
            case BYTE:
                addByte(index);
                break;
            case BOOLEAN:
                addBool(index);
                break;
            case CHAR:
                addChar(index);
                break;
            case SHORT:
                addShort(index);
                break;
            case INT:
                addInt(index);
                break;
            case LONG:
                addLong(index);
                break;
            case FLOAT:
                addFloat(index);
                break;
            case DOUBLE:
                addDouble(index);
                break;
            case BYTE_ARRAY:
                addByteArray(index);
                break;
            case BOOL_ARRAY:
                addBoolArray(index);
                break;
            case CHAR_ARRAY:
                addCharArray(index);
                break;
            case SHORT_ARRAY:
                addShortArray(index);
                break;
            case INT_ARRAY:
                addIntArray(index);
                break;
            case LONG_ARRAY:
                addLongArray(index);
                break;
            case FLOAT_ARRAY:
                addFloatArray(index);
                break;
            case DOUBLE_ARRAY:
                addDoubleArray(index);
                break;
            case STRING:
                addString(index);
                break;
            case INT_ARRAY_2D:
                addIntArray2D(index);
                break;
        }
        argTypes[index] = type;
        argDescriptors[index] = getTypeDescriptor(type);
    }

    private void assertArray(Object actual, int index) {
        switch (argTypes[index]) {
            case BYTE_ARRAY:
                Assert.assertArrayEquals((byte[]) argTuples[index].value, (byte[]) actual);
                break;
            case BOOL_ARRAY:
                Assert.assertArrayEquals((boolean[]) argTuples[index].value, (boolean[]) actual);
                break;
            case CHAR_ARRAY:
                Assert.assertArrayEquals((char[]) argTuples[index].value, (char[]) actual);
                break;
            case SHORT_ARRAY:
                Assert.assertArrayEquals((short[]) argTuples[index].value, (short[]) actual);
                break;
            case INT_ARRAY:
                Assert.assertArrayEquals((int[]) argTuples[index].value, (int[]) actual);
                break;
            case LONG_ARRAY:
                Assert.assertArrayEquals((long[]) argTuples[index].value, (long[]) actual);
                break;
            case FLOAT_ARRAY:
                Assert.assertTrue(Arrays.equals((float[]) argTuples[index].value, (float[]) actual));
                break;
            case DOUBLE_ARRAY:
                Assert.assertArrayEquals((double[]) argTuples[index].value, (double[]) actual, 0.1);
                break;
            case INT_ARRAY_2D:
                Assert.assertArrayEquals((int[][]) argTuples[index].value, (int[][]) actual);
                break;
            default:
                Assert.fail("Not an array, test failed");
        }
    }

    private String getTypeDescriptor(Type type) {
        String t = "";
        switch (type) {
            case BYTE:
                t = "B";
                break;
            case BOOLEAN:
                t = "Z";
                break;
            case CHAR:
                t = "C";
                break;
            case SHORT:
                t = "S";
                break;
            case INT:
                t = "I";
                break;
            case LONG:
                t = "J";
                break;
            case FLOAT:
                t = "F";
                break;
            case DOUBLE:
                t = "D";
                break;
            case BYTE_ARRAY:
                t = "[B";
                break;
            case BOOL_ARRAY:
                t = "[Z";
                break;
            case CHAR_ARRAY:
                t = "[C";
                break;
            case SHORT_ARRAY:
                t = "[S";
                break;
            case INT_ARRAY:
                t = "[I";
                break;
            case LONG_ARRAY:
                t = "[J";
                break;
            case FLOAT_ARRAY:
                t = "[F";
                break;
            case DOUBLE_ARRAY:
                t = "[D";
                break;
            case STRING:
                t = "Ljava/lang/String;";
                break;
            case INT_ARRAY_2D:
                t = "[[I";
                break;
        }
        return t;
    }
}
