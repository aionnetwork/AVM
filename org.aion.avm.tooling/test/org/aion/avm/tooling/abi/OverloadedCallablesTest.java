package org.aion.avm.tooling.abi;

import static org.junit.Assert.assertTrue;

import org.aion.avm.core.dappreading.JarBuilder;
import org.junit.Test;


public class OverloadedCallablesTest {
    // Compilation should fail because @Callable methods cannot be overloaded
    @Test(expected = ABICompilerException.class)
    public void testBadParams() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(OverloadedCallablesTarget.class);
        try {
            ABICompiler.compileJarBytes(jar);
        } catch(ABICompilerException e) {
            System.out.println(e.getMessage());
            assertTrue(e.getMessage().contains("test1"));
            throw e;
        }
    }
}
