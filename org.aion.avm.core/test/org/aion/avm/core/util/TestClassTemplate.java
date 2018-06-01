package org.aion.avm.core.util;


/**
 * This class exists as the basis for various tests we want to create which depend on a fixed binary representation, not something we can just compile.
 * This might be because of specific code shapes which are uncommon, the use of deprecated bytecodes, unsupported versions, post-compile corruption, etc.
 * 
 * Typically, we create these by using ASMifier to generate the ASM code for a test.
 * Example:  java -cp "lib/*" org.objectweb.asm.util.ASMifier java.lang.Runnable
 * Then, modify that code and run it to a ClassWriter, serializing the resulting bytes as the class file.
 * 
 * We typically store these fixed binary files in "test/resources".
 * 
 * Changes to this class should not be committed since they represent the starting-state we used as the basis for these fixed binary test files.
 */
public class TestClassTemplate {
    public static void main(String[] args) {
        // We don't want to include any complex types here so we will just create a RuntimeException for each part.
        String result = "START";
        try {
            result = "TRY";
        } catch (Throwable t) {
            result = "CATCH";
        } finally {
            result = "FINALLY";
        }
        result = "DONE";
        throw new RuntimeException(result);
    }
}
