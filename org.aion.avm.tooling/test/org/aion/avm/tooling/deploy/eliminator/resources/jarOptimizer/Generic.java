package org.aion.avm.tooling.deploy.eliminator.resources.jarOptimizer;

public class Generic<T> {

    public void outerMethod() {
        StaticInnerClassGeneric.innerMethod();
        InnerClassGeneric generic = new InnerClassGeneric();
        generic.innerMethod();
    }

    public static class StaticInnerClassGeneric<T> {
        public static void innerMethod() {

        }
    }

    public class InnerClassGeneric<T> {
        public void innerMethod() {

        }
    }
}
