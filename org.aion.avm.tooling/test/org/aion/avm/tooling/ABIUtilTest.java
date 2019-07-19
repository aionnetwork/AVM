package org.aion.avm.tooling;

import org.aion.avm.userlib.abi.ABIToken;
import org.junit.Assert;
import org.junit.Test;
import java.math.BigInteger;

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

        BigInteger[] bigIntegers = new BigInteger[3];
        bigIntegers[0] = new BigInteger(0, new byte[]{0});
        bigIntegers[1] = new BigInteger(1, new byte[]{127, 126, 5});
        bigIntegers[2] = new BigInteger(-1, new byte[]{10, 11});
        encoded = ABIUtil.encodeMethodArguments("method", BigInteger.TEN, "hello", bigIntegers);
        expected = new byte[] {
                ABIToken.STRING, 0, 6, 0x6d, 0x65, 0x74, 0x68, 0x6f, 0x64,
                ABIToken.BIGINT, 1, 10,
                ABIToken.STRING, 0, 5, 0x68, 0x65, 0x6c, 0x6c, 0x6f,
                ABIToken.ARRAY, ABIToken.BIGINT, 0, 3,
                ABIToken.BIGINT, 1, 0, ABIToken.BIGINT, 3, 127, 126, 5, ABIToken.BIGINT, 2, -11, -11
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

        BigInteger[] bigIntegers = new BigInteger[3];
        bigIntegers[0] = new BigInteger(0, new byte[]{0});
        bigIntegers[1] = new BigInteger(1, new byte[]{127, 126, 5});
        bigIntegers[2] = new BigInteger(-1, new byte[]{10, 11});
        encoded = ABIUtil.encodeDeploymentArguments(BigInteger.TEN, "hello", bigIntegers);
        expected = new byte[] {
                ABIToken.BIGINT, 1, 10,
                ABIToken.STRING, 0, 5, 0x68, 0x65, 0x6c, 0x6c, 0x6f,
                ABIToken.ARRAY, ABIToken.BIGINT, 0, 3,
                ABIToken.BIGINT, 1, 0, ABIToken.BIGINT, 3, 127, 126, 5, ABIToken.BIGINT, 2, -11, -11
        };
        Assert.assertArrayEquals(expected, encoded);
    }

    @Test
    public void testDecodeOneObject(){
        BigInteger[] bigIntegers = new BigInteger[3];
        bigIntegers[0] = new BigInteger(0, new byte[]{0});
        bigIntegers[1] = new BigInteger(1, new byte[]{127, 126, 5});
        bigIntegers[2] = new BigInteger(-1, new byte[]{10, 11});

        byte[] encoded = new byte[]{ABIToken.ARRAY, ABIToken.BIGINT, 0, 3, ABIToken.BIGINT, 1, 0, ABIToken.BIGINT, 3, 127, 126, 5, ABIToken.BIGINT, 2, -11, -11};
        Assert.assertArrayEquals(bigIntegers, (BigInteger[]) ABIUtil.decodeOneObject(encoded));

        encoded = new byte[]{ABIToken.BIGINT, 12, -34, 54, 89, 22, 120, 30, 66, 109, -99, 105, -115, -120};
        Assert.assertEquals(new BigInteger("-10456787634565768768761787000"), ABIUtil.decodeOneObject(encoded));
    }
}
