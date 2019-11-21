package org.aion.avm.embed.shadowing.testInvoke;

import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.AionList;

import java.util.function.Function;

public class RunnableResource {

    private static int val;
    private static String str;
    private static AionList<String> list = new AionList<>();
    private static Function<int[], Integer> func = i -> StrictMath.multiplyExact(i[0], i[1]);
    private static int runResult;
    private static String superClassResult;

    @Callable
    public static void onPrimitive() {
        Runnable r = () -> {
            int a = 100;
            a *= 100;
            runResult = a;
        };
        r.run();
        Blockchain.require(10_000 == runResult);
    }

    @Callable
    public static void onAionList() {
        list.add("str1");
        list.add("str2");

        Runnable r = () -> {
            str = "new String";
            list.add(str);
        };
        r.run();
        Blockchain.require(list.size() == 3);
    }

    @Callable
    public static void onArray() {
        Runnable r = () -> {
            int[][] a = new int[10][];
            a[0] = new int[100];
            Blockchain.require(a.length == 10);
        };
        r.run();
    }

    @Callable
    public static void onStatic() {
        Runnable r = () -> val = 100;
        r.run();
        Blockchain.require(val == 100);
    }

    @Callable
    public static void onFunction() {
        Runnable r = () -> {
            Function<Character, Boolean> isLowerCase = Character::isLowerCase;
            Blockchain.require(isLowerCase.apply('b'));
        };
        r.run();
    }

    @Callable
    public static void onStaticFunction() {
        Runnable r = () -> {
            int res = func.apply(new int[] {10, 100});
            Blockchain.require(res == 1000);
        };
        r.run();
    }

    @Callable
    public static void onNewInvokeSpecialFunction() {
        Runnable r = () -> {
            Function<String, SuperClass> newObject = SuperClass::new;
            newObject.apply("Tom");
        };
        r.run();
        Blockchain.require("Tom".equals(superClassResult));
    }

    @Callable
    public static void onNewObject() {
        Runnable r = () -> new SuperClass("");
        r.run();
        Blockchain.require("".equals(superClassResult));
    }

    static class SuperClass {
        public SuperClass(String name) { superClassResult = name; }
    }
}

