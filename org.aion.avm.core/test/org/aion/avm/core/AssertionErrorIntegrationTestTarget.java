package org.aion.avm.core;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;


/**
 * The test class loaded by AssertionErrorIntegrationTest.
 */
public class AssertionErrorIntegrationTestTarget {
    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(AssertionErrorIntegrationTestTarget.class, BlockchainRuntime.getData());
    }

    public static byte[] emptyError() {
        String message = new AssertionError().getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }

    public static byte[] stringError(byte[] utf8) {
        String message = new AssertionError(new String(utf8)).getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }

    public static byte[] throwableError() {
        String message = new AssertionError(new AssertionError()).getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }

    public static byte[] boolError(boolean val) {
        String message = new AssertionError(val).getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }

    public static byte[] charError(char val) {
        String message = new AssertionError(val).getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }

    public static byte[] intError(int val) {
        String message = new AssertionError(val).getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }

    public static byte[] longError(long val) {
        String message = new AssertionError(val).getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }

    public static byte[] floatError(float val) {
        String message = new AssertionError(val).getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }

    public static byte[] doubleError(double val) {
        String message = new AssertionError(val).getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }

    public static byte[] normalError(byte[] utf8) {
        String message = new AssertionError(new String(utf8), new AssertionError()).getMessage();
        return (null != message)
                ? message.getBytes()
                : null;
    }
}
