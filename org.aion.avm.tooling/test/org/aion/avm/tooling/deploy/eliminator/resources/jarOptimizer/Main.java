package org.aion.avm.tooling.deploy.eliminator.resources.jarOptimizer;

import java.util.ArrayList;

public class Main {
    public static void main() {
        callInnerClassMethod();
        accessInnerClassField();
        onlyKeepOuterClass();
        genericClass();
    }

    private static void callInnerClassMethod() {
        InnerMethodAccess.InnerClass innerMethodAccess = new InnerMethodAccess.InnerClass();
        innerMethodAccess.method();
    }

    private static void accessInnerClassField() {
        InnerFieldAccess innerFieldAccess = new InnerFieldAccess();
        innerFieldAccess.getCount();
    }

    private static void onlyKeepOuterClass() {
        InnerClassUnreachable innerClassUnreachable = new InnerClassUnreachable();
        innerClassUnreachable.outerMethod(new ArrayList<>());
    }

    private static void genericClass() {
        Generic<Integer> generic = new Generic();
        generic.outerMethod();
    }
}
