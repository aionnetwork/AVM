package org.aion.avm.core.shadowing.misc;

import java.lang.reflect.Method;

import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
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
        this.avm = new SimpleAvm(1_000_000L, TestResource.class);
        this.clazz = avm.getClassLoader().loadUserClassByOriginalName(TestResource.class.getName());
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
        Object string = this.clazz.getMethod(UserClassMappingVisitor.mapMethodName("returnString")).invoke(null);
        Object object = this.clazz.getMethod(UserClassMappingVisitor.mapMethodName("returnObject")).invoke(null);
        Object stringClass = this.clazz.getMethod(UserClassMappingVisitor.mapMethodName("returnClass")).invoke(null);
        Method cast = this.clazz.getMethod(UserClassMappingVisitor.mapMethodName("cast"), org.aion.avm.shadow.java.lang.Class.class ,org.aion.avm.internal.IObject.class);
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
        Object string = this.clazz.getMethod(UserClassMappingVisitor.mapMethodName("returnString")).invoke(null);
        Object object = this.clazz.getMethod(UserClassMappingVisitor.mapMethodName("returnObject")).invoke(null);
        Method getClass = this.clazz.getMethod(UserClassMappingVisitor.mapMethodName("getClass"), org.aion.avm.internal.IObject.class);
        Method getSuperclass = this.clazz.getMethod(UserClassMappingVisitor.mapMethodName("getSuperclass"), org.aion.avm.shadow.java.lang.Class.class);
        
        Object stringClass = getClass.invoke(null, string);
        Object objectClass = getClass.invoke(null, object);
        Object stringSuper = getSuperclass.invoke(null, stringClass);
        Object objectSuper = getSuperclass.invoke(null, objectClass);
        Assert.assertTrue(objectClass == stringSuper);
        Assert.assertTrue(null == objectSuper);
    }
}
