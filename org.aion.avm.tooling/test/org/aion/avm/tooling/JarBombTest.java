package org.aion.avm.tooling;

import static org.junit.Assert.assertEquals;

import avm.Address;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.aion.avm.userlib.CodeAndArguments;
import org.aion.kernel.AvmWrappedTransactionResult.AvmInternalError;
import org.aion.types.TransactionResult;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests deploying jar files that compress to be small relative to how large their decompressed form is.
 */
public class JarBombTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);

    private static final int TOO_LARGE_CLASS_SIZE = 1024 * 1024;
    private static final int LARGE_CLASS_SIZE = 1024 * 1023;

    private Address deployer = avmRule.getPreminedAccount();

    /**
     * Deploys a jar that consists of a single 1MiB class. This is the class size at which a class is
     * rejected for being too large.
     *
     * The jar itself compresses down to 1KB.
     */
    @Test
    public void testVeryLargeSingleClassJarBomb() throws IOException {
        byte[] jar = makeBomb(1, TOO_LARGE_CLASS_SIZE);
        TransactionResult result = avmRule.deploy(deployer, BigInteger.ZERO, jar, 5_000_000, 1).getTransactionResult();
        assertEquals(AvmInternalError.FAILED_INVALID_DATA.error, result.transactionStatus.causeOfError);
    }

    /**
     * Deploys a jar that consists of 10 class files that are each 1 byte short of being 1MiB in size.
     * This is so that they are not rejected as over-sized class files (1MiB is our limit).
     *
     * The total jar size is ~10MiB and compresses down to 114KB.
     */
    @Test
    public void testLargeMultiClassJarBomb() throws IOException {
        byte[] jar = makeBomb(10, LARGE_CLASS_SIZE);
        TransactionResult result = avmRule.deploy(deployer, BigInteger.ZERO, jar, 5_000_000, 1).getTransactionResult();
        assertEquals(AvmInternalError.FAILED_INVALID_DATA.error, result.transactionStatus.causeOfError);
    }


    private byte[] makeBomb(int numEntries, int entrySize) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipStream = new ZipOutputStream(new BufferedOutputStream(byteStream))) {
            for (int i = 0; i < numEntries; i++) {
                zipStream.putNextEntry(new ZipEntry("Bomb" + i + ".class"));
                zipStream.write(new byte[entrySize]);
                zipStream.closeEntry();
            }
        }
        return new CodeAndArguments(byteStream.toByteArray(), new byte[0]).encodeToBytes();
    }
}
