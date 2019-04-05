package org.aion.avm.tooling;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import avm.Address;
import avm.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;


public class ShadowCoverageTarget {
    private static final JavaLang javaLang;
    private static final JavaMath javaMath;
    private static final Api api;
    static {
        javaLang = new JavaLang();
        javaMath = new JavaMath();
        api = new Api();
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(BlockchainRuntime.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("populate_JavaLang")) {
                return ABIEncoder.encodeOneInteger(populate_JavaLang());
            } else if (methodName.equals("getHash_JavaLang")) {
                return ABIEncoder.encodeOneInteger(getHash_JavaLang());
            } else if (methodName.equals("verifyReentrantChange_JavaLang")) {
                return ABIEncoder.encodeOneBoolean(verifyReentrantChange_JavaLang());
            } else if (methodName.equals("populate_JavaMath")) {
                return ABIEncoder.encodeOneInteger(populate_JavaMath());
            } else if (methodName.equals("getHash_JavaMath")) {
                return ABIEncoder.encodeOneInteger(getHash_JavaMath());
            } else if (methodName.equals("verifyReentrantChange_JavaMath")) {
                return ABIEncoder.encodeOneBoolean(verifyReentrantChange_JavaMath());
            } else if (methodName.equals("populate_Api")) {
                return ABIEncoder.encodeOneInteger(populate_Api());
            } else if (methodName.equals("getHash_Api")) {
                return ABIEncoder.encodeOneInteger(getHash_Api());
            } else if (methodName.equals("verifyReentrantChange_Api")) {
                return ABIEncoder.encodeOneBoolean(verifyReentrantChange_Api());
            } else {
                return new byte[0];
            }
        }
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
        BigInteger value = BigInteger.ZERO;
        byte[] data = ABIEncoder.encodeOneString(methodName);
        long energyLimit = 500000;
        byte[] response = BlockchainRuntime.call(BlockchainRuntime.getAddress(), value, data, energyLimit).getReturnData();
        ABIDecoder decoder = new ABIDecoder(response);
        return decoder.decodeOneInteger();
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
