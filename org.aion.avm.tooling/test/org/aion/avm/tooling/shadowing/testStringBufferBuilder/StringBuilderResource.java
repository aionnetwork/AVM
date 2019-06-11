package org.aion.avm.tooling.shadowing.testStringBufferBuilder;

import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;

public class StringBuilderResource {

    @Callable
    public static void stringBuilderInsertNull() {
        StringBuilder s = new StringBuilder();

        s.insert(0, (Object) null);
        Blockchain.require(s.toString().equals("null"));

        s.insert(4, (String) null);
        Blockchain.require(s.toString().equals("nullnull"));

        s.insert(8, (CharSequence) null);
        Blockchain.require(s.toString().equals("nullnullnull"));

        s.insert(12, (CharSequence) null, 0, 1);
        Blockchain.require(s.toString().equals("nullnullnulln"));

        boolean thrown = false;
        try {
            s.insert(0, (char[]) null);
        } catch (NullPointerException e) {
            thrown = true;
        }
        Blockchain.require(thrown);
    }

    @Callable
    public static void stringBuilderAppendNull() {
        StringBuilder s = new StringBuilder();

        s.append((Object) null);
        Blockchain.require(s.toString().equals("null"));

        s.append((String) null);
        Blockchain.require(s.toString().equals("nullnull"));

        s.append((StringBuffer) null);
        Blockchain.require(s.toString().equals("nullnullnull"));

        s.append((CharSequence) null);
        Blockchain.require(s.toString().equals("nullnullnullnull"));

        boolean thrown = false;
        try {
            s.append((char[]) null);
        } catch (NullPointerException e) {
            thrown = true;
        }
        Blockchain.require(thrown);
    }

    @Callable
    public static void stringBuilderAddObject() {
        StringBuilder sb = new StringBuilder();
        SampleClass sampleClass = new SampleClass();
        sb.append(sampleClass);
        Blockchain.require(sb.toString().equals(sampleClass.toString()));

        sb.insert(sampleClass.toString().length(), sampleClass);
        Blockchain.require(sb.toString().equals(sampleClass.toString() + sampleClass.toString()));

    }

    @Callable
    public static void stringBuilderConstructor() {
        StringBuilder sb = new StringBuilder(100);
        Blockchain.require(sb.length() == 0);

        StringBuilder sb2 = new StringBuilder("MyString");
        Blockchain.require(sb2.toString().equals("MyString"));

        StringBuilder sb3 = new StringBuilder((CharSequence) "MyString");
        Blockchain.require(sb3.toString().equals("MyString"));
    }

    @Callable
    public static void stringBuilderInvalidConstructor() {
        long energyRemaining = 0l;
        boolean exceptionThrown = false;
        try {
            energyRemaining = Blockchain.getRemainingEnergy();
            StringBuilder sb = new StringBuilder(-10000000);
        } catch (NegativeArraySizeException e) {
            exceptionThrown = true;
            Blockchain.require(energyRemaining > Blockchain.getRemainingEnergy());
        }
        Blockchain.require(exceptionThrown);
    }

    @Callable
    public static void stringBuilderInvalidAppend(){
        long energyRemaining = 0l;
        boolean exceptionThrown = false;
        try {
            StringBuilder sb = new StringBuilder();
            char[] arr = new char[]{'a', 'b', 'c'};
            energyRemaining = Blockchain.getRemainingEnergy();
            sb.append(arr, 1, -10000000);
        } catch (IndexOutOfBoundsException e) {
            exceptionThrown = true;
            Blockchain.require(energyRemaining > Blockchain.getRemainingEnergy());
        }
        Blockchain.require(exceptionThrown);
    }

    @Callable
    public static void stringBuilderInvalidInsert(){
        long energyRemaining = 0l;
        boolean exceptionThrown = false;
        try {
            StringBuilder sb = new StringBuilder();
            char[] arr = new char[]{'a', 'b', 'c'};
            energyRemaining = Blockchain.getRemainingEnergy();
            sb.insert(0, arr, 0, -10000000);
        } catch (IndexOutOfBoundsException e) {
            exceptionThrown = true;
            Blockchain.require(energyRemaining > Blockchain.getRemainingEnergy());
        }
        Blockchain.require(exceptionThrown);
    }

    @Callable
    public static void stringBuilderInsert(){
        StringBuilder sb = new StringBuilder()
                .insert(0, new char[]{'a', 'b', 'c'}, 0, 1)
                .insert(1, 1)
                .insert(2, 10l)
                .insert(3, 1.4d)
                .insert(4, 1.2f)
                .insert(5, "Hello")
                .insert(6, new SampleClass());
        Blockchain.require(sb.toString().length() == 106);
    }

    @Callable
    public static void stringBuilderAppend(){
        StringBuilder sb = new StringBuilder()
                .append("Hello ")
                .append("World!")
                .append(123)
                .append(true)
                .append(new char[]{'a', 'b', 'c'}, 0, 1)
                .append(new SampleClass());
        Blockchain.require(sb.toString().length() == 111);
    }

    public static class SampleClass{}
}
