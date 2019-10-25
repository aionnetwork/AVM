package org.aion.avm.core.util;

import java.io.File;
import java.math.BigInteger;

import org.aion.types.AionAddress;

import i.RuntimeAssertionError;


/**
 * Created as part of AKI-467 to capture contracts deployed on an AVM instance for offline analysis.
 */
public class ContractCaptureTool {
    private static final String CODE_FILE_NAME = "code.jar";
    private static final String ARGUMENTS_FILE_NAME = "arguments.bin";
    private static final String CREATOR_FILE_NAME = "creator.bin";
    private static final String NONCE_FILE_NAME = "creator_nonce.bin";
    private static final String BLOCK_FILE_NAME = "block_height.txt";

    private final File contractCaptureDirectory;

    public ContractCaptureTool(File contractCaptureDirectory) {
        this.contractCaptureDirectory = contractCaptureDirectory;
    }

    public void startup() {
        if (!this.contractCaptureDirectory.exists()) {
            this.contractCaptureDirectory.mkdirs();
        }
        RuntimeAssertionError.assertTrue(this.contractCaptureDirectory.isDirectory());
    }

    public void captureDeployment(long blockNumber, AionAddress senderAddress, AionAddress newContractAddress, BigInteger senderNonce, byte[] code, byte[] arguments) {
        String newContract = Helpers.bytesToHexString(newContractAddress.toByteArray());
        File thisDirectory = new File(this.contractCaptureDirectory, newContract);
        thisDirectory.mkdirs();
        Helpers.writeBytesToFile(code, new File(thisDirectory, CODE_FILE_NAME).getAbsolutePath());
        if (null != arguments) {
            Helpers.writeBytesToFile(arguments, new File(thisDirectory, ARGUMENTS_FILE_NAME).getAbsolutePath());
        }
        Helpers.writeBytesToFile(senderAddress.toByteArray(), new File(thisDirectory, CREATOR_FILE_NAME).getAbsolutePath());
        Helpers.writeBytesToFile(senderNonce.toByteArray(), new File(thisDirectory, NONCE_FILE_NAME).getAbsolutePath());
        Helpers.writeBytesToFile(Long.toString(blockNumber).getBytes(), new File(thisDirectory, BLOCK_FILE_NAME).getAbsolutePath());
    }
}
