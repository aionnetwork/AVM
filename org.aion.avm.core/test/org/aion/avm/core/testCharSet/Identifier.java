package org.aion.avm.core.testCharSet;

import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;

/**
 * The class is for testing the different string naming or inputting.
 */
public class Identifier {

    private static String sayHelloEN() {
        String output = new String("Hello!".getBytes());
        Blockchain.println(output);
        return output;
    }

    private static String say哈囉() {
        String output = new String("哈囉!".getBytes());

        Blockchain.println(output);
        return output;

    }

    private static String sayHelloExtendChar() {
        char[] charArray = new char[]{'n', 'i', '\\', '3', '6', '1', 'o', '!'};
        Blockchain.println(new String(charArray));
        return String.valueOf(charArray);

    }

    private static String sayHelloExtendChar2() {
        String output = new String("����!".getBytes());
        Blockchain.println(output);
        return output;

    }

    private static String sayHelloÿ() {
        String output = new String("sayHelloÿ!".getBytes());
        Blockchain.println(output);
        return output;

    }

    private static String ÿ() {
        String output = new String("ÿÿÿÿ!".getBytes());
        Blockchain.println(output);
        return output;

    }

    private static String 哈囉() {
        String output = new String("哈囉!".getBytes());
        Blockchain.println(output);
        return output;
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());

        String methodName = new String(decoder.decodeMethodName().getBytes());

        return checkHello(methodName).getBytes();
    }

    private static String checkHello(String methodName) {
        Blockchain.println("MethodName: " + methodName);
        switch (methodName) {
            case "sayHelloTC":
                return say哈囉();
            case "sayHelloEN":
                return sayHelloEN();
            case "sayHelloExtendChar":
                return sayHelloExtendChar();
            case "sayHelloExtendChar2":
                return sayHelloExtendChar2();
            case "sayHelloExtendChar3":
                return sayHelloÿ();
            case "ÿ":
                return ÿ();
            case "哈囉":
                return 哈囉();
            default:
                return "Invalid method name!";
        }
    }
}
