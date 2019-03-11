package org.aion.avm.tooling.abi;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import org.aion.avm.core.dappreading.JarBuilder;
import org.junit.Test;


public class OverloadedCallablesTest {

    private static ABICompiler compiler = new ABICompiler();

    // Compilation should fail because @Callable methods cannot be overloaded
    @Test(expected = ABICompilerException.class)
    public void testBadParams() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(OverloadedCallablesTarget.class);
        try {
            compiler.compile(new ByteArrayInputStream(jar));
        } catch(ABICompilerException e) {
            System.out.println(e.getMessage());
            assertTrue(e.getMessage().contains("test1"));
            throw e;
        }
    }
}
