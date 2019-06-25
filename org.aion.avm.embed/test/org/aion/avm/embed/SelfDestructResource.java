package org.aion.avm.embed;

import java.math.BigInteger;
import avm.Address;
import avm.Blockchain;
import avm.Result;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;


/**
 * A test created as part of issue-299 to test out self-destruct behaviour and how it interacts with reentrant calls and transfers.
 */
public class SelfDestructResource {
    public static final int DELETE_AND_RETURN = 1; 
    public static final int JUST_RETURN = 2; 
    public static final int DELETE_AND_FAIL_TO_CALL_SELF = 3; 
    public static final int CALL_TO_DELETE_SUCCESS = 4; 
    public static final int CALL_TO_DELETE_FAIL = 5;

    static {
        // If we passed in non-empty args to deployment, destroy ourselves.
        byte[] data = Blockchain.getData();
        if (data.length > 0) {
            Address beneficiary = new Address(data);
            Blockchain.selfDestruct(beneficiary);
        }
    }

    @Callable
    public static int deleteAndReturn(Address beneficiary) {
        Blockchain.selfDestruct(beneficiary);
        return DELETE_AND_RETURN;
    }

    @Callable
    public static int deleteCallAndReturn(Address beneficiary, Address target) {
        Blockchain.selfDestruct(beneficiary);
        BigInteger value = BigInteger.ZERO;
        byte[] data = ABIEncoder.encodeOneString("justReturn");
        long energyLimit = Blockchain.getRemainingEnergy() / 2;
        byte[] response = Blockchain.call(target, value, data, energyLimit).getReturnData();
        ABIDecoder decoder = new ABIDecoder(response);
        return decoder.decodeOneInteger();
    }

    @Callable
    public static int justReturn() {
        return JUST_RETURN;
    }

    @Callable
    public static Address deleteDeployAndReturnAddress(Address beneficiary, byte[] data) {
        Blockchain.selfDestruct(beneficiary);
        BigInteger value = BigInteger.ZERO;
        long energyLimit = Blockchain.getRemainingEnergy() / 2;
        Result result = Blockchain.create(value, data, energyLimit);
        return new Address(result.getReturnData());
    }

    @Callable
    public static long deleteAndReturnBalance(Address beneficiary) {
        Blockchain.selfDestruct(beneficiary);
        return Blockchain.getBalance(Blockchain.getAddress()).longValueExact();
    }

    @Callable
    public static long deleteAndReturnBalanceFromAnother(Address beneficiary, Address target) {
        Blockchain.selfDestruct(beneficiary);
        BigInteger value = BigInteger.ZERO;

        byte[] methodNameBytes = ABIEncoder.encodeOneString("returnCallerBalance");
        byte[] argBytes = ABIEncoder.encodeOneAddress(Blockchain.getAddress());
        byte[] data = new byte[methodNameBytes.length + argBytes.length];
        System.arraycopy(methodNameBytes, 0, data, 0, methodNameBytes.length);
        System.arraycopy(argBytes, 0, data, methodNameBytes.length, argBytes.length);

        long energyLimit = Blockchain.getRemainingEnergy() / 2;
        byte[] response = Blockchain.call(target, value, data, energyLimit).getReturnData();
        ABIDecoder decoder = new ABIDecoder(response);
        return decoder.decodeOneLong();
    }

    @Callable
    public static long returnCallerBalance(Address caller) {
        return Blockchain.getBalance(caller).longValueExact();
    }

    @Callable
    public static int deleteAndFailToCallSelf(Address beneficiary) {
        Blockchain.selfDestruct(beneficiary);
        BigInteger value = BigInteger.ZERO;
        byte[] data = ABIEncoder.encodeOneString("justReturn");
        long energyLimit = Blockchain.getRemainingEnergy() / 2;
        // Calling someone deleted is always a success but we expect this to be an empty array, not the value this method would return.
        Result result = Blockchain.call(Blockchain.getAddress(), value, data, energyLimit);
        assert (result.isSuccess());
        assert (null == result.getReturnData());
        return DELETE_AND_FAIL_TO_CALL_SELF;
    }

    @Callable
    public static int callToDeleteSuccess(Address beneficiary, Address target) {
        // Call the target to get them to delete themselves.
        BigInteger value = BigInteger.ZERO;

        byte[] methodNameBytes = ABIEncoder.encodeOneString("deleteAndReturn");
        byte[] argBytes = ABIEncoder.encodeOneAddress(beneficiary);
        byte[] data = new byte[methodNameBytes.length + argBytes.length];
        System.arraycopy(methodNameBytes, 0, data, 0, methodNameBytes.length);
        System.arraycopy(argBytes, 0, data, methodNameBytes.length, argBytes.length);

        long energyLimit = Blockchain.getRemainingEnergy() / 2;
        byte[] response = Blockchain.call(target, value, data, energyLimit).getReturnData();
        ABIDecoder decoder = new ABIDecoder(response);
        int decodedResponse = decoder.decodeOneInteger();
        assert (DELETE_AND_RETURN == decodedResponse);
        
        // Call back to ourselves, to verify that we are ok.
        data = ABIEncoder.encodeOneString("justReturn");
        energyLimit = Blockchain.getRemainingEnergy() / 2;
        response = Blockchain.call(Blockchain.getAddress(), value, data, energyLimit).getReturnData();
        decoder = new ABIDecoder(response);
        decodedResponse = decoder.decodeOneInteger();
        assert (JUST_RETURN == decodedResponse);
        
        // Try to call them, verifying that they are not accessible.
        data = ABIEncoder.encodeOneString("justReturn");
        energyLimit = Blockchain.getRemainingEnergy() / 2;
        Result result = Blockchain.call(target, value, data, energyLimit);
        response = result.getReturnData();
        // Calling a deleted DApp is a success (since it is an account), but returns null (since there is no code).
        assert (result.isSuccess());
        assert (null == response);
        return CALL_TO_DELETE_SUCCESS;
    }

    @Callable
    public static int callToDeleteFailure(Address beneficiary, Address target) {
        // Call the target to get them to delete themselves.
        BigInteger value = BigInteger.ZERO;

        byte[] methodNameBytes = ABIEncoder.encodeOneString("deleteAndFail");
        byte[] argBytes = ABIEncoder.encodeOneAddress(beneficiary);
        byte[] data = new byte[methodNameBytes.length + argBytes.length];
        System.arraycopy(methodNameBytes, 0, data, 0, methodNameBytes.length);
        System.arraycopy(argBytes, 0, data, methodNameBytes.length, argBytes.length);

        long energyLimit = Blockchain.getRemainingEnergy() / 2;
        Result result = Blockchain.call(target, value, data, energyLimit);
        assert (!result.isSuccess());
        
        // Call back to ourselves, to verify that we are ok.
        data = ABIEncoder.encodeOneString("justReturn");
        energyLimit = Blockchain.getRemainingEnergy() / 2;
        byte[] response = Blockchain.call(Blockchain.getAddress(), value, data, energyLimit).getReturnData();
        ABIDecoder decoder = new ABIDecoder(response);
        int decodedResponse = decoder.decodeOneInteger();
        assert (JUST_RETURN == decodedResponse);
        
        // Try to call them, verifying that they are still accessible.
        data = ABIEncoder.encodeOneString("justReturn");
        energyLimit = Blockchain.getRemainingEnergy() / 2;
        result = Blockchain.call(target, value, data, energyLimit);
        assert (result.isSuccess());
        return CALL_TO_DELETE_FAIL;
    }

    @Callable
    public static int deleteAndFail(Address beneficiary) {
        Blockchain.selfDestruct(beneficiary);
        throw new AssertionError();
    }

    @Callable
    public static long deleteAndReturnBeneficiaryBalance(Address beneficiary) {
        Blockchain.selfDestruct(beneficiary);
        return Blockchain.getBalance(beneficiary).longValueExact();
    }
}
