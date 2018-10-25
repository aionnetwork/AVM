package org.aion.avm.core.persistence;

import org.junit.Assert;
import org.junit.Test;


public class SerializedRepresentationTest {
    @Test
    public void testEmpty() {
        SerializedRepresentation extent1 = new SerializedRepresentation(new byte[0], new INode[0]);
        SerializedRepresentation extent2 = new SerializedRepresentation(new byte[0], new INode[0]);
        
        Assert.assertEquals(extent1.hashCode(), extent2.hashCode());
        Assert.assertEquals(extent1, extent2);
    }

    @Test
    public void testMatchWithBoth() {
        INode instance = new ConstantNode(1L);
        SerializedRepresentation extent1 = new SerializedRepresentation(new byte[] {0x1, 0x2}, new INode[] {instance});
        SerializedRepresentation extent2 = new SerializedRepresentation(new byte[] {0x1, 0x2}, new INode[] {instance});
        
        Assert.assertEquals(extent1.hashCode(), extent2.hashCode());
        Assert.assertEquals(extent1, extent2);
    }

    @Test
    public void testFailDifferentPrimitiveSize() {
        INode instance = new ConstantNode(1L);
        SerializedRepresentation extent1 = new SerializedRepresentation(new byte[] {0x1, 0x2}, new INode[] {instance});
        SerializedRepresentation extent2 = new SerializedRepresentation(new byte[] {0x1, 0x2, 0x3}, new INode[] {instance});
        
        Assert.assertNotEquals(extent1.hashCode(), extent2.hashCode());
        Assert.assertNotEquals(extent1, extent2);
    }

    @Test
    public void testFailDifferentPrimitiveData() {
        INode instance = new ConstantNode(1L);
        SerializedRepresentation extent1 = new SerializedRepresentation(new byte[] {0x1, 0x2}, new INode[] {instance});
        SerializedRepresentation extent2 = new SerializedRepresentation(new byte[] {0x1, 0x3}, new INode[] {instance});
        
        Assert.assertNotEquals(extent1.hashCode(), extent2.hashCode());
        Assert.assertNotEquals(extent1, extent2);
    }

    @Test
    public void testFailDifferentReferenceSize() {
        INode instance1 = new ConstantNode(1L);
        INode instance2 = new ConstantNode(2L);
        SerializedRepresentation extent1 = new SerializedRepresentation(new byte[] {0x1, 0x2}, new INode[] {instance1});
        SerializedRepresentation extent2 = new SerializedRepresentation(new byte[] {0x1, 0x2}, new INode[] {instance1, instance2});
        
        Assert.assertNotEquals(extent1.hashCode(), extent2.hashCode());
        Assert.assertNotEquals(extent1, extent2);
    }

    @Test
    public void testFailDifferentReferenceData() {
        INode instance1 = new ConstantNode(1L);
        INode instance2 = new ConstantNode(2L);
        SerializedRepresentation extent1 = new SerializedRepresentation(new byte[] {0x1, 0x2}, new INode[] {instance1, null});
        SerializedRepresentation extent2 = new SerializedRepresentation(new byte[] {0x1, 0x2}, new INode[] {instance1, instance2});
        
        Assert.assertNotEquals(extent1.hashCode(), extent2.hashCode());
        Assert.assertNotEquals(extent1, extent2);
    }

    @Test
    public void testToString() {
        INode instance = new ConstantNode(1L);
        SerializedRepresentation extent = new SerializedRepresentation(new byte[] {0x1, 0x2}, new INode[] {instance});
        
        Assert.assertEquals("SerializedRepresentation(1 references, 2 primitive bytes)", extent.toString());
    }
}
