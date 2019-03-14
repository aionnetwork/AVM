package org.aion.avm.userlib.abi;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ABIEncoderRandomTest {

    private static Random random;

    private int numberOfArgs;
    private Object[] varArgs;
    private ABICodec.Tuple[] argTuples;
    private int[] argTypes;

    @Before
    public void testSetup() {
        random = new Random();
        long seed = random.nextLong();
        System.out.println("Test seed is " + seed);
        random.setSeed(seed);

        numberOfArgs = random.nextInt(10);
        varArgs = new Object[numberOfArgs];
        argTuples = new ABICodec.Tuple[numberOfArgs];
        argTypes = new int[numberOfArgs];
    }

    private static String getRandomString(int max) {
        int length = random.nextInt(max) + 5;
        String allChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(allChars.charAt(random.nextInt(62)));
        }
        return sb.toString();
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

    private void addRandomType(int type, int index) {
        switch (type) {
            case 1:
                addByte(index);
                break;
            case 2:
                addBool(index);
                break;
            case 3:
                addChar(index);
                break;
            case 4:
                addShort(index);
                break;
            case 5:
                addInt(index);
                break;
            case 6:
                addLong(index);
                break;
            case 7:
                addFloat(index);
                break;
            case 8:
                addDouble(index);
                break;
            case 9:
                addByteArray(index);
                break;
            case 10:
                addBoolArray(index);
                break;
            case 11:
                addCharArray(index);
                break;
            case 12:
                addShortArray(index);
                break;
            case 13:
                addIntArray(index);
                break;
            case 14:
                addLongArray(index);
                break;
            case 15:
                addFloatArray(index);
                break;
            case 16:
                addDoubleArray(index);
                break;
            case 17:
                addString(index);
                break;
            case 18:
                addIntArray2D(index);
                break;
        }
        argTypes[index] = type;
    }

    private void assertArray(Object actual, int index) {
        switch (argTypes[index]) {
            case 9:
                Assert.assertArrayEquals((byte[]) argTuples[index].value, (byte[]) actual);
                break;
            case 10:
                Assert.assertArrayEquals((boolean[]) argTuples[index].value, (boolean[]) actual);
                break;
            case 11:
                Assert.assertArrayEquals((char[]) argTuples[index].value, (char[]) actual);
                break;
            case 12:
                Assert.assertArrayEquals((short[]) argTuples[index].value, (short[]) actual);
                break;
            case 13:
                Assert.assertArrayEquals((int[]) argTuples[index].value, (int[]) actual);
                break;
            case 14:
                Assert.assertArrayEquals((long[]) argTuples[index].value, (long[]) actual);
                break;
            case 15:
                Assert
                    .assertTrue(Arrays.equals((float[]) argTuples[index].value, (float[]) actual));
                break;
            case 16:
                Assert.assertArrayEquals((double[]) argTuples[index].value, (double[]) actual, 0.1);
                break;
            case 18:
                Assert.assertArrayEquals((int[][]) argTuples[index].value, (int[][]) actual);
                break;
            default:
                Assert.fail("Not an array, test failed");
        }
    }

    @Test
    public void testRandom() {
        String methodName = getRandomString(9);

        for (int i = 0; i < numberOfArgs; i++) {
            addRandomType(random.nextInt(18) + 1, i);
        }

        byte[] encoded = ABIEncoder.encodeMethodArguments(methodName, varArgs);
        List<ABICodec.Tuple> list = ABICodec.parseEverything(encoded);
        Assert.assertEquals(numberOfArgs + 1, list.size());
        Assert.assertEquals(methodName, list.get(0).value);
        for (int i = 0; i < numberOfArgs; i++) {
            Assert.assertEquals(argTuples[i].standardType, list.get(i + 1).standardType);
            if (argTuples[i].standardType.isArray()) {
                assertArray(list.get(i + 1).value, i);
            } else {
                Assert.assertEquals(argTuples[i].value, list.get(i + 1).value);
            }

        }
    }

    // This test will break if any change is made to the ABI encoding specification
    @Test
    public void testFixed() {
        random.setSeed(8239882748599547375L);

        numberOfArgs = random.nextInt(10);
        varArgs = new Object[numberOfArgs];
        argTuples = new ABICodec.Tuple[numberOfArgs];
        argTypes = new int[numberOfArgs];

        byte[] encoded = {33, 0, 10, 85, 79, 76, 70, 70, 78, 89, 114, 99, 81, 6, 123, 88, 120, 23,
            -87, 57, -88, 35, 22, 0, 19, 79, 39, -96, 14, 54, -45, -94, -91, -24, -32, -39, -84,
            -21, 118, -11, 48, -7, -48, 62, 8, 80, 91, -45, -86, -7, 127, 65, -38, 16, 23, -34, 73,
            -2, -93, -17, 112, 84, 51, 48, -18, -78, -34, -1, -7, 94, 74, -102, 14, 97, -115, 47,
            -53, 58, -23, -93, -77, 19, 41, 124, -60, 116, -35, -14, 125, 121, -92, -74, 64, 32,
            -117, 113, -7, 13, -22, -40, 47, -53, 122, 10, -32, -55, -128, -89, 90, 93, -65, -122,
            -20, -43, -2, -6, -51, -22, 96, -97, 102, 105, -98, -97, 11, -49, -99, 78, -98, 56, -13,
            -39, 116, 47, 57, 41, -18, 7, -43, -42, -11, 104, 88, 24, -10, 3, -39, -49, 27, -128,
            -70, 0, 38, 105, -50, -20, -27, -46, 73, 55, -25, 117, -91, 49, 11, 118, 124, -66, -44,
            4, 83, 13, -96, 97, -63, 76, -28
        };

        String methodName = getRandomString(9);

        for (int i = 0; i < numberOfArgs; i++) {
            addRandomType(random.nextInt(18) + 1, i);
        }
        List<ABICodec.Tuple> list = ABICodec.parseEverything(encoded);
        Assert.assertEquals(numberOfArgs + 1, list.size());
        Assert.assertEquals(methodName, list.get(0).value);
        for (int i = 0; i < numberOfArgs; i++) {
            Assert.assertEquals(argTuples[i].standardType, list.get(i + 1).standardType);
            if (argTuples[i].standardType.isArray()) {
                assertArray(list.get(i + 1).value, i);
            } else {
                Assert.assertEquals(argTuples[i].value, list.get(i + 1).value);
            }

        }
    }
}
