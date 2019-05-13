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

    public static class SampleClass{}
}
