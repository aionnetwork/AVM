package com.example.deployAndRunTest;

public class DeployAndRunTest {

    public int foo;

    public static int bar;

    public int add(int a, int b) {
        return a + b;
    }

    public int addArray(int[] a) {
        return a[0] + a[1];
    }

    public byte[] run() {
        return "Hello, world!".getBytes();
    }
}
