package org.aion.avm.core;

import org.aion.types.Address;
import org.junit.Assert;
import org.junit.Test;


/**
 * Basic tests of ReentrantDAppStack, just to demonstrate what it does and how it works.
 */
public class ReentrantDAppStackTest {
    @Test
    public void commonPushAndPop() throws Exception {
        ReentrantDAppStack stack = new ReentrantDAppStack();
        ReentrantDAppStack.ReentrantState state1 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x1), null, 1, null);
        ReentrantDAppStack.ReentrantState state2 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x2), null, 1, null);
        ReentrantDAppStack.ReentrantState state3 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x3), null, 1, null);
        
        stack.pushState(state1);
        stack.pushState(state2);
        stack.pushState(state3);
        Assert.assertEquals(state3, stack.popState());
        Assert.assertEquals(state2, stack.popState());
        Assert.assertEquals(state1, stack.popState());
    }

    @Test
    public void popEmptyAsNull() throws Exception {
        ReentrantDAppStack stack = new ReentrantDAppStack();
        ReentrantDAppStack.ReentrantState state1 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x1), null, 1, null);
        
        stack.pushState(state1);
        Assert.assertEquals(state1, stack.popState());
        Assert.assertNull(stack.popState());
    }

    @Test
    public void basicSearch() throws Exception {
        ReentrantDAppStack stack = new ReentrantDAppStack();
        ReentrantDAppStack.ReentrantState state1 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x1), null, 1, null);
        ReentrantDAppStack.ReentrantState state2 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x2), null, 1, null);
        ReentrantDAppStack.ReentrantState state3 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x3), null, 1, null);
        
        Assert.assertNull(stack.tryShareState(getNewAddress(0x1)));
        
        stack.pushState(state1);
        stack.pushState(state2);
        stack.pushState(state3);
        Assert.assertEquals(state1, stack.tryShareState(getNewAddress(0x1)));
        Assert.assertEquals(state1, stack.tryShareState(getNewAddress(0x1)));
        Assert.assertEquals(state3, stack.tryShareState(getNewAddress(0x3)));
        
        Assert.assertEquals(state3, stack.popState());
        Assert.assertEquals(state2, stack.popState());
        Assert.assertEquals(state1, stack.tryShareState(getNewAddress(0x1)));
        Assert.assertNull(stack.tryShareState(getNewAddress(0x3)));
        
        Assert.assertEquals(state1, stack.popState());
        Assert.assertNull(stack.tryShareState(getNewAddress(0x1)));
    }

    @Test
    public void shadowedSearch() throws Exception {
        ReentrantDAppStack stack = new ReentrantDAppStack();
        ReentrantDAppStack.ReentrantState state1 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x1), null, 1, null);
        ReentrantDAppStack.ReentrantState state2 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x2), null, 1, null);
        ReentrantDAppStack.ReentrantState state3 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x3), null, 1, null);
        ReentrantDAppStack.ReentrantState state1_again = new ReentrantDAppStack.ReentrantState(getNewAddress(0x1), null, 1, null);
        
        stack.pushState(state1);
        stack.pushState(state2);
        stack.pushState(state3);
        Assert.assertEquals(state1, stack.tryShareState(getNewAddress(0x1)));
        
        // Push this reentered state and see that it appears first, shadowing the other version.
        stack.pushState(state1_again);
        Assert.assertEquals(state1_again, stack.tryShareState(getNewAddress(0x1)));
        Assert.assertEquals(state3, stack.tryShareState(getNewAddress(0x3)));
        
        Assert.assertEquals(state1_again, stack.popState());
        Assert.assertEquals(state3, stack.popState());
        Assert.assertEquals(state2, stack.popState());
        Assert.assertEquals(state1, stack.tryShareState(getNewAddress(0x1)));
        Assert.assertNull(stack.tryShareState(getNewAddress(0x3)));
        
        Assert.assertEquals(state1, stack.popState());
        Assert.assertNull(stack.tryShareState(getNewAddress(0x1)));
    }


    private static Address getNewAddress(int leadingByte) {
        byte[] address = new byte[Address.SIZE];
        address[0] = (byte) leadingByte;
        return Address.wrap(address);
    }
}
