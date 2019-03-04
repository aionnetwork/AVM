package org.aion.avm.tooling.abi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import org.aion.avm.core.dappreading.JarBuilder;
import org.junit.Before;
import org.junit.Test;


public class ExtractAnnotationsTest {

    private static ABICompiler compiler;

    @Before
    public void setup() {
        compiler = new ABICompiler();
    }

    @Test
    public void testOneClass() {
        List<String> callables = new ArrayList<>();
        try {
            byte[] jar = JarBuilder.buildJarForMainAndClasses(DAppWithMainNoFallbackTarget.class);

            compiler.compile(new ByteArrayInputStream(jar));
            callables = compiler.getCallables();

        } catch (Throwable e) {
            e.printStackTrace();
        }
        assertEquals(2, callables.size());
        assertEquals(
            "org/aion/avm/tooling/abi/DAppWithMainNoFallbackTarget: public static boolean test1(boolean)",
            callables.get(0));
        assertEquals(callables.get(1), "org/aion/avm/tooling/abi/DAppWithMainNoFallbackTarget: public static boolean test2(int, java.lang.String, long[])");
    }

    @Test(expected = AnnotationException.class)
    public void testNonPublicCallable() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(DAppProtectedCallableTarget.class);
        try {
            compiler.compile(new ByteArrayInputStream(jar));
        } catch(AnnotationException e) {
            assertTrue(e.getMessage().contains("test4"));
            throw e;
        }
    }

    @Test(expected = AnnotationException.class)
        public void testNonStaticCallable() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(DAppNonstaticCallableTarget.class);
        try {
            compiler.compile(new ByteArrayInputStream(jar));
        } catch(AnnotationException e) {
            assertTrue(e.getMessage().contains("test2"));
            throw e;
        }
    }

    @Test
    public void testMultiClasses() {
        List<String> callables = new ArrayList<>();
        try {
            byte[] jar = JarBuilder.buildJarForMainAndClasses(DAppWithMainNoFallbackTarget.class, DAppNoMainWithFallbackTarget.class);

            compiler.compile(new ByteArrayInputStream(jar));
            callables = compiler.getCallables();

        } catch (Throwable e) {
            e.printStackTrace();
        }
        assertEquals(2, callables.size());
        assertTrue(callables.get(0).indexOf("test1") > 0);
        assertTrue(callables.get(1).indexOf("test2") > 0);
    }
}
