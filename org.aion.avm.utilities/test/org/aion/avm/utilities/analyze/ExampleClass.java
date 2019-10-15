package org.aion.avm.utilities.analyze;

import java.util.function.Function;

public class ExampleClass {
    private int a = 789416;
    private static long b = 456L;
    private static String s = "String";
    private Double d2 = 123d;
    private Float f = 1.0004f;

    private static Function<Integer, Integer> doubleValue = (i -> i * 2);

    public static void main(String[] args) {
        System.out.println("Hello world!");
    }

    public static void apply(int i) {
        doubleValue.apply(i);
    }
}
