package org.aion.avm.core.persistence;

import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class AutomaticGraphVisitorTest {
    private Class<?> primaryClass;
    private Class<?> secondaryClass;

    @Before
    public void setup() throws Exception {
        SimpleAvm avm = new SimpleAvm(1_000_000L, AutomaticGraphVisitorTargetPrimary.class, AutomaticGraphVisitorTargetSecondary.class);
        AvmClassLoader loader = avm.getClassLoader();
        
        this.primaryClass = loader.loadUserClassByOriginalName(AutomaticGraphVisitorTargetPrimary.class.getName());
        this.secondaryClass = loader.loadUserClassByOriginalName(AutomaticGraphVisitorTargetSecondary.class.getName());
    }

    @Test
    public void createPrimary() throws Exception {
        Object primary = this.primaryClass.getConstructor().newInstance();
        Assert.assertEquals(42, this.primaryClass.getDeclaredField("avm_value").getInt(primary));
    }

    @Test
    public void createSecondaryDirect() throws Exception {
        Object secondary = this.secondaryClass.getConstructor(int.class).newInstance(5);
        Assert.assertEquals(5, this.secondaryClass.getDeclaredField("avm_value").getInt(secondary));
        this.secondaryClass.getMethod("avm_setValue", int.class).invoke(secondary, 6);
        Assert.assertEquals(6, this.secondaryClass.getDeclaredField("avm_value").getInt(secondary));
    }

    @Test
    public void createSecondaryThroughPrimary() throws Exception {
        Object secondary = this.primaryClass.getMethod("avm_createSecondary", int.class, int.class).invoke(null, 5, 6);
        Assert.assertEquals(6, this.secondaryClass.getDeclaredField("avm_value").getInt(secondary));
        this.primaryClass.getMethod("avm_changeAgain", this.secondaryClass, int.class).invoke(null, secondary, 7);
        Assert.assertEquals(7, this.secondaryClass.getDeclaredField("avm_value").getInt(secondary));
    }

    @Test
    public void createSecondaryEmptyConstructor() throws Exception {
        Object secondary = this.secondaryClass.getConstructor().newInstance();
        Assert.assertEquals(0, this.secondaryClass.getDeclaredField("avm_value").getInt(secondary));
        this.secondaryClass.getMethod("avm_setValue", int.class).invoke(secondary, 1);
        Assert.assertEquals(1, this.secondaryClass.getDeclaredField("avm_value").getInt(secondary));
    }
}
