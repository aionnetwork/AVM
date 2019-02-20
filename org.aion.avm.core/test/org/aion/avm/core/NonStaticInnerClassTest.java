package org.aion.avm.core;

import java.lang.reflect.Method;

import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.junit.Assert;
import org.junit.Test;


/**
 * issue-156:  Verifies that non-static inner classes can be instantiated (including with other arguments).
 */
public class NonStaticInnerClassTest {
    private boolean preserveDebuggability = false;

    @Test
    public void testInnerClassLoads() throws Exception {
        SimpleAvm avm = new SimpleAvm(10000L, this.preserveDebuggability, NonStaticInnerClassTarget.class, NonStaticInnerClassTarget.Inner.class, NonStaticInnerClassTarget.Inner.Deeper.class);
        AvmClassLoader loader = avm.getClassLoader();
        
        Class<?> clazz = loader.loadUserClassByOriginalName(NonStaticInnerClassTarget.class.getName(), this.preserveDebuggability);
        Assert.assertNotNull(clazz);
        Object outerInstance = clazz.getConstructor().newInstance();
        Assert.assertNotNull(outerInstance);
        
        Class<?> inner = loader.loadUserClassByOriginalName(NonStaticInnerClassTarget.Inner.class.getName(), this.preserveDebuggability);
        Assert.assertNotNull(inner);
        Object inner1 = inner.getConstructor(clazz, inner).newInstance(outerInstance, null);
        Assert.assertNotNull(inner1);
        
        Class<?> deeper = loader.loadUserClassByOriginalName(NonStaticInnerClassTarget.Inner.Deeper.class.getName(), this.preserveDebuggability);
        Assert.assertNotNull(deeper);
        Object deeper1 = deeper.getConstructor(inner).newInstance(inner1);
        Assert.assertNotNull(deeper1);
        avm.shutdown();
    }

    @Test
    public void testSetOnParameters() throws Exception {
        SimpleAvm avm = new SimpleAvm(10000L, this.preserveDebuggability, NonStaticInnerClassTarget.class, NonStaticInnerClassTarget.Inner.class, NonStaticInnerClassTarget.Inner.Deeper.class);
        AvmClassLoader loader = avm.getClassLoader();
        
        Class<?> clazz = loader.loadUserClassByOriginalName(NonStaticInnerClassTarget.class.getName(), this.preserveDebuggability);
        Assert.assertNotNull(clazz);
        Object outerInstance = clazz.getConstructor().newInstance();
        Assert.assertNotNull(outerInstance);
        
        Class<?> inner = loader.loadUserClassByOriginalName(NonStaticInnerClassTarget.Inner.class.getName(), this.preserveDebuggability);
        Assert.assertNotNull(inner);
        Object inner1 = inner.getConstructor(clazz, inner).newInstance(outerInstance, null);
        Assert.assertNotNull(inner1);
        
        Class<?> deeper = loader.loadUserClassByOriginalName(NonStaticInnerClassTarget.Inner.Deeper.class.getName(), this.preserveDebuggability);
        Assert.assertNotNull(deeper);
        Method readParent = deeper.getMethod(NamespaceMapper.mapMethodName("readParent"));
        
        // Initially, the parent is still 0 until we use it to create another instance.
        Object deeper1 = deeper.getConstructor(inner).newInstance(inner1);
        Assert.assertNotNull(deeper1);
        Assert.assertEquals(0L, readParent.invoke(deeper1));
        
        // Now, the creation of inner2 should have set inner1 to 42.
        Object inner2 = inner.getConstructor(clazz, inner).newInstance(outerInstance, inner1);
        Assert.assertNotNull(inner2);
        Assert.assertEquals(42L, readParent.invoke(deeper1));
        avm.shutdown();
    }
}
