package org.aion.avm.tooling;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import avm.Address;
import java.math.BigInteger;
import java.util.Arrays;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.AvmTransactionResult.Code;
import org.junit.ClassRule;
import org.junit.Test;

public class ManipulateCreateAndCallResultsTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);

    private Address deployer = avmRule.getPreminedAccount();

    /**
     * NOTE: this test shows that it is not possible to create a dApp with an external transaction and
     * then to modify the dApp address using the returned array.
     */
    @Test
    public void testManipulateDeployResult() {
        byte[] jar = avmRule.getDappBytes(DappManipulator.class, new byte[0]);

        AvmTransactionResult result = avmRule.deploy(deployer, BigInteger.ZERO, jar, 5_000_000, 1).getTransactionResult();
        assertEquals(Code.SUCCESS, result.getResultCode());

        byte[] originalAddress = Arrays.copyOf(result.getReturnData(), 32);

        // Modify the returned byte array.
        result.getReturnData()[0] = (byte) ~result.getReturnData()[0];
        byte[] manipulatedAddress = result.getReturnData();

        avmRule.kernel.generateBlock();

        Address contract = new Address(originalAddress);
        Address manipulatedContract = new Address(manipulatedAddress);

        // The contract is found at the original address
        byte[] data = ABIUtil.encodeMethodArguments("getAddress");
        result = avmRule.call(deployer, contract, BigInteger.ZERO, data, 2_000_000, 1).getTransactionResult();
        assertEquals(Code.SUCCESS, result.getResultCode());
        assertArrayEquals(originalAddress, new ABIDecoder(result.getReturnData()).decodeOneByteArray());

        // No such contract is found at the manipulated address
        data = ABIUtil.encodeMethodArguments("getAddress");
        result = avmRule.call(deployer, manipulatedContract, BigInteger.ZERO, data, 2_000_000, 1).getTransactionResult();
        assertEquals(Code.SUCCESS, result.getResultCode());
        assertNull(result.getReturnData());
    }

    @Test
    public void testManipulateCallResult() {
        // Deploy the field target class that will return its field to a dapp that tries to modify it.
        byte[] jar = avmRule.getDappBytes(ManipulateFieldTarget.class, new byte[0]);
        AvmTransactionResult result = avmRule.deploy(deployer, BigInteger.ZERO, jar, 5_000_000, 1).getTransactionResult();
        assertEquals(Code.SUCCESS, result.getResultCode());
        Address fieldContract = new Address(result.getReturnData());

        // Deploy the dapp that will query the other dapp's field and try to modify it.
        avmRule.kernel.generateBlock();
        jar = avmRule.getDappBytes(DappManipulator.class, new byte[0]);
        result = avmRule.deploy(deployer, BigInteger.ZERO, jar, 5_000_000, 1).getTransactionResult();
        assertEquals(Code.SUCCESS, result.getResultCode());
        Address contract = new Address(result.getReturnData());

        // Call the method that does the manipulation. If it succeeds then nothing was modified.
        avmRule.kernel.generateBlock();
        byte[] data = ABIUtil.encodeMethodArguments("manipulateField", fieldContract);
        result = avmRule.call(deployer, contract, BigInteger.ZERO, data, 2_000_000, 1).getTransactionResult();
        assertEquals(Code.SUCCESS, result.getResultCode());
    }

    @Test
    public void testManipulateCreateResult() {
        // Deploy the dapp that will create the other dapp and modify its returned address.
        avmRule.kernel.generateBlock();
        byte[] jar = avmRule.getDappBytes(DappManipulator.class, new byte[0]);
        AvmTransactionResult result = avmRule.deploy(deployer, BigInteger.ZERO, jar, 5_000_000, 1).getTransactionResult();
        assertEquals(Code.SUCCESS, result.getResultCode());
        Address contract = new Address(result.getReturnData());

        // Call the method that does the manipulation. If it succeeds then nothing was modified.
        avmRule.kernel.generateBlock();
        byte[] otherDapp = avmRule.getDappBytes(ManipulateFieldTarget.class, new byte[0]);
        byte[] data = ABIUtil.encodeMethodArguments("manipulateDeployAddress", otherDapp);
        result = avmRule.call(deployer, contract, BigInteger.ZERO, data, 2_000_000, 1).getTransactionResult();
        assertEquals(Code.SUCCESS, result.getResultCode());

        // Make a call into the other dapp to verify it exists & its address was not modified.
        avmRule.kernel.generateBlock();
        Address otherDappOriginalAddress = new Address(new ABIDecoder(result.getReturnData()).decodeOneByteArray());
        data = ABIUtil.encodeMethodArguments("getField");
        result = avmRule.call(deployer, otherDappOriginalAddress, BigInteger.ZERO, data, 2_000_000, 1).getTransactionResult();
        assertEquals(Code.SUCCESS, result.getResultCode());

        // Verify that we can call the 'getField' method of the deployed contract.
        assertArrayEquals(new byte[20], new ABIDecoder(result.getReturnData()).decodeOneByteArray());
    }
}
