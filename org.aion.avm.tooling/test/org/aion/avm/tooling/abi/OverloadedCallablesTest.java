package org.aion.avm.tooling.abi;

import static org.junit.Assert.assertTrue;

import org.aion.avm.tooling.deploy.eliminator.TestUtil;
import org.junit.Test;


public class OverloadedCallablesTest {
    // Compilation should fail because @Callable methods cannot be overloaded
    @Test(expected = ABICompilerException.class)
    public void testBadParams() {
        byte[] jar = TestUtil.serializeClassesAsJar(OverloadedCallablesTarget.class);
        try {
            ABICompiler.compileJarBytes(jar);
        } catch(ABICompilerException e) {
            assertTrue(e.getMessage().contains("test1"));
            throw e;
        }
    }
}
