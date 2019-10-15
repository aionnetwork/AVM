package org.aion.avm.tooling.deploy.eliminator;

import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIException;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.avm.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;


import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.jar.JarInputStream;

public class ConstantRemoverTest {

    @Test
    public void decoderException() throws Exception {
        byte[] encoded = ABIEncoder.encodeOneInteger(10);
        byte[] jarBytes = TestUtil.serializeClassesAsJar(ABIDecoder.class);
        byte[] updatedJarBytes = ConstantRemover.removeABIExceptionMessages(jarBytes);

        JarInputStream jarReader = new JarInputStream(new ByteArrayInputStream(updatedJarBytes), true);
        Map<String, byte[]> classMap = Utilities.extractClasses(jarReader, Utilities.NameStyle.DOT_NAME);
        Class<?> clazz = new SingleLoader().loadClassFromByteCode(ABIDecoder.class.getName(), classMap.get(ABIDecoder.class.getName()));

        Constructor constructor = clazz.getConstructor(byte[].class);
        Object obj = constructor.newInstance((Object) encoded);
        Method decodeOneInteger = clazz.getMethod("decodeOneInteger");
        int res = (int) decodeOneInteger.invoke(obj);
        Assert.assertEquals(10, res);

        // exception case
        byte[] malformed = new byte[1];
        System.arraycopy(encoded, 0, malformed, 0, 1);

        obj = constructor.newInstance((Object) malformed);

        boolean reached = false;
        try {
            decodeOneInteger.invoke(obj);
        } catch (Exception e) {
            reached = true;
            Assert.assertTrue(e.getCause() instanceof ABIException);
            Assert.assertNull(e.getCause().getMessage());
        }
        Assert.assertTrue(reached);
    }

    @Test
    public void encoderBigInteger() throws Exception {
        BigInteger value = BigInteger.TEN;

        byte[] jarBytes = TestUtil.serializeClassesAsJar(ABIEncoder.class);
        byte[] updatedJarBytes = ConstantRemover.removeABIExceptionMessages(jarBytes);

        JarInputStream jarReader = new JarInputStream(new ByteArrayInputStream(updatedJarBytes), true);
        Map<String, byte[]> classMap = Utilities.extractClasses(jarReader, Utilities.NameStyle.DOT_NAME);
        Class<?> clazz = new SingleLoader().loadClassFromByteCode(ABIEncoder.class.getName(), classMap.get(ABIEncoder.class.getName()));

        Method encodeOneBigInteger = clazz.getMethod("encodeOneBigInteger", BigInteger.class);
        byte[] res = (byte[]) encodeOneBigInteger.invoke(null, value);
        Assert.assertArrayEquals(ABIEncoder.encodeOneBigInteger(value), res);

        // exception case
        byte[] Exception33BytesArray = new byte[33];
        Arrays.fill(Exception33BytesArray, Byte.MAX_VALUE);
        BigInteger exceptionValue = new BigInteger(Exception33BytesArray);

        boolean reached = false;
        try {
            res = (byte[]) encodeOneBigInteger.invoke(null, exceptionValue);
        } catch (Exception e) {
            reached = true;
            Assert.assertTrue(e.getCause() instanceof ABIException);
            Assert.assertNull(e.getCause().getMessage());
        }
        Assert.assertTrue(reached);
    }

    @Test
    public void streamingEncoderBigInteger() throws Exception {
        BigInteger value = BigInteger.TEN;

        byte[] jarBytes = TestUtil.serializeClassesAsJar(ABIStreamingEncoder.class);
        byte[] updatedJarBytes = ConstantRemover.removeABIExceptionMessages(jarBytes);

        JarInputStream jarReader = new JarInputStream(new ByteArrayInputStream(updatedJarBytes), true);
        Map<String, byte[]> classMap = Utilities.extractClasses(jarReader, Utilities.NameStyle.DOT_NAME);
        Class<?> clazz = new SingleLoader().loadClassFromByteCode(ABIStreamingEncoder.class.getName(), classMap.get(ABIStreamingEncoder.class.getName()));

        Constructor constructor = clazz.getConstructor();
        Object obj = constructor.newInstance();
        Method encodeOneBigInteger = clazz.getMethod("encodeOneBigInteger", BigInteger.class);

        encodeOneBigInteger.invoke(obj, value);
        Method toBytes = clazz.getMethod("toBytes");
        byte[] res = (byte[]) toBytes.invoke(obj);

        Assert.assertArrayEquals(new ABIStreamingEncoder().encodeOneBigInteger(value).toBytes(), (res));

        // exception case
        byte[] Exception33BytesArray = new byte[33];
        Arrays.fill(Exception33BytesArray, Byte.MAX_VALUE);
        BigInteger exceptionValue = new BigInteger(Exception33BytesArray);

        boolean reached = false;
        try {
            encodeOneBigInteger.invoke(obj, exceptionValue);
        } catch (Exception e) {
            reached = true;
            Assert.assertTrue(e.getCause() instanceof ABIException);
            Assert.assertNull(e.getCause().getMessage());
        }
        Assert.assertTrue(reached);
    }

    public class SingleLoader extends ClassLoader {

        public Class<?> loadClassFromByteCode(String name, byte[] bytecode) {
            Class<?> clazz = this.defineClass(name, bytecode, 0, bytecode.length);
            return clazz;
        }
    }
}
