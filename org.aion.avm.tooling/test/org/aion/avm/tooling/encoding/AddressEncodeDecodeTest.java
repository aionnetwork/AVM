package org.aion.avm.tooling.encoding;

import avm.Address;

import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Test to validate encoding and decoding of Address from api to shadow type
 */
public class AddressEncodeDecodeTest {

    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    public Address dappAddress;
    public Address from = avmRule.getPreminedAccount();
    public Address randomAddress = avmRule.getRandomAddress(BigInteger.ZERO);

    @Before
    public void deploy() {
        byte[] dappBytes = avmRule.getDappBytes(AddressEndodeDecodeTarget.class, new byte[0]);
        dappAddress = avmRule.deploy(from, BigInteger.ZERO, dappBytes).getDappAddress();
    }

    @Test
    public void testAddressEncodeDecode(){
        byte[] data = ABIUtil.encodeMethodArguments("addressEncodeDecode");
        AvmRule.ResultWrapper r = avmRule.call(from, dappAddress, BigInteger.ZERO, data);
        Assert.assertTrue(r.getTransactionResult().getResultCode().isSuccess());
    }

    @Test
    public void testCreateAddress(){
        byte[] data = ABIUtil.encodeMethodArguments("createAddress");
        AvmRule.ResultWrapper r = avmRule.call(from, dappAddress, BigInteger.ZERO, data);
        Assert.assertTrue(r.getTransactionResult().getResultCode().isSuccess());
    }

    @Test
    public void testAddressArgumentReturn(){
        byte[] data = ABIUtil.encodeMethodArguments("saveAddress", randomAddress);
        AvmRule.ResultWrapper r = avmRule.call(from, dappAddress, BigInteger.ZERO, data);
        Assert.assertTrue(r.getTransactionResult().getResultCode().isSuccess());

        data = ABIUtil.encodeMethodArguments("getStoredAddress");
        r = avmRule.call(from, dappAddress, BigInteger.ZERO, data);
        Assert.assertTrue(r.getTransactionResult().getResultCode().isSuccess());
        Assert.assertEquals(randomAddress, r.getDecodedReturnData());
    }

    @Test
    public void testAddressArrayArgument(){
        Address[] tempArray = new Address[3];
        for(int i = 0 ; i< tempArray.length; i++){
            tempArray[i] = avmRule.getRandomAddress(BigInteger.ZERO);
        }
        byte[] data = ABIUtil.encodeMethodArguments("checkAddressArrayArgument", (Object)tempArray);
        AvmRule.ResultWrapper r = avmRule.call(from, dappAddress, BigInteger.ZERO, data);
        Assert.assertTrue(r.getTransactionResult().getResultCode().isSuccess());
    }

    @Test
    public void testAddressArrayReturn(){
        byte[] data = ABIUtil.encodeMethodArguments("getAddressArray", 5);
        AvmRule.ResultWrapper r = avmRule.call(from, dappAddress, BigInteger.ZERO, data);
        Assert.assertTrue(r.getTransactionResult().getResultCode().isSuccess());
        Assert.assertEquals(5, ((Address []) r.getDecodedReturnData()).length);
    }

    @Test
    public void testAddressArrayEncodeDecode(){
        Address[] tempArray = new Address[3];
        for(int i = 0 ; i< tempArray.length; i++){
            tempArray[i] = avmRule.getRandomAddress(BigInteger.ZERO);
        }
        byte[] data = ABIUtil.encodeMethodArguments("addressArrayEncodeDecode", (Object)tempArray);
        AvmRule.ResultWrapper r = avmRule.call(from, dappAddress, BigInteger.ZERO, data);
        Assert.assertTrue(r.getTransactionResult().getResultCode().isSuccess());
        Assert.assertTrue(Arrays.equals(tempArray, (Address [])r.getDecodedReturnData()));
    }
}
