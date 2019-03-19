package org.aion.avm.core;

import java.math.BigInteger;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.Result;
import org.aion.avm.userlib.AionBuffer;


public class DeploymentArgumentTarget {
    private static String arg0;
    private static Address[] arg1;
    private static int arg2;
    private static double arg3;
    private static byte[] copyOfSelf;

    static {
        Object[] args = ABIDecoder.decodeDeploymentArguments(BlockchainRuntime.getData());
        arg0 = (String)args[0];
        arg1 = (Address[])args[1];
        arg2 = (Integer)args[2];
        arg3 = (Double)args[3];
        copyOfSelf = (byte[]) args[4];
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(DeploymentArgumentTarget.class, BlockchainRuntime.getData());
    }

    public static void correctDeployment() {
        byte[] deploymentArgs = ABIEncoder.encodeDeploymentArguments(arg0, arg1, arg2, arg3, copyOfSelf);
        byte[] codeAndArguments = encodeCodeAndArguments(deploymentArgs);
        Result createResult = BlockchainRuntime.create(BigInteger.ZERO, codeAndArguments, BlockchainRuntime.getEnergyLimit());
        BlockchainRuntime.require(createResult.isSuccess());
    }

    public static void incorrectDeployment() {
        // For this failed attempt, we will omit the final argument, which should cause a deployment failure.
        byte[] deploymentArgs = ABIEncoder.encodeDeploymentArguments(arg0, arg1, arg2, arg3);
        byte[] codeAndArguments = encodeCodeAndArguments(deploymentArgs);
        Result createResult = BlockchainRuntime.create(BigInteger.ZERO, codeAndArguments, BlockchainRuntime.getEnergyLimit());
        // We still want to pass (to ensure this isn't a different failure) so require that the sub-deployment failed.
        BlockchainRuntime.require(!createResult.isSuccess());
    }


    // Note that we currently don't have a way to encode the CodeAndArguments from inside a contract.
    // TODO:  This capability should be added to the new user-space ABI.
    private static byte[] encodeCodeAndArguments(byte[] deploymentArgs) {
        byte[] codeAndArguments = new byte[Integer.BYTES + copyOfSelf.length + Integer.BYTES + deploymentArgs.length];
        AionBuffer codeAndArgumentsBuffer = AionBuffer.wrap(codeAndArguments);
        codeAndArgumentsBuffer.putInt(copyOfSelf.length);
        codeAndArgumentsBuffer.put(copyOfSelf);
        codeAndArgumentsBuffer.putInt(deploymentArgs.length);
        codeAndArgumentsBuffer.put(deploymentArgs);
        return codeAndArguments;
    }
}
