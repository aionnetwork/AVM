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
}
