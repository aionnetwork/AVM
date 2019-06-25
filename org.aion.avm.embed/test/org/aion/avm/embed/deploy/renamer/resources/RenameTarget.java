package org.aion.avm.embed.deploy.renamer.resources;

import avm.Blockchain;

import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.avm.userlib.abi.ABIDecoder;

import java.math.BigInteger;
import java.util.Map;
import java.util.function.Function;

//NOTE:  This is a copy of a test in org.aion.avm.tooling in order to support the RenameDeployTest.
public class RenameTarget {
    static Map<Integer, String> map1 = new AionMap<>();
    static AionSet<String> set = new AionSet<>();
    static Runnable b = () -> {
        set.add("new String");
    };

    static {
        testUserlib();
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else if (methodName.equals("testException")) {
            testException();
            return new byte[0];
        } else if (methodName.equals("testInheritance")) {
            testInheritance();
            return new byte[0];
        } else if (methodName.equals("testComparable")) {
            testComparable();
            return new byte[0];
        } else if (methodName.equals("testUserlib")) {
            testUserlib();
            return new byte[0];
        } else if (methodName.equals("testInvokeDynamics")) {
            testInvokeDynamics();
            return new byte[0];
        } else if (methodName.equals("testEnum")) {
            testEnum();
            return new byte[0];
        } else {
            Blockchain.revert();
            return null;
        }
    }

    public static void testException() {
        String result;
        try {
            throw new UserDefinedRuntimeException("");
        } catch (UserDefinedRuntimeException e) {
            result = e.getMessage();
        }
        Blockchain.require(result.equals("UserDefinedRuntimeException thrown"));
    }

    public static void testInheritance() {
        ConcreteChildOne childOne = new ConcreteChildOne();
        childOne.getBoolVal();
        childOne.getCharVal();
        childOne.getLongVal();
        childOne.getIntVal();
        internalCall(childOne);
    }

    public static void testComparable() {
        ComparableSubject subj = new ComparableSubject("One");
        subj.compareTo("Two");
        org.aion.avm.embed.deploy.renamer.resources.ClassB b = new org.aion.avm.embed.deploy.renamer.resources.ClassB();
        b.compareTo("Three");
    }

    public static void testUserlib() {
        map1.put(1, "One");
        map1.remove(1);
        AionList list1 = new AionList();
        list1.add(new ClassB());
        Blockchain.require(!list1.contains(10));
    }

    public static void testInvokeDynamics() {
        b.run();
        Blockchain.require(set.size() == 1);
        Function<BigInteger, Integer> c = BigInteger::hashCode;
        c.apply(BigInteger.ONE);
    }

    public static void testEnum() {
        EnumElements.valueOf("ClassA");
        EnumElements.ClassA.name.equals("A");
        EnumElements.ClassA.equals(EnumElements.ClassB);
        EnumElements.values();
        EnumElements.ClassB.name();
    }

    private static void internalCall(Object o) {
        Blockchain.require(((ConcreteChildOne) o).getBoolVal());
    }


    public interface ParentInterfaceOne {
        boolean b = true;

        int getIntVal();
    }

    public interface ChildInterfaceOne extends ParentInterfaceOne {
        long getLongVal();

        int getIntVal(int a);
    }

    public interface ParentInterfaceTwo {
        char getCharVal();
    }

    public static class ConcreteChildOne implements ChildInterfaceOne, ParentInterfaceTwo {
        boolean b2 = this.b;

        @Override
        public int getIntVal() {
            return 0;
        }

        @Override
        public long getLongVal() {
            return 0;
        }

        @Override
        public int getIntVal(int i) {
            return i;
        }

        @Override
        public char getCharVal() {
            return 'a';
        }

        public boolean getBoolVal() {
            return b2;
        }
    }

    public static class UserDefinedRuntimeException extends RuntimeException {

        public UserDefinedRuntimeException(String message) {
            super(message);
        }

        @Override
        public String getMessage() {
            return "UserDefinedRuntimeException thrown";
        }
    }

    private static class ComparableSubject implements Comparable<String> {
        String str;

        public ComparableSubject(String str) {
            this.str = str;
        }

        @Override
        public int compareTo(String o) {
            return o.compareTo(str);
        }
    }

    public static class ClassB {
        int f;
        private void privateMethod(){}
        public class ClassC {
            int f;
            public void innerMethod() {
                privateMethod();
            }
            public class ClassD {
                public class ClassE {

                }
            }
        }
    }
}
