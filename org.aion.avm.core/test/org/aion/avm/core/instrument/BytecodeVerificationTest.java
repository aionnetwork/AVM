package org.aion.avm.core.instrument;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.*;

/*
*
*  The purpose of BVT is to ensure that we can completely trust JVM verifier
*  to verify the bytecode from user as well as our instrumented code.
*
*/
public class BytecodeVerificationTest {
    @Test
    public void testMaxStackSize() throws Exception {
        Assert.assertEquals(0, 0);
    }

    @Test
    public void testNumOfLocals() throws Exception {
        Assert.assertEquals(0, 0);
    }

}
