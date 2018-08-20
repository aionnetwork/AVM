package org.aion.avm.core;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;


public class ShadowCoverageTarget {
    private static final JavaLang javaLang;
    static {
        javaLang = new JavaLang();
    }

    public static byte[] main() {
        byte[] input = BlockchainRuntime.getData();
        return ABIDecoder.decodeAndRunWithObject(new ShadowCoverageTarget(), input);
    }

    public static void populate_JavaLang() {
        javaLang.populate();
    }

    public static int getHash_JavaLang() {
        return javaLang.buildHash();
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
}
