package org.aion.avm.embed.deploy.remover.resources;

public class Main {
    public static byte[] main() {
        call();
        return new byte[0];
    }

    public static void call() {
        String result = ClassA.methodB1();
        assert result.equals("ClassB_methodB1");
        result = ClassA.methodB2();
        assert result.equals("ClassA_methodB2");
    }
}
