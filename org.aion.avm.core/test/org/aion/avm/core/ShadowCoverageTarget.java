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
import org.aion.avm.api.BlockchainRuntime;


public class ShadowCoverageTarget {
    private static final JavaLang javaLang;
    private static final JavaMath javaMath;
    static {
        javaLang = new JavaLang();
        javaMath = new JavaMath();
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

    public static int populate_JavaMath() {
        javaMath.populate();
        return javaMath.buildHash();
    }

    public static int getHash_JavaMath() {
        return javaMath.buildHash();
    }

    public static int runBasicNio() {
        int capacity = 50;
        ByteBuffer aByteBuffer = ByteBuffer.allocate(capacity);
        for (int i = 0; i < capacity; ++i) {
            aByteBuffer.put((byte)i);
        }
        aByteBuffer.flip();
        ByteOrder aByteOrder = aByteBuffer.order();
        CharBuffer aCharBuffer = aByteBuffer.asCharBuffer().position(1);
        DoubleBuffer aDoubleBuffer = aByteBuffer.asDoubleBuffer().position(1);
        FloatBuffer aFloatBuffer = aByteBuffer.asFloatBuffer().position(1);
        IntBuffer aIntBuffer = aByteBuffer.asIntBuffer().position(1);
        LongBuffer aLongBuffer = aByteBuffer.asLongBuffer().position(1);
        ShortBuffer aShortBuffer = aByteBuffer.asShortBuffer().position(1);
        
        return aByteBuffer.toString().hashCode()
                + aByteOrder.toString().hashCode()
                + aCharBuffer.toString().hashCode()
                + aDoubleBuffer.toString().hashCode()
                + aFloatBuffer.toString().hashCode()
                + aIntBuffer.toString().hashCode()
                + aLongBuffer.toString().hashCode()
                + aShortBuffer.toString().hashCode()
                ;
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
}
