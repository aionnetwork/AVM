package org.aion.avm.testcontracts;

public class MethodInheritanceTest {
    public static void main(String[] args) {
        B b = new B();
        System.out.println(b.a);

        A a = (A) b;
        System.out.println(a.a);
    }
}

class A {
    int a;

    public A() {
        a = 1;
    }
}

class B extends A {
    int a;

    public B() {
        super.a = 2;
    }
}
