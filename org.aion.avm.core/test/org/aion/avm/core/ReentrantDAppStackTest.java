package org.aion.avm.core;

import org.aion.avm.core.persistence.ISuspendableInstanceLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.AvmAddress;
import org.aion.vm.api.interfaces.Address;
import org.junit.Assert;
import org.junit.Test;


/**
 * Basic tests of ReentrantDAppStack, just to demonstrate what it does and how it works.
 */
public class ReentrantDAppStackTest {
    @Test
    public void commonPushAndPop() throws Exception {
        ReentrantDAppStack stack = new ReentrantDAppStack();
        ReentrantDAppStack.ReentrantState state1 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x1), null, null);
        ReentrantDAppStack.ReentrantState state2 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x2), null, null);
        ReentrantDAppStack.ReentrantState state3 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x3), null, null);
        FlagInstanceLoader flag1 = new FlagInstanceLoader();
        FlagInstanceLoader flag2 = new FlagInstanceLoader();
        FlagInstanceLoader flag3 = new FlagInstanceLoader();
        state1.setInstanceLoader(flag1);
        state2.setInstanceLoader(flag2);
        state3.setInstanceLoader(flag3);
        
        stack.pushState(state1);
        stack.pushState(state2);
        stack.pushState(state3);
        Assert.assertFalse(flag1.flag);
        Assert.assertFalse(flag2.flag);
        Assert.assertTrue(flag3.flag);
        Assert.assertEquals(state3, stack.popState());
        Assert.assertTrue(flag2.flag);
        Assert.assertEquals(state2, stack.popState());
        Assert.assertTrue(flag1.flag);
        Assert.assertEquals(state1, stack.popState());
    }

    @Test
    public void popEmptyAsNull() throws Exception {
        ReentrantDAppStack stack = new ReentrantDAppStack();
        ReentrantDAppStack.ReentrantState state1 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x1), null, null);
        
        stack.pushState(state1);
        Assert.assertEquals(state1, stack.popState());
        Assert.assertNull(stack.popState());
    }

    @Test
    public void basicSearch() throws Exception {
        ReentrantDAppStack stack = new ReentrantDAppStack();
        ReentrantDAppStack.ReentrantState state1 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x1), null, null);
        ReentrantDAppStack.ReentrantState state2 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x2), null, null);
        ReentrantDAppStack.ReentrantState state3 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x3), null, null);
        state1.setInstanceLoader(new FlagInstanceLoader());
        state2.setInstanceLoader(new FlagInstanceLoader());
        state3.setInstanceLoader(new FlagInstanceLoader());
        
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
        ReentrantDAppStack.ReentrantState state1 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x1), null, null);
        ReentrantDAppStack.ReentrantState state2 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x2), null, null);
        ReentrantDAppStack.ReentrantState state3 = new ReentrantDAppStack.ReentrantState(getNewAddress(0x3), null, null);
        ReentrantDAppStack.ReentrantState state1_again = new ReentrantDAppStack.ReentrantState(getNewAddress(0x1), null, null);
        state1.setInstanceLoader(new FlagInstanceLoader());
        state2.setInstanceLoader(new FlagInstanceLoader());
        state3.setInstanceLoader(new FlagInstanceLoader());
        state1_again.setInstanceLoader(new FlagInstanceLoader());
        
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


    private static class FlagInstanceLoader implements ISuspendableInstanceLoader {
        public boolean flag = true;
        
        @Override
        public void loaderDidBecomeActive() {
            Assert.assertFalse(this.flag);
            this.flag = true;
        }
        @Override
        public void loaderDidBecomeInactive() {
            Assert.assertTrue(this.flag);
            this.flag = false;
        }
    }
    
    private static Address getNewAddress(int leadingByte) {
        byte[] address = new byte[Address.SIZE];
        address[0] = (byte) leadingByte;
        return AvmAddress.wrap(address);
    }
}
