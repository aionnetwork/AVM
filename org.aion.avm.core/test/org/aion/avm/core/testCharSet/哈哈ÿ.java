package org.aion.avm.core.testCharSet;

import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;

/**
 * The class is for testing the different class naming.
 */
public class 哈哈ÿ {

    public static class Inner哈囉 {

        Inner哈囉() {
            Blockchain.println(new String("哈囉!".getBytes()));
        }

        String get哈囉() {
            return new String("哈囉!".getBytes());
        }
    }

    public static class ÿ {

        ÿ() {
            Blockchain.println(new String("ÿ!".getBytes()));
        }

        String getÿ() {
            return new String("ÿ!".getBytes());
        }
    }


    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());

        String methodName = new String(decoder.decodeMethodName().getBytes());

        return checkHello(methodName).getBytes();
    }

    private static String checkHello(String methodName) {
        Blockchain.println("MethodName: " + methodName);
        switch (methodName) {
            case "callInnerClass1":
                return new Inner哈囉().get哈囉();
            case "callInnerClass2":
                return new ÿ().getÿ();
            default:
                return "Invalid method name!";
        }
    }
}
