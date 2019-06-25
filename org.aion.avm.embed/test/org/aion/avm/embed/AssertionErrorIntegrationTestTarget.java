package org.aion.avm.embed;

import org.aion.avm.tooling.abi.Callable;

/**
 * The test class loaded by AssertionErrorIntegrationTest.
 */
public class AssertionErrorIntegrationTestTarget {

    @Callable
    public static byte[] emptyError() {
        String message = new AssertionError().getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }

    @Callable
    public static byte[] stringError(byte[] utf8) {
        String message = new AssertionError(new String(utf8)).getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }

    @Callable
    public static byte[] throwableError() {
        String message = new AssertionError(new AssertionError()).getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }

    @Callable
    public static byte[] boolError(boolean val) {
        String message = new AssertionError(val).getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }

    @Callable
    public static byte[] charError(char val) {
        String message = new AssertionError(val).getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }

    @Callable
    public static byte[] intError(int val) {
        String message = new AssertionError(val).getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }

    @Callable
    public static byte[] longError(long val) {
        String message = new AssertionError(val).getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }

    @Callable
    public static byte[] floatError(float val) {
        String message = new AssertionError(val).getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }

    @Callable
    public static byte[] doubleError(double val) {
        String message = new AssertionError(val).getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }

    @Callable
    public static byte[] normalError(byte[] utf8) {
        String message = new AssertionError(new String(utf8), new AssertionError()).getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }
}
