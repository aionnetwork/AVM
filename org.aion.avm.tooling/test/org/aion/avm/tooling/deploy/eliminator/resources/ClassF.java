package org.aion.avm.tooling.deploy.eliminator.resources;

import java.util.function.Function;

public class ClassF extends ClassE {

    public Function<Integer, String> func;

    public char classF() {
        return 'f';
    }

    public Function<Integer, Integer> getIncrementorLambda() {
        String str = "aaaaaaaaaaaaaa";
        func = (x) -> str.substring(0, x);
        Function<String, Integer> func2 = (inputStr) -> onlyCalledByLambda(inputStr);
        return func2.compose(func);
    }

    private int onlyCalledByLambda(String s) {
        return  s.length();
    }

    public static int classFStaticMethod() {
        return 5;
    }

    @Override
    public char classE() {
        return 'e' + 'f';
    }

    @Override
    public char classD() {
        ClassG g = new ClassG();
        // this invokes a static method through an instance
        g.invokeFlambda();
        return 'd' + 'f';
    }

    @Override
    public char interfaceA() {
        return 'a' + 'f';
    }

    @Override
    public char interfaceB() {
        return 'b' + 'f';
    }
}
