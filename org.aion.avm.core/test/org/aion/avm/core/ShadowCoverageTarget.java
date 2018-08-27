package org.aion.avm.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;


public class ShadowCoverageTarget {
    private static final JavaLang javaLang;
    private static final JavaMath javaMath;
    private static final JavaNio javaNio;
    private static final Api api;
    static {
        javaLang = new JavaLang();
        javaMath = new JavaMath();
        javaNio = new JavaNio();
        api = new Api();
    }

    public static byte[] main() {
        byte[] input = BlockchainRuntime.getData();
        return ABIDecoder.decodeAndRunWithObject(new ShadowCoverageTarget(), input);
    }

    public static int populate_JavaLang() {
        javaLang.populate();
        return javaLang.buildHash();
    }

    public static int getHash_JavaLang() {
        return javaLang.buildHash();
    }

    public static boolean verifyReentrantChange_JavaLang() {
        // Verify reentrant hash before call.
        int localStartHash = javaLang.buildHash();
        int remoteStartHash = reentrantMethodWithoutArgs("getHash_JavaLang");
        if (localStartHash != remoteStartHash) {
            throw new AssertionError();
        }
        
        // Check reentrant commit is observed.
        int remoteNextHash = reentrantMethodWithoutArgs("populate_JavaLang");
        int localNextHash = javaLang.buildHash();
        return (remoteNextHash == localNextHash);
    }

    public static int populate_JavaMath() {
        javaMath.populate();
        return javaMath.buildHash();
    }

    public static int getHash_JavaMath() {
        return javaMath.buildHash();
    }

    public static boolean verifyReentrantChange_JavaMath() {
        // Verify reentrant hash before call.
        int localStartHash = javaMath.buildHash();
        int remoteStartHash = reentrantMethodWithoutArgs("getHash_JavaMath");
        if (localStartHash != remoteStartHash) {
            throw new AssertionError();
        }
        
        // Check reentrant commit is observed.
        int remoteNextHash = reentrantMethodWithoutArgs("populate_JavaMath");
        int localNextHash = javaMath.buildHash();
        return (remoteNextHash == localNextHash);
    }

    public static int populate_JavaNio() {
        javaNio.populate();
        return javaNio.buildHash();
    }

    public static int getHash_JavaNio() {
        return javaNio.buildHash();
    }

    public static boolean verifyReentrantChange_JavaNio() {
        // Verify reentrant hash before call.
        int localStartHash = javaNio.buildHash();
        int remoteStartHash = reentrantMethodWithoutArgs("getHash_JavaNio");
        if (localStartHash != remoteStartHash) {
            throw new AssertionError();
        }
        
        // Check reentrant commit is observed.
        int remoteNextHash = reentrantMethodWithoutArgs("populate_JavaNio");
        int localNextHash = javaNio.buildHash();
        return (remoteNextHash == localNextHash);
    }

    public static int populate_Api() {
        api.populate();
        return api.buildHash();
    }

    public static int getHash_Api() {
        return api.buildHash();
    }

    public static boolean verifyReentrantChange_Api() {
        // Verify reentrant hash before call.
        int localStartHash = api.buildHash();
        int remoteStartHash = reentrantMethodWithoutArgs("getHash_Api");
        if (localStartHash != remoteStartHash) {
            throw new AssertionError();
        }
        
        // Check reentrant commit is observed.
        int remoteNextHash = reentrantMethodWithoutArgs("populate_Api");
        int localNextHash = api.buildHash();
        return (remoteNextHash == localNextHash);
    }


    private static int reentrantMethodWithoutArgs(String methodName) {
        // Call this method via the runtime.
        long value = 1;
        byte[] data = ABIEncoder.encodeMethodArguments(methodName);
        long energyLimit = 500000;
        byte[] response = BlockchainRuntime.call(BlockchainRuntime.getAddress(), value, data, energyLimit).getReturnData();
        return ((Integer)ABIDecoder.decodeOneObject(response)).intValue();
    }


    private static class JavaLang {
        public Boolean aBoolean;
        public Byte aByte;
        public Character aCharacter;
        public Class<?> aClass;
        public Double aDouble;
        public EnumConstantNotPresentException aEnumConstantNotPresentException;
        public Exception aException;
        public Float aFloat;
        public Integer aInteger;
        public Long aLong;
        public Object aObject;
        public RuntimeException aRuntimeException;
        public Short aShort;
        public String aString;
        public StringBuffer aStringBuffer;
        public StringBuilder aStringBuilder;
        public Throwable aThrowable;
        public TypeNotPresentException aTypeNotPresentException;
        
        public int buildHash() {
            return aBoolean.hashCode()
                    + aByte.hashCode()
                    + aCharacter.hashCode()
                    + aClass.hashCode()
                    + aDouble.hashCode()
                    + aEnumConstantNotPresentException.hashCode()
                    + aException.hashCode()
                    + aFloat.hashCode()
                    + aInteger.hashCode()
                    + aLong.hashCode()
                    + aObject.hashCode()
                    + aRuntimeException.hashCode()
                    + aShort.hashCode()
                    + aString.hashCode()
                    + aStringBuffer.hashCode()
                    + aStringBuilder.hashCode()
                    + aThrowable.hashCode()
                    + aTypeNotPresentException.hashCode()
                    ;
        }
        
        public void populate() {
            aBoolean = Boolean.valueOf("true");
            aByte = Byte.valueOf((byte)5);
            aCharacter = Character.valueOf('f');
            aClass = String.class;
            aDouble = Double.valueOf(5.5d);
            aEnumConstantNotPresentException = new EnumConstantNotPresentException(Enum.class, "testing");
            aException = new IllegalStateException();
            aFloat = Float.valueOf(4.6f);
            aInteger = Integer.valueOf(6);
            aLong = Long.valueOf(9L);
            aObject = new Object();
            aRuntimeException = new NullPointerException();
            aShort = Short.valueOf((short)4);
            aString = "test string";
            aStringBuffer = new StringBuffer("buffer");
            aStringBuilder = new StringBuilder("builder");
            aThrowable = new AssertionError();
            aTypeNotPresentException = new TypeNotPresentException("type", null);
        }
    }


    private static class JavaMath {
        public BigDecimal aBigDecimal;
        public BigInteger aBigInteger;
        public MathContext aMathContext;
        public RoundingMode aRoundingMode;
        
        public int buildHash() {
            return aBigDecimal.hashCode()
                    + aBigInteger.hashCode()
                    + aMathContext.hashCode()
                    + aRoundingMode.hashCode()
                    ;
        }
        
        public void populate() {
            aBigDecimal = new BigDecimal("1234567890.0987654321");
            aBigInteger = new BigInteger("123456789000987654321");
            aMathContext = new MathContext(1);
            aRoundingMode = RoundingMode.UP;
        }
    }


    private static class JavaNio {
        public ByteBuffer aByteBuffer;
        public ByteOrder aByteOrder;
        public CharBuffer aCharBuffer;
        public DoubleBuffer aDoubleBuffer;
        public FloatBuffer aFloatBuffer;
        public IntBuffer aIntBuffer;
        public LongBuffer aLongBuffer;
        public ShortBuffer aShortBuffer;
        
        public int buildHash() {
            return aByteBuffer.hashCode()
                    + aByteOrder.hashCode()
                    + aCharBuffer.hashCode()
                    + aDoubleBuffer.hashCode()
                    + aFloatBuffer.hashCode()
                    + aIntBuffer.hashCode()
                    + aLongBuffer.hashCode()
                    + aShortBuffer.hashCode()
                    ;
        }
        
        public void populate() {
            int capacity = 50;
            aByteBuffer = ByteBuffer.allocate(capacity);
            for (int i = 0; i < capacity; ++i) {
                aByteBuffer.put((byte)i);
            }
            aByteBuffer.flip();
            aByteOrder = aByteBuffer.order();
            aCharBuffer = aByteBuffer.asCharBuffer().position(1);
            aDoubleBuffer = aByteBuffer.asDoubleBuffer().position(1);
            aFloatBuffer = aByteBuffer.asFloatBuffer().position(1);
            aIntBuffer = aByteBuffer.asIntBuffer().position(1);
            aLongBuffer = aByteBuffer.asLongBuffer().position(1);
            aShortBuffer = aByteBuffer.asShortBuffer().position(1);
        }
    }


    private static class Api {
        public Address aAddress;
        
        public int buildHash() {
            return aAddress.hashCode()
                    ;
        }
        
        public void populate() {
            byte[] raw = new byte[Address.LENGTH];
            for (int i = 0; i < raw.length; ++i) {
                raw[i] = (byte)i;
            }
            aAddress = new Address(raw);
        }
    }
}
