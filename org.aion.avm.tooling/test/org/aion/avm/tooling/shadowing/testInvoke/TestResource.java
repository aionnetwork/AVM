package org.aion.avm.tooling.shadowing.testInvoke;


import avm.Address;
import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;

import java.math.BigInteger;
import java.util.function.Function;

public class TestResource {

    private static int val = 0;
    private static Function<BigInteger, Integer> getHash = BigInteger::hashCode;
    private static Function<String, SuperClass> newPerson = SuperClass::new;
    private static Function<Integer, String> f = (s) -> {
        val = s;
        return "Updated to " + val;
    };

    @Callable
    public static void applyLambda() {
        String result = f.apply(100);
        Blockchain.require(result.equals("Updated to 100"));
    }

    @Callable
    public static void applyMethodReference() {
        int result = getHash.apply(BigInteger.valueOf(120));
        Blockchain.require(result == 120);
    }

    @Callable
    public static void applyMethodReferenceNewInstance() {
        SuperClass result = newPerson.apply("Tom");
        Blockchain.require(result.name.equals("Tom"));
    }

    @Callable
    public static void applyLambdaVirtual() {
        Function<BigInteger, Integer> f2 = s -> s.intValueExact();
        Blockchain.require(f2.apply(BigInteger.valueOf(1500)) == 1500);
    }

    @Callable
    public static void applyMethodReferenceVirtual() {
        Function<BigInteger, Integer> f2 = BigInteger::intValueExact;
        Blockchain.require(f2.apply(BigInteger.valueOf(1500)) == 1500);
    }

    @Callable
    public static void applyMethodReferenceStatic() {
        Function<String, Integer> f2 = Integer::parseInt;
        Blockchain.require(f2.apply("1500") == 1500);
    }

    @Callable
    public static void applyLambdaStatic() {
        Function<String, Integer> f2 = i -> Integer.parseInt(i);
        Blockchain.require(f2.apply("1500") == 1500);
    }

    @Callable
    public static void applyMethodReferenceNewSpecial() {
        Function<String, SuperClass> f2 = SuperClass::new;
        Blockchain.require(f2.apply("Tom").name.equals("Tom"));
    }

    @Callable
    public static void applyLambdaSpecial() {
        Function<String, SuperClass> f2 = (s) -> new SuperClass(s);
        Blockchain.require(f2.apply("Tom").name.equals("Tom"));
    }

    @Callable
    public static void applyMethodReferenceStaticForInstance() {
        Function<String, SuperClass> f2 = (s) -> new SuperClass(s);
        SuperClass p = f2.apply("Tom");
        Function<SuperClass, String> f3 = SuperClass::getName;
        Function<SuperClass, Integer> f4 = SuperClass::getConstant;
        Blockchain.require(f3.apply(p).equals("Tom"));
        Blockchain.require(f4.apply(p) == 1);
    }

    @Callable
    public static void applyMethodReferenceInterface() {
        Function<ChildInterfaceOne, Integer> f2 = ChildInterfaceOne::getValue;
        Blockchain.require(f2.apply(new ConcreteClass()) == 0);
    }

    @Callable
    public static void applyMethodReferenceVirtual2() {
        Function<String, Integer> f2 = String::length;
        Blockchain.require(f2.apply("MyString") == 8);
    }

    @Callable
    public static void applyMethodReferenceInterfacePrimitiveArg() {
        Function<Integer, Integer> f2 = ChildInterfaceOne::getNewValue;
        Blockchain.require(f2.apply(1420) == 1420);
    }

    @Callable
    public static void applyMethodReferenceInnerClass() {
        Function<Integer, SubClass.InnerClass> f2 = SubClass.InnerClass::new;
        Blockchain.require(f2.apply(10).hashCode() > 0);
    }

    @Callable
    public static void applyLambdaArrayInput() {
        Function<int[], Integer> f2 = i -> StrictMath.subtractExact(i[0], i[1]);
        int[] input = {10, 1};
        Blockchain.require(f2.apply(input) == 9);
    }

    @Callable
    public static void applyException() {
        Function<Exception, String> f2 = (s) -> s.getMessage();
        Blockchain.require(f2.apply(new NullPointerException("myException")).equals("myException"));
    }

    @Callable
    public static void applyMethodReferenceException() {
        Function<Exception, String> f2 = Throwable::getMessage;
        Blockchain.require(f2.apply(new NullPointerException("myException")).equals("myException"));
    }

    @Callable
    public static void applyLambdaPrimitiveInput() {
        Function<Integer, ConcreteClass> f2 = e -> new ConcreteClass();
        f2.apply(10);
        Function<Character, Boolean> f3 = c -> ConcreteClass.isLowerCase.apply(c);
        Blockchain.require(f3.apply('c'));
    }

    @Callable
    public static void apply1DArray() {
        Integer[] arr = new Integer[100];
        Function<Integer[], Integer> f2 = (s) -> s.length;
        Blockchain.require(f2.apply(arr) == 100);
    }

    @Callable
    public static void apply2DArray() {
        Function<Integer[][], Integer> f2 = (s) -> s[0][0];
        Integer[][] arr = new Integer[100][];
        arr[0] = new Integer[10];
        arr[0][0] = 150;
        Blockchain.require(f2.apply(arr) == 150);
    }

    @Callable
    public static void applyObjectArray() {
        Function<SuperClass[][], String> f2 = (s) -> s[0][0].name;
        SuperClass[][] arr = new SuperClass[100][];
        arr[0] = new SuperClass[10];
        arr[0][0] = new SuperClass("Tom");
        Blockchain.require(f2.apply(arr).equals("Tom"));
    }

    @Callable
    public static void applyInterfaceArray() {
        Function<ChildInterfaceOne[][], String> f2 = (s) -> s[0][0].getClass().getName();
        ChildInterfaceOne[][] arr = new ChildInterfaceOne[100][];
        arr[0] = new ChildInterfaceOne[10];
        arr[0][0] = new ConcreteClass();
        Blockchain.require(f2.apply(arr).equals(ConcreteClass.class.getName()));
    }

    @Callable
    public static void applyReturnArray() {
        Function<ChildInterfaceOne[][], ChildInterfaceOne[][]> f2 = (s) -> {
            s[0][0] = new ConcreteClass();
            return s;
        };
        ConcreteClass[][] arr = new ConcreteClass[100][];
        arr[0] = new ConcreteClass[10];
        Blockchain.require(f2.apply(arr)[0][0] != null);
    }

    @Callable
    public static void applyOnFunction() {
        Function<Function<SuperClass[][], String>, String> f2 = (s) -> {
            SuperClass[][] arr = new SuperClass[100][10];
            arr[0][0] = new SuperClass("Tom");
            return s.apply(arr);
        };
        Function<SuperClass[][], String> f3 = (s) -> s[0][0].name;
        Blockchain.require(f2.apply(f3).equals("Tom"));
    }

    @Callable
    public static void applyOnFunction2() {
        Function<Function<SuperClass, String>, String> f2 = (s) -> {
            SuperClass p = new SuperClass("Tom");
            return s.apply(p);
        };

        Function<SuperClass, String> f3 = (s) -> s.name;
        Blockchain.require(f2.apply(f3).equals("Tom"));
    }

    @Callable
    public static void applyBlockchainGetBalance() {
        Function<Address, BigInteger> f2 = Blockchain::getBalance;
        Blockchain.require(f2.apply(Blockchain.getCaller()).equals(Blockchain.getBalance(Blockchain.getCaller())));
    }

    @Callable
    public static void applyLambdaTryCatch() {
        Function<String, byte[]> f2 = String::getBytes;
        boolean thrown = false;
        try {
            f2.apply(null);
        } catch (NullPointerException e){
            thrown = true;
        }
        Blockchain.require(thrown);
    }

    public interface CommonInterface { }

    public interface ChildInterfaceOne extends CommonInterface {
        int getValue();
        static int getNewValue(int a){ return a;}
    }

    static class ConcreteClass implements ChildInterfaceOne {
        public static Function<Character, Boolean> isLowerCase = Character::isLowerCase;

        @Override
        public int getValue() {
            return 0;
        }
    }

    static class SuperClass {
        private final String name;

        public SuperClass(String name) { this.name = name; }
        public String getName() {
            return name;
        }
        public int getConstant() { return 1; }
    }

    static class SubClass extends SuperClass {
        public SubClass(String name) {
            super(name);
        }

        static class InnerClass {
            public InnerClass(int val) { }
        }
    }
}
