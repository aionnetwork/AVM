package org.aion.avm.tooling;

import org.aion.avm.userlib.abi.ABIToken;
import org.junit.Assert;
import org.junit.Test;

public class ABIUtilTest {

    @Test
    public void testMethodCallEncoding() {
        byte[] encoded = ABIUtil.encodeMethodArguments("method", 123, (byte)-1, "hello");
        byte[] expected = new byte[] {
            ABIToken.STRING, 0, 6, 0x6d, 0x65, 0x74, 0x68, 0x6f, 0x64,
            ABIToken.INT, 0x00, 0x00, 0x00, 0x7b,
            ABIToken.BYTE, (byte)0xff,
            ABIToken.STRING, 0, 5, 0x68, 0x65, 0x6c, 0x6c, 0x6f,
        };
        Assert.assertArrayEquals(expected, encoded);
    }

    @Test
    public void testDeploymentArgsEncoding() {
        byte[] encoded = ABIUtil.encodeDeploymentArguments(123, (byte)-1, "hello");
        byte[] expected = new byte[] {
            ABIToken.INT, 0x00, 0x00, 0x00, 0x7b,
            ABIToken.BYTE, (byte)0xff,
            ABIToken.STRING, 0, 5, 0x68, 0x65, 0x6c, 0x6c, 0x6f,
        };
        Assert.assertArrayEquals(expected, encoded);
    }
}
