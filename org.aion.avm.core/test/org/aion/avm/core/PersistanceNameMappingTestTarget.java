package org.aion.avm.core;

import avm.Blockchain;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIException;

public class PersistanceNameMappingTestTarget {

    // Constants.
    private static final BigInteger bigIntegerConstant = BigInteger.ZERO;
    private static final Integer integerConstant = Integer.BYTES;

    // The user-defined classes.
    private static Comparable<String> userComparable;
    private static UserInterface userClass;
    private static UserEnum userEnum;

    // The JCL classes.
    private static String concreteType;
    private static Number abstractType;
    private static Serializable interfaceType;
    private static Object object;
    private static Enum someEnum;

    // The userlib classes.
    private static org.aion.avm.userlib.abi.ABIToken token;
    private static org.aion.avm.userlib.AionBuffer buffer;
    private static org.aion.avm.userlib.AionSet set;

    // The api classes.
    private static avm.Address address;

    // The arrays.
    private static int[] primitiveArray1D;
    private static boolean[][][] primitiveArrayMD;
    private static String[] objectArray1D;
    private static Comparable[][] objectArrayMD;
    private static PersistanceNameMappingTestTarget[] userArray;
    private static Object[] objectArray;

    // The exceptions.
    private static IllegalStateException jclException;
    private static ABIException userlibException;
    private static UserException userException;
    private static NullPointerException exceptionSetDuringTryCatch;

    //<-------------------------------------------------------------------------------------------->

    public static byte[] main() {
        String method = new ABIDecoder(Blockchain.getData()).decodeOneString();

        if (method.equals("setFields")) {
            setFields();
        } else if (method.equals("verifyFields")) {
            verifyFields();
        } else {
            throw new IllegalStateException("Unexpected method name: " + method);
        }
        return null;
    }

    //<------------------------------------TESTING METHODS----------------------------------------->

    public static void setFields() {
        setUserDefinedFields();
        setJclFields();
        setUserlibFields();
        setApiFields();
        setArrayFields();
        setExceptionFields();
    }

    public static void verifyFields() {
        verifyUserDefinedFields();
        verifyJclFields();
        verifyArrayFields();
        verifyUserlibFields();
        verifyApiFields();
        verifyExceptionFields();
        verifyConstants();
    }

    //<------------------------------------HELPERS BELOW------------------------------------------->

    private static void setUserDefinedFields() {
        userComparable = new UserComparable();
        userClass = new UserClass(17);
        userEnum = UserEnum.ENUM1;
    }

    private static void setJclFields() {
        concreteType = String.valueOf("concrete");
        abstractType = 5;
        interfaceType = new BigInteger("56");
        object = new Object();
        someEnum = TimeUnit.HOURS;
    }

    private static void setExceptionFields() {
        jclException = new IllegalStateException("illegal");
        userlibException = new ABIException("abi");
        userException = new UserException("user");

        try {
            throw new NullPointerException("exceptional");
        } catch (NullPointerException e) {
            exceptionSetDuringTryCatch = e;
        }
    }

    private static void setUserlibFields() {
        token = new org.aion.avm.userlib.abi.ABIToken();
        buffer = org.aion.avm.userlib.AionBuffer.wrap(new byte[]{ 0x1, 0x2, 0x3 });
        set = new org.aion.avm.userlib.AionSet();
        set.add(new Object());
    }

    private static void setApiFields() {
        address = new avm.Address(new byte[avm.Address.LENGTH]);
    }

    private static void setArrayFields() {
        primitiveArray1D = new int[]{ 1, 2, 3, 4 };
        primitiveArrayMD = new boolean[][][]{ new boolean[][]{ new boolean[]{ true, true }, new boolean[]{ false, false} } };
        objectArray1D = new String[]{ "hi", "hello" };
        objectArrayMD = new Comparable[][]{ new Integer[]{ 1 }, new Character[]{ 'a' } };
        userArray = new PersistanceNameMappingTestTarget[]{ new PersistanceNameMappingTestTarget() };
        objectArray = new Object[]{ new Object(), new Object(), new Object() };
    }

    private static void verifyUserDefinedFields() {
        Blockchain.require(userComparable != null);
        Blockchain.require(userComparable.compareTo("blank") == 1);

        Blockchain.require(userClass != null);
        Blockchain.require(userClass.getNumber() == 17);

        Blockchain.require(userEnum != null);
        Blockchain.require(userEnum == UserEnum.ENUM1);
    }

    private static void verifyJclFields() {
        Blockchain.require(concreteType != null);
        Blockchain.require(concreteType.equals("concrete"));

        Blockchain.require(abstractType != null);
        Blockchain.require(abstractType instanceof Integer);
        Blockchain.require(abstractType.equals(5));

        Blockchain.require(interfaceType != null);
        Blockchain.require(interfaceType.equals(BigInteger.valueOf(56)));

        Blockchain.require(object != null);

        Blockchain.require(someEnum != null);
        Blockchain.require(someEnum == TimeUnit.HOURS);
    }

    private static void verifyExceptionFields() {
        Blockchain.require(jclException != null);
        Blockchain.require(jclException.getMessage().equals("illegal"));

        Blockchain.require(userlibException != null);
        Blockchain.require(userlibException.getMessage().equals("abi"));

        Blockchain.require(userException != null);
        Blockchain.require(userException.getMessage().equals("user"));

        Blockchain.require(exceptionSetDuringTryCatch != null);
        Blockchain.require(exceptionSetDuringTryCatch.getMessage().equals("exceptional"));
    }

    private static void verifyUserlibFields() {
        Blockchain.require(token != null);

        Blockchain.require(buffer != null);
        Blockchain.require(buffer.getArray().length == 3);

        Blockchain.require(set != null);
        Blockchain.require(!set.isEmpty());
    }

    private static void verifyApiFields() {
        Blockchain.require(address != null);
        Blockchain.require(address.unwrap().length == avm.Address.LENGTH);
    }

    private static void verifyArrayFields() {
        Blockchain.require(primitiveArray1D != null);
        Blockchain.require(primitiveArray1D.length == 4);
        Blockchain.require(primitiveArray1D[0] == 1 && primitiveArray1D[3] == 4);

        Blockchain.require(primitiveArrayMD != null);
        Blockchain.require(primitiveArrayMD.length == 1);
        Blockchain.require(primitiveArrayMD[0].length == 2);
        Blockchain.require(primitiveArrayMD[0][0].length == 2);
        Blockchain.require(primitiveArrayMD[0][1].length == 2);
        Blockchain.require(primitiveArrayMD[0][0][0] && primitiveArrayMD[0][0][1]);
        Blockchain.require(!primitiveArrayMD[0][1][0] && !primitiveArrayMD[0][1][1]);

        Blockchain.require(objectArray1D != null);
        Blockchain.require(objectArray1D.length == 2);
        Blockchain.require(objectArray1D[0].equals("hi"));
        Blockchain.require(objectArray1D[1].equals("hello"));

        Blockchain.require(objectArrayMD != null);
        Blockchain.require(objectArrayMD.length == 2);
        Blockchain.require(objectArrayMD[0].length == 1);
        Blockchain.require(objectArrayMD[1].length == 1);
        Blockchain.require(objectArrayMD[0][0] instanceof Integer);
        Blockchain.require(objectArrayMD[1][0] instanceof Character);
        Blockchain.require(objectArrayMD[0][0].equals(Integer.valueOf(1)));
        Blockchain.require(((Character) objectArrayMD[1][0]).charValue() == 'a');

        Blockchain.require(userArray != null);
        Blockchain.require(userArray.length == 1);
        Blockchain.require(userArray[0].hashCode() > 0);

        Blockchain.require(objectArray != null);
        Blockchain.require(objectArray.length == 3);
    }

    private static void verifyConstants() {
        Blockchain.require(bigIntegerConstant == BigInteger.ZERO);
        Blockchain.require(integerConstant == Integer.BYTES);
    }

    //<--------------------------------------USER-DEFINED CLASSES---------------------------------->

    private static class UserException extends Throwable {
        private String message;

        public UserException(String message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            return this.message;
        }
    }

    private static interface UserInterface {
        public int getNumber();
    }

    private static abstract class UserAbstractClass implements UserInterface {
        protected int i;

        public UserAbstractClass(int i) {
            this.i = i;
        }
    }

    private static class UserClass extends UserAbstractClass {
        public UserClass(int n) {
            super(n);
        }

        @Override
        public int getNumber() {
            return this.i;
        }
    }

    private static class UserComparable implements Comparable<String> {
        @Override
        public int compareTo(String o) {
            return 1;
        }
    }

    private static enum UserEnum {
        ENUM1,
        ENUM2;
    }
}
