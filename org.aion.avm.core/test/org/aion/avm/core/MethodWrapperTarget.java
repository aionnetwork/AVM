package org.aion.avm.core;

import avm.Blockchain;
import java.math.BigInteger;
import java.io.Serializable;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class MethodWrapperTarget {
    public enum E { YES, NO }
    private static int i;
    private String k;

    // To produce a <clinit>
    static {
        i = 2;
    }

    // To verify behaviour with inner class interactions.
    static class InnerClass {

        private void innerMethod(String s, boolean[] b) {}
    }

    // To ensure that the constructor doesn't get wrapped.
    public MethodWrapperTarget(String s) {
        this.k = s;
    }

    // <--------------------- calls with mixtures of return & param types ------------------------->

    public static byte[] main() {
        // This case happens when we call into the main via reflection directly and have no Blockchain.getData()
        try {
            Blockchain.getData();
        } catch (NullPointerException e) {
            return ABIEncoder.encodeOneString("testMain");
        }

        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String action = decoder.decodeOneString();

        if (action.equals("invokeVoidReturnMethod")) {
            returnStaticVoid();
            return ABIEncoder.encodeOneString("invokeVoidReturnMethod");
        } else if (action.equals("invokePrimitiveReturnMethod")) {
            byte b = decoder.decodeOneByte();
            char[] c = decoder.decodeOneCharacterArray();
            String s = decoder.decodeOneString();
            return ABIEncoder.encodeOneFloat(returnStaticFloatWithParams(b, c, s));
        } else if (action.equals("invokeArrayReturnMethod")) {
            return ABIEncoder.encodeOneIntegerArray(returnStaticArray());
        } else if (action.equals("invokeObjectReturnMethod")) {
            byte b = decoder.decodeOneByte();
            char[] c = decoder.decodeOneCharacterArray();
            String s = decoder.decodeOneString();
            return ABIEncoder.encodeOneString((String) returnStaticObjectWithParams(b, c, s));
        } else if (action.equals("invokeFinalMethod")) {
            return ABIEncoder.encodeOneBigInteger((BigInteger) finalMethod(E.YES));
        } else if (action.equals("invokeMethodWithTryCatch")) {
            return ABIEncoder.encodeOneIntegerArray(methodWithTryCatch(true));
        } else if (action.equals("invokeCallChainOfDepth3")) {
            // depth 1 will call into depth 2, which calls into depth 3.
            int i = decoder.decodeOneInteger();
            int i2 = decoder.decodeOneInteger();
            return ABIEncoder.encodeOne2DIntegerArray((int[][]) callChainDepth1(i, i2));
        } else if (action.equals("invokeRecursiveMethod")) {
            int i = decoder.decodeOneInteger();
            return ABIEncoder.encodeOneInteger(recurse(i));
        } else if (action.equals("invokeImplementationMethodDefinedInAbstractClass")) {
            return ABIEncoder.encodeOneLong(staticMethodWithImplementation(4, new NullPointerException()));
        } else if (action.equals("invokeImplementationOfAbstractMethod")) {
            int i = decoder.decodeOneInteger();
            long l = decoder.decodeOneLong();
            return ABIEncoder.encodeOneBigInteger(callInnerAbstractImpl(i, l));
        } else if (action.equals("invokeImplementationOfInterfaceMethod")) {
            String s = decoder.decodeOneString();
            return ABIEncoder.encodeOneString(callInnerInterfaceImpl(new InnerInterfaceImpl(), s));
        } else if (action.equals("invokeMethodThatThrowsException")) {
            callAndThrow();
        } else if (action.equals("invokeUnimplementedToStringOnMainClass")) {
            String s = decoder.decodeOneString();
            return ABIEncoder.encodeOneString(new MethodWrapperTarget(s).toString());
        } else if (action.equals("invokeDefaultMethod")) {
            String s = decoder.decodeOneString();
            return ABIEncoder.encodeOneLong(callDefaultMethod(new InnerInterfaceWithDefaultMethodImpl(), s));
        } else if (action.equals("invokeConstructors")) {
            String s1 = decoder.decodeOneString();
            String s2 = decoder.decodeOneString();
            return ABIEncoder.encodeOneString(callConstructors(s1, s2));
        }

        // Basically just a unique return type here to avoid conflating this case with a real case above.
        // Avoiding throwing an exception since an above target may want to do that as a legit use case. Null is also legit.
        return ABIEncoder.encodeOneFloatArray(new float[]{ 0, 0, 0, 0, 0 });
    }

    private static long returnStaticLong() {
        return -1;
    }

    public long returnLong() {
        return -1;
    }

    private static long returnStaticLongWithParams(byte b, char[] c, Object o) {
        return -1;
    }

    private long returnLongWithParams(int i, String s, String s2, String s3, String s4, Object o, byte b) {
        return -1;
    }

    private static int returnStaticInt() {
        return 0;
    }

    public int returnInt() {
        return 0;
    }

    private static int returnStaticIntWithParams(byte b, char[] c, Object o) {
        return 0;
    }

    private int returnIntWithParams(int i, String s, InnerClass s2, Enum e) {
        return 0;
    }

    private static char returnStaticChar() {
        return 'a';
    }

    public char returnChar() {
        return 'a';
    }

    private static char returnStaticCharWithParams(byte b, char[] c, Object o) {
        return 'a';
    }

    private char returnCharWithParams(int i, String s, String s2, Enum e) {
        return 'a';
    }

    private static byte returnStaticByte() {
        return 0x1;
    }

    public byte returnByte() {
        return 0x1;
    }

    private static byte returnStaticByteWithParams(byte b, char[] c, Object o) {
        return 0x1;
    }

    private byte returnByteWithParams(int i, String s, String s2, Enum e) {
        return 0x1;
    }

    private static short returnStaticShort() {
        return 2;
    }

    public short returnShort() {
        return 2;
    }

    private static short returnStaticShortWithParams(byte b, char[] c, Object o) {
        return 2;
    }

    private short returnShortWithParams(int i, String s, String s2, InnerClass e) {
        return 2;
    }

    private static boolean returnStaticBoolean() {
        return true;
    }

    public boolean returnBoolean() throws IllegalArgumentException {
        return true;
    }

    private static boolean returnStaticBooleanWithParams(byte b, char[] c, Object o) throws IndexOutOfBoundsException {
        return true;
    }

    private boolean returnBooleanWithParams(int i, String s, String s2, Exception e) {
        return true;
    }

    private static double returnStaticDouble() {
        return 1.0;
    }

    public double returnDouble() {
        return 1.0;
    }

    private static double returnStaticDoubleWithParams(byte b, char[] c, InnerClass o) {
        return 1.0;
    }

    private double returnDoubleWithParams(int i, String s, String s2, Enum e) {
        return 1.0;
    }

    private static float returnStaticFloat() {
        return (float) 0.0;
    }

    public float returnFloat() {
        return (float) 0.0;
    }

    private static float returnStaticFloatWithParams(byte b, char[] c, Object o) {
        return (float) 0.0;
    }

    private float returnFloatWithParams(int i, String s, String s2, Enum e) {
        return (float) 0.0;
    }

    private static void returnStaticVoid() {}

    public void returnVoid() {}

    private static void returnStaticVoidWithParams(byte b, char[] c, Object o) {}

    private void returnVoidWithParams(int i, String s, String s2, Enum e) {}

    private static int[] returnStaticArray() {
        return new int[]{ 5, 4, 3, 2, 1 };
    }

    public byte[][] returnArray() {
        return new byte[0][];
    }

    private static String[] returnStaticArrayWithParams(byte b, char[] c, Object o) {
        return new String[0];
    }

    private Object[][] returnArrayWithParams(int i, String s, String s2, Enum e) {
        return new String[0][];
    }

    private static String returnStaticObject() {
        return "";
    }

    public Object returnObject() {
        return new Object();
    }

    private static Serializable returnStaticObjectWithParams(byte b, char[] c, Object o) {
        return null;
    }

    private Number returnObjectWithParams(int i, InnerClass s, String s2, Enum e) {
        return BigInteger.ZERO;
    }

    // <---------------------------- some miscellaneous calls ------------------------------------->

    protected static final Object finalMethod(E e) {
        return BigInteger.TEN;
    }

    private static int[] methodWithTryCatch(boolean throwNull) {
        try {
            if (throwNull) {
                throw new NullPointerException();
            } else {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {

        } catch (NullPointerException e) {

        } finally {
            return new int[2];
        }
    }

    public static int recurse(int num) {
        if (num > 0) {
            return recurse(num - 1) + 1;
        } else {
            return 0;
        }
    }

    public static Object callChainDepth1(int i, int i2) {
        int[][] ints = new int[2][];
        ints[0] = callChainDepth2(i, i2);
        ints[1] = callChainDepth2(i2, i);
        return ints;
    }

    private static int[] callChainDepth2(int i, int i2) {
        int[] ints = new int[2];
        ints[0] = callChainDepth3(i, i);
        ints[1] = callChainDepth3(i2, i2);
        return ints;
    }

    private static int callChainDepth3(int i, int i2) {
        return i + i2;
    }

    public static abstract class InnerAbstract {
        public abstract BigInteger callInnerAbstract(int i, long l);
    }

    public static class InnerAbstractImpl extends InnerAbstract {
        @Override
        public BigInteger callInnerAbstract(int i, long l) {
            return BigInteger.valueOf(i * l);
        }
    }

    public static BigInteger callInnerAbstractImpl(int i, long l) {
        return new InnerAbstractImpl().callInnerAbstract(i, l);
    }

    public static interface InnerInterface {
        public String callInnerInterface(String s);
    }

    public static class InnerInterfaceImpl implements InnerInterface {
        @Override
        public String callInnerInterface(String s) {
            return "avm_" + s;
        }
    }

    private static String callInnerInterfaceImpl(InnerInterface i, String s) {
        return i.callInnerInterface(s);
    }

    protected static void callAndThrow() {
        throw new IllegalArgumentException();
    }

    public static interface InnerInterfaceWithDefaultMethod {
        public default long callDefault(String s) {
            return 51;
        }
    }

    public static class InnerInterfaceWithDefaultMethodImpl implements InnerInterfaceWithDefaultMethod {

    }

    public static long callDefaultMethod(InnerInterfaceWithDefaultMethod in, String s) {
        return in.callDefault(s);
    }

    public static class InnerConstructorClass {
        public final String string;

        public InnerConstructorClass() {
            this("");
        }

        public InnerConstructorClass(String s) {
            this.string = "args = " + s;
        }

        public InnerConstructorClass(String s1, String s2) {
            this(s1 + s2);
        }
    }

    public static String callConstructors(String s1, String s2) {
        InnerConstructorClass inner1 = new InnerConstructorClass();
        InnerConstructorClass inner2 = new InnerConstructorClass(s1);
        InnerConstructorClass inner3 = new InnerConstructorClass(s1, s2);
        return inner1.string + inner2.string + inner3.string;
    }

    // <---------------------------- calls into abstract class below ------------------------------>

    public void abstractVoidReturn(MethodWrapperAbstractTarget t) {
        t.abstractVoidReturn();
    }

    public static void abstractStaticVoidReturn(MethodWrapperAbstractTarget t) {
        t.abstractVoidReturn();
    }

    public void abstractVoidReturnWithParams(MethodWrapperAbstractTarget t) {
        t.abstractVoidReturnWithParams((byte) 0x2, 'b');
    }

    public static void abstractStaticVoidReturnWithParams(MethodWrapperAbstractTarget t) {
        t.abstractVoidReturnWithParams((byte) 0x2, 'b');
    }

    public long abstractPrimitiveReturn(MethodWrapperAbstractTarget t) {
        return t.abstractPrimitiveReturn();
    }

    public static long abstractStaticPrimitiveReturn(MethodWrapperAbstractTarget t) {
        return t.abstractPrimitiveReturn();
    }

    public int abstractPrimitiveReturnWithParams(MethodWrapperAbstractTarget t) {
        return t.abstractPrimitiveReturnWithParams("s", new int[2]);
    }

    public static int abstractStaticPrimitiveReturnWithParams(MethodWrapperAbstractTarget t) {
        return t.abstractPrimitiveReturnWithParams("s", new int[2]);
    }

    public byte[] abstractArrayReturn(MethodWrapperAbstractTarget t) {
        return t.abstractArrayReturn();
    }

    public static byte[] abstractStaticArrayReturn(MethodWrapperAbstractTarget t) {
        return t.abstractArrayReturn();
    }

    public String[][] abstractArrayReturnWithParams(MethodWrapperAbstractTarget t) {
        return t.abstractArrayReturnWithParams('a', new byte[1], 3, "s", 5);
    }

    public static String[][] abstractStaticArrayReturnWithParams(MethodWrapperAbstractTarget t) {
        return t.abstractArrayReturnWithParams('a', new byte[1], 3, "s", 5);
    }

    public Object abstractObjectReturn(MethodWrapperAbstractTarget t) {
        return t.abstractObjectReturn();
    }

    public static Object abstractStaticObjectReturn(MethodWrapperAbstractTarget t) {
        return t.abstractObjectReturn();
    }

    public Number abstractObjectReturnWithParams(MethodWrapperAbstractTarget t) {
        return t.abstractObjectReturnWithParams(BigInteger.ONE, 4, 1.0);
    }

    public static Number abstractStaticObjectReturnWithParams(MethodWrapperAbstractTarget t) {
        return t.abstractObjectReturnWithParams(BigInteger.ONE, 4, 1.0);
    }

    public long methodWithImplementation(MethodWrapperAbstractTarget t) {
        return t.methodWithImplementation();
    }

    public static long staticMethodWithImplementation(int i, Exception e) {
        return MethodWrapperAbstractTarget.methodWithImplementation(i, e);
    }

    // <--------------------------- calls into interface class below ------------------------------>

    public void interfaceVoidReturn(MethodWrapperInterfaceTarget t) {
        t.interfaceReturnVoid();
    }

    public static void interfaceStaticVoidReturn(MethodWrapperInterfaceTarget t) {
        t.interfaceReturnVoid();
    }

    public void interfaceVoidReturnWithParams(MethodWrapperInterfaceTarget t) {
        t.interfaceReturnVoidWithParams(new Object());
    }

    public static void interfaceStaticVoidReturnWithParams(MethodWrapperInterfaceTarget t) {
        t.interfaceReturnVoidWithParams(BigInteger.ZERO);
    }

    public double interfacePrimitiveReturn(MethodWrapperInterfaceTarget t) {
        return t.interfaceReturnPrimitive();
    }

    public static double interfaceStaticPrimitiveReturn(MethodWrapperInterfaceTarget t) {
        return t.interfaceReturnPrimitive();
    }

    public float interfacePrimitiveReturnWithParams(MethodWrapperInterfaceTarget t) {
        return t.interfaceReturnPrimitiveWithParams(1, 'a', "s");
    }

    public static float interfaceStaticPrimitiveReturnWithParams(MethodWrapperInterfaceTarget t) {
        return t.interfaceReturnPrimitiveWithParams(1, 'a', "s");
    }

    public boolean[] interfaceArrayReturn(MethodWrapperInterfaceTarget t) {
        return t.interfaceReturnArray();
    }

    public static boolean[] interfaceStaticArrayReturn(MethodWrapperInterfaceTarget t) {
        return t.interfaceReturnArray();
    }

    public Number[][] interfaceArrayReturnWithParams(MethodWrapperInterfaceTarget t) {
        return t.interfaceReturnArrayWithParams(BigInteger.TEN, (short) 0);
    }

    public static Number[][] interfaceStaticArrayReturnWithParams(MethodWrapperInterfaceTarget t) {
        return t.interfaceReturnArrayWithParams(BigInteger.TEN, (short) 0);
    }

    public StringBuilder interfaceObjectReturn(MethodWrapperInterfaceTarget t) {
        return t.interfaceReturnObject();
    }

    public static StringBuilder interfaceStaticObjectReturn(MethodWrapperInterfaceTarget t) {
        return t.interfaceReturnObject();
    }

    public Serializable interfaceObjectReturnWithParams(MethodWrapperInterfaceTarget t) {
        return t.interfaceReturnObjectWithParams(new NullPointerException(), new char[2], new int[1]);
    }

    public static Serializable interfaceStaticObjectReturnWithParams(MethodWrapperInterfaceTarget t) {
        return t.interfaceReturnObjectWithParams(new NullPointerException(), new char[2], new int[1]);
    }
}
