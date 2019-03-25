package org.aion.avm.tooling;

import java.math.BigInteger;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.Result;
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
        byte[] data = BlockchainRuntime.getData();
        if (data.length > 0) {
            Address beneficiary = new Address(data);
            BlockchainRuntime.selfDestruct(beneficiary);
        }
    }

    @Callable
    public static int deleteAndReturn(Address beneficiary) {
        BlockchainRuntime.selfDestruct(beneficiary);
        return DELETE_AND_RETURN;
    }

    @Callable
    public static int deleteCallAndReturn(Address beneficiary, Address target) {
        BlockchainRuntime.selfDestruct(beneficiary);
        BigInteger value = BigInteger.ZERO;
        byte[] data = ABIEncoder.encodeMethodArguments("justReturn");
        long energyLimit = BlockchainRuntime.getRemainingEnergy() / 2;
        byte[] response = BlockchainRuntime.call(target, value, data, energyLimit).getReturnData();
        return (Integer)ABIDecoder.decodeOneObject(response);
    }

    @Callable
    public static int justReturn() {
        return JUST_RETURN;
    }

    @Callable
    public static Address deleteDeployAndReturnAddress(Address beneficiary, byte[] data) {
        BlockchainRuntime.selfDestruct(beneficiary);
        BigInteger value = BigInteger.ZERO;
        long energyLimit = BlockchainRuntime.getRemainingEnergy() / 2;
        Result result = BlockchainRuntime.create(value, data, energyLimit);
        return new Address(result.getReturnData());
    }

    @Callable
    public static long deleteAndReturnBalance(Address beneficiary) {
        BlockchainRuntime.selfDestruct(beneficiary);
        return BlockchainRuntime.getBalance(BlockchainRuntime.getAddress()).longValueExact();
    }

    @Callable
    public static long deleteAndReturnBalanceFromAnother(Address beneficiary, Address target) {
        BlockchainRuntime.selfDestruct(beneficiary);
        BigInteger value = BigInteger.ZERO;
        byte[] data = ABIEncoder.encodeMethodArguments("returnCallerBalance", BlockchainRuntime.getAddress());
        long energyLimit = BlockchainRuntime.getRemainingEnergy() / 2;
        byte[] response = BlockchainRuntime.call(target, value, data, energyLimit).getReturnData();
        return (Long)ABIDecoder.decodeOneObject(response);
    }

    @Callable
    public static long returnCallerBalance(Address caller) {
        return BlockchainRuntime.getBalance(caller).longValueExact();
    }

    @Callable
    public static int deleteAndFailToCallSelf(Address beneficiary) {
        BlockchainRuntime.selfDestruct(beneficiary);
        BigInteger value = BigInteger.ZERO;
        byte[] data = ABIEncoder.encodeMethodArguments("justReturn");
        long energyLimit = BlockchainRuntime.getRemainingEnergy() / 2;
        // Calling someone deleted is always a success but we expect this to be an empty array, not the value this method would return.
        Result result = BlockchainRuntime.call(BlockchainRuntime.getAddress(), value, data, energyLimit);
        assert (result.isSuccess());
        assert (null == result.getReturnData());
        return DELETE_AND_FAIL_TO_CALL_SELF;
    }

    @Callable
    public static int callToDeleteSuccess(Address beneficiary, Address target) {
        // Call the target to get them to delete themselves.
        BigInteger value = BigInteger.ZERO;
        byte[] data = ABIEncoder.encodeMethodArguments("deleteAndReturn", beneficiary);
        long energyLimit = BlockchainRuntime.getRemainingEnergy() / 2;
        byte[] response = BlockchainRuntime.call(target, value, data, energyLimit).getReturnData();
        assert (DELETE_AND_RETURN == (Integer)ABIDecoder.decodeOneObject(response));
        
        // Call back to ourselves, to verify that we are ok.
        data = ABIEncoder.encodeMethodArguments("justReturn");
        energyLimit = BlockchainRuntime.getRemainingEnergy() / 2;
        response = BlockchainRuntime.call(BlockchainRuntime.getAddress(), value, data, energyLimit).getReturnData();
        assert (JUST_RETURN == (Integer)ABIDecoder.decodeOneObject(response));
        
        // Try to call them, verifying that they are not accessible.
        data = ABIEncoder.encodeMethodArguments("justReturn");
        energyLimit = BlockchainRuntime.getRemainingEnergy() / 2;
        response = BlockchainRuntime.call(target, value, data, energyLimit).getReturnData();
        assert (JUST_RETURN == (Integer)ABIDecoder.decodeOneObject(response));
        return CALL_TO_DELETE_SUCCESS;
    }

    @Callable
    public static int callToDeleteFailure(Address beneficiary, Address target) {
        // Call the target to get them to delete themselves.
        BigInteger value = BigInteger.ZERO;
        byte[] data = ABIEncoder.encodeMethodArguments("deleteAndFail", beneficiary);
        long energyLimit = BlockchainRuntime.getRemainingEnergy() / 2;
        Result result = BlockchainRuntime.call(target, value, data, energyLimit);
        assert (!result.isSuccess());
        
        // Call back to ourselves, to verify that we are ok.
        data = ABIEncoder.encodeMethodArguments("justReturn");
        energyLimit = BlockchainRuntime.getRemainingEnergy() / 2;
        byte[] response = BlockchainRuntime.call(BlockchainRuntime.getAddress(), value, data, energyLimit).getReturnData();
        assert (JUST_RETURN == (Integer)ABIDecoder.decodeOneObject(response));
        
        // Try to call them, verifying that they are still accessible.
        data = ABIEncoder.encodeMethodArguments("justReturn");
        energyLimit = BlockchainRuntime.getRemainingEnergy() / 2;
        result = BlockchainRuntime.call(target, value, data, energyLimit);
        assert (result.isSuccess());
        return CALL_TO_DELETE_FAIL;
    }

    @Callable
    public static int deleteAndFail(Address beneficiary) {
        BlockchainRuntime.selfDestruct(beneficiary);
        throw new AssertionError();
    }

    @Callable
    public static long deleteAndReturnBeneficiaryBalance(Address beneficiary) {
        BlockchainRuntime.selfDestruct(beneficiary);
        return BlockchainRuntime.getBalance(beneficiary).longValueExact();
    }
}
