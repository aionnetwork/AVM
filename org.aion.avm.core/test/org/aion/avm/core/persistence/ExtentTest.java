package org.aion.avm.core.persistence;

import org.junit.Assert;
import org.junit.Test;


public class ExtentTest {
    @Test
    public void testEmpty() {
        Extent extent1 = new Extent(new byte[0], new INode[0]);
        Extent extent2 = new Extent(new byte[0], new INode[0]);
        
        Assert.assertEquals(extent1.hashCode(), extent2.hashCode());
        Assert.assertEquals(extent1, extent2);
    }

    @Test
    public void testMatchWithBoth() {
        INode instance = new ConstantNode(1L);
        Extent extent1 = new Extent(new byte[] {0x1, 0x2}, new INode[] {instance});
        Extent extent2 = new Extent(new byte[] {0x1, 0x2}, new INode[] {instance});
        
        Assert.assertEquals(extent1.hashCode(), extent2.hashCode());
        Assert.assertEquals(extent1, extent2);
    }

    @Test
    public void testFailDifferentPrimitiveSize() {
        INode instance = new ConstantNode(1L);
        Extent extent1 = new Extent(new byte[] {0x1, 0x2}, new INode[] {instance});
        Extent extent2 = new Extent(new byte[] {0x1, 0x2, 0x3}, new INode[] {instance});
        
        Assert.assertNotEquals(extent1.hashCode(), extent2.hashCode());
        Assert.assertNotEquals(extent1, extent2);
    }

    @Test
    public void testFailDifferentPrimitiveData() {
        INode instance = new ConstantNode(1L);
        Extent extent1 = new Extent(new byte[] {0x1, 0x2}, new INode[] {instance});
        Extent extent2 = new Extent(new byte[] {0x1, 0x3}, new INode[] {instance});
        
        Assert.assertNotEquals(extent1.hashCode(), extent2.hashCode());
        Assert.assertNotEquals(extent1, extent2);
    }

    @Test
    public void testFailDifferentReferenceSize() {
        INode instance1 = new ConstantNode(1L);
        INode instance2 = new ConstantNode(2L);
        Extent extent1 = new Extent(new byte[] {0x1, 0x2}, new INode[] {instance1});
        Extent extent2 = new Extent(new byte[] {0x1, 0x2}, new INode[] {instance1, instance2});
        
        Assert.assertNotEquals(extent1.hashCode(), extent2.hashCode());
        Assert.assertNotEquals(extent1, extent2);
    }

    @Test
    public void testFailDifferentReferenceData() {
        INode instance1 = new ConstantNode(1L);
        INode instance2 = new ConstantNode(2L);
        Extent extent1 = new Extent(new byte[] {0x1, 0x2}, new INode[] {instance1, null});
        Extent extent2 = new Extent(new byte[] {0x1, 0x2}, new INode[] {instance1, instance2});
        
        Assert.assertNotEquals(extent1.hashCode(), extent2.hashCode());
        Assert.assertNotEquals(extent1, extent2);
    }

    @Test
    public void testToString() {
        INode instance = new ConstantNode(1L);
        Extent extent = new Extent(new byte[] {0x1, 0x2}, new INode[] {instance});
        
        Assert.assertEquals("Extent(1 references, 2 primitive bytes)", extent.toString());
    }
}
