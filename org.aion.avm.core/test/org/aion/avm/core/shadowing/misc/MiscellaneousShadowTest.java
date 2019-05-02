package org.aion.avm.core.shadowing.misc;

import java.lang.reflect.Method;

import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Miscellaneous tests for our shadow implementation.
 */
public class MiscellaneousShadowTest {
    private SimpleAvm avm;
    private Class<?> clazz;

    @Before
    public void setup() throws Exception {
        boolean preserveDebuggability = false;
        this.avm = new SimpleAvm(1_000_000L, preserveDebuggability, TestResource.class);
        this.clazz = avm.getClassLoader().loadUserClassByOriginalName(TestResource.class.getName(), preserveDebuggability);
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    /**
     * Checks that the java.lang.Class.cast() method works as expected on our shadow instance.
     */
    @Test
    public void testClassCast() throws Exception {
        Object string = this.clazz.getMethod(NamespaceMapper.mapMethodName("returnString")).invoke(null);
        Object object = this.clazz.getMethod(NamespaceMapper.mapMethodName("returnObject")).invoke(null);
        Object stringClass = this.clazz.getMethod(NamespaceMapper.mapMethodName("returnClass")).invoke(null);
        Method cast = this.clazz.getMethod(NamespaceMapper.mapMethodName("cast"), s.java.lang.Class.class ,i.IObject.class);
        boolean didCastString = (Boolean)cast.invoke(null, stringClass, string);
        Assert.assertTrue(didCastString);
        boolean didCastObject = (Boolean)cast.invoke(null, stringClass, object);
        Assert.assertFalse(didCastObject);
    }

    /**
     * Checks that the java.lang.Class.getSuperclass() method works as expected on our shadow instance.
     */
    @Test
    public void testSuperclass() throws Exception {
        Object string = this.clazz.getMethod(NamespaceMapper.mapMethodName("returnString")).invoke(null);
        Object object = this.clazz.getMethod(NamespaceMapper.mapMethodName("returnObject")).invoke(null);
        Method getClass = this.clazz.getMethod(NamespaceMapper.mapMethodName("getClass"), i.IObject.class);
        Method getSuperclass = this.clazz.getMethod(NamespaceMapper.mapMethodName("getSuperclass"), s.java.lang.Class.class);
        
        Object stringClass = getClass.invoke(null, string);
        Object objectClass = getClass.invoke(null, object);
        Object stringSuper = getSuperclass.invoke(null, stringClass);
        Object objectSuper = getSuperclass.invoke(null, objectClass);
        Assert.assertTrue(objectClass == stringSuper);
        Assert.assertTrue(null == objectSuper);
    }

    /**
     * Tests that the String.valueOf helper correctly calls the avm_toString(), as opposed to toString().
     */
    @Test
    public void testStringValueOf() throws Exception {
        int valueOfLength = (Integer) this.clazz.getMethod(NamespaceMapper.mapMethodName("checkValueOf")).invoke(null);
        // We override the toString to return an empty string.
        Assert.assertEquals(0, valueOfLength);
    }

    /**
     * Tests that the " " + " " helper correctly calls the avm_toString(), as opposed to toString().
     */
    @Test
    public void testDirectStringAppend() throws Exception {
        int valueOfLength = (Integer) this.clazz.getMethod(NamespaceMapper.mapMethodName("checkStringAppend")).invoke(null);
        // We override the toString to return an empty string and we add 2 spaces to it.
        Assert.assertEquals(2, valueOfLength);
    }

    /**
     * Tests that the StringBuilder.append helper correctly calls the avm_toString(), as opposed to toString().
     */
    @Test
    public void testStringBuilderAppend() throws Exception {
        int valueOfLength = (Integer) this.clazz.getMethod(NamespaceMapper.mapMethodName("checkStringBuilderAppend")).invoke(null);
        // We override the toString to return an empty string and we add 2 spaces to it.
        Assert.assertEquals(2, valueOfLength);
    }

    /**
     * Tests that the StringBuilder.append helper correctly handles null arguments.
     */
    @Test
    public void testNullStringBuilderAppend() throws Exception {
        int valueOfLength = (Integer) this.clazz.getMethod(NamespaceMapper.mapMethodName("checkNullStringBuilderAppend")).invoke(null);
        // We wrote 7 nulls, and only 2 caused NPE.  Of those 5 writes, 4 were "null" but 1 was "n".
        Assert.assertEquals(17, valueOfLength);
    }

    @Test
    public void testStringBufferGetChars() throws Exception {
        CharArray result = (CharArray) this.clazz.getMethod(NamespaceMapper.mapMethodName("stringBufferGetChars")).invoke(null);
        Assert.assertEquals("tester", String.valueOf(result.getUnderlying()));
    }

    @Test
    public void testStringBufferInsert() throws Exception {
        this.clazz.getMethod(NamespaceMapper.mapMethodName("stringBufferInsert")).invoke(null);
    }

    @Test
    public void testStringBuilderInsertObject() throws Exception {
        this.clazz.getMethod(NamespaceMapper.mapMethodName("stringBuilderInsertObject")).invoke(null);
    }

    @Test
    public void testStringBuilderInsertString() throws Exception {
        s.java.lang.String result = (s.java.lang.String) this.clazz.getMethod(NamespaceMapper.mapMethodName("stringBuilderInsertString")).invoke(null);
        Assert.assertEquals("InsertTesting", result.getUnderlying());
    }

    @Test
    public void testStringBuilderAppendStringBuilder() throws Exception {
        s.java.lang.String result = (s.java.lang.String) this.clazz.getMethod(NamespaceMapper.mapMethodName("stringBuilderAppend")).invoke(null);
        Assert.assertEquals("TestingBuilder", result.getUnderlying());
    }

    @Test
    public void testlastIndexOfStringBuilder() throws Exception{
        int result = (int) this.clazz.getMethod(NamespaceMapper.mapMethodName("lastIndexOfStringBuilder")).invoke(null);
        Assert.assertEquals(3, result);
    }

    @Test
    public void teststringBufferLength() throws Exception{
        int result = (int) this.clazz.getMethod(NamespaceMapper.mapMethodName("stringBufferLength")).invoke(null);
        Assert.assertEquals(7, result);
    }
}
