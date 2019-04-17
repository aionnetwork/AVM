package org.aion.avm.tooling.abi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.aion.avm.core.dappreading.JarBuilder;
import org.junit.Test;


public class ExtractAnnotationsTest {
    @Test
    public void testOneClass() {
        List<String> callables = new ArrayList<>();
        try {
            byte[] jar = JarBuilder.buildJarForMainAndClasses(DAppWithMainNoFallbackTarget.class);

            ABICompiler compiler = ABICompiler.compileJarBytes(jar);
            callables = compiler.getCallables();

        } catch (Throwable e) {
            e.printStackTrace();
        }
        assertEquals(2, callables.size());
        assertEquals(
            "public static boolean test1(boolean)",
            callables.get(0));
        assertEquals("public static boolean test2(int, String, long[])", callables.get(1));
    }

    @Test(expected = ABICompilerException.class)
    public void testNonPublicCallable() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(DAppProtectedCallableTarget.class);
        try {
            ABICompiler.compileJarBytes(jar);
        } catch(ABICompilerException e) {
            assertTrue(e.getMessage().contains("test4"));
            throw e;
        }
    }

    @Test(expected = ABICompilerException.class)
        public void testNonStaticCallable() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(DAppNonstaticCallableTarget.class);
        try {
            ABICompiler.compileJarBytes(jar);
        } catch(ABICompilerException e) {
            assertTrue(e.getMessage().contains("test2"));
            throw e;
        }
    }

    @Test
    public void testMultiClasses() {
        List<String> callables = new ArrayList<>();
        try {
            byte[] jar = JarBuilder.buildJarForMainAndClasses(DAppWithMainNoFallbackTarget.class, DAppNoMainWithFallbackTarget.class);

            ABICompiler compiler = ABICompiler.compileJarBytes(jar);
            callables = compiler.getCallables();

        } catch (Throwable e) {
            e.printStackTrace();
        }
        assertEquals(2, callables.size());
        assertTrue(callables.get(0).indexOf("test1") > 0);
        assertTrue(callables.get(1).indexOf("test2") > 0);
    }
}
