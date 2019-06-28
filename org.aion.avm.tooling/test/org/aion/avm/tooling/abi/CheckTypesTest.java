package org.aion.avm.tooling.abi;

import static org.junit.Assert.assertTrue;

import org.aion.avm.tooling.deploy.eliminator.TestUtil;
import org.junit.Test;


public class CheckTypesTest {
    // Compilation should fail because of boxed-type parameters
    @Test(expected = ABICompilerException.class)
    public void testBadParams() {
        byte[] jar = TestUtil.serializeClassesAsJar(DAppForbiddenParameterTarget.class);
        try {
            ABICompiler.compileJarBytes(jar);
        } catch(ABICompilerException e) {
            System.out.println(e.getMessage());
            assertTrue(e.getMessage().contains("badParams"));
            throw e;
        }
    }

    // Compilation should fail because of boxed-type return
    @Test(expected = ABICompilerException.class)
    public void testBadReturnType() {
        byte[] jar = TestUtil.serializeClassesAsJar(DAppForbiddenReturnTypeTarget.class);
        try {
            ABICompiler.compileJarBytes(jar);
        } catch(ABICompilerException e) {
            assertTrue(e.getMessage().contains("badReturn"));
            throw e;
        }
    }

    // Compilation should fail because of a 3-dimensional integer array parameter
    @Test(expected = ABICompilerException.class)
    public void testBadIntArray() {
        byte[] jar = TestUtil.serializeClassesAsJar(DAppForbiddenIntArrayTarget.class);
        try {
            ABICompiler.compileJarBytes(jar);
        } catch(ABICompilerException e) {
            assertTrue(e.getMessage().contains("badIntArray"));
            throw e;
        }
    }

    // Compilation should fail because of a 2-dimensional String array parameter
    @Test(expected = ABICompilerException.class)
    public void testBadStringArray() {
        byte[] jar = TestUtil.serializeClassesAsJar(DAppForbiddenStringArrayTarget.class);
        try {
            ABICompiler.compileJarBytes(jar);
        } catch(ABICompilerException e) {
            assertTrue(e.getMessage().contains("badStringArray"));
            throw e;
        }
    }
}
