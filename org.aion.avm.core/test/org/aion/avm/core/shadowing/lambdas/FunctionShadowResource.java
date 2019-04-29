package org.aion.avm.core.shadowing.lambdas;

import java.util.function.Function;

import avm.Blockchain;


public class FunctionShadowResource {
    private static Runnable RUNNABLE;
    private static Function<String, String> FUNCTION;
    private static int RESULT = 0;


    public static byte[] main() {
        switch (Blockchain.getData()[0]) {
            case 0:
                Runnable runnable = createRunnable();
                checkRunnable(runnable);
                runRunnable(runnable);
                break;
            case 1:
                Function<String, String> function = createFunction();
                checkFunction(function);
                runFunction(function);
                break;
            case 2:
                RUNNABLE = createRunnable();
                checkRunnable(RUNNABLE);
                break;
            case 3:
                checkRunnable(RUNNABLE);
                runRunnable(RUNNABLE);
                break;
            case 4:
                FUNCTION = createFunction();
                checkFunction(FUNCTION);
                break;
            case 5:
                checkFunction(FUNCTION);
                runFunction(FUNCTION);
                break;
            case 6:
                Function<String, String> reference = FunctionShadowResource::referable;
                checkFunction(reference);
                runFunction(reference);
                break;
            case 7:
                // Verify that multiple attempts to create the Runnable and Function each return a unique instance.
                Runnable runnable1 = createRunnable();
                Runnable runnable2 = createRunnable();
                Blockchain.require(runnable1 != runnable2);
                Function<String, String> function1 = createFunction();
                Function<String, String> function2 = createFunction();
                Blockchain.require(function1 != function2);
                break;
            case 8:
                testExceptions();
                break;
            default:
                // Unknown.
                Blockchain.revert();
        }
        return new byte[0];
    }

    private static String referable(String input) {
        return "PREFIX: " + input;
    }

    private static Runnable createRunnable() {
        return () -> {
            RESULT = 1;
        };
    }

    private static void checkRunnable(Runnable runnable) {
        // We expect the hashcode of lambdas to be 0.
        int hashcode = runnable.hashCode();
        Blockchain.require(0 == hashcode);
    }

    private static void runRunnable(Runnable runnable) {
        // Run the runnable and observe the side-effect.
        Blockchain.require(0 == RESULT);
        runnable.run();
        Blockchain.require(1 == RESULT);
    }

    private static Function<String, String> createFunction() {
        return string -> "PREFIX: " + string;
    }

    private static void checkFunction(Function<String, String> function) {
        // We expect the hashcode of lambdas to be 0.
        int hashcode = function.hashCode();
        Blockchain.require(0 == hashcode);
    }

    private static void runFunction(Function<String, String> function) {
        // Make sure the function is correctly executed.
        String output = function.apply("INPUT");
        Blockchain.require("PREFIX: INPUT".equals(output));
    }

    private static void testExceptions() {
        // Test the Runnable.
        Runnable runnable = () -> ((Object)null).hashCode();
        boolean found = false;
        try {
            runnable.run();
        } catch (NullPointerException e) {
            found = true;
        }
        Blockchain.require(found);
        
        // Test the Function.
        Function<Void, String> function = (v) -> v.toString();
        found = false;
        try {
            function.apply(null);
        } catch (NullPointerException e) {
            found = true;
        }
        Blockchain.require(found);
    }
}

