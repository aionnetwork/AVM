package org.aion.avm.core.persistence;

import java.util.Arrays;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.internal.Helper;
import org.aion.kernel.KernelApiImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class RootClassCodecTest {
    @Before
    public void setup() {
        // Force the initialization of the NodeEnvironment singleton.
        Assert.assertNotNull(NodeEnvironment.singleton);
        
        new Helper(ReflectionStructureCodecTarget.class.getClassLoader(), 1_000_000L, 1);
        // Clear statics, since our tests interact with them.
        ReflectionStructureCodecTarget.s_one = false;
        ReflectionStructureCodecTarget.s_two = 0;
        ReflectionStructureCodecTarget.s_three = 0;
        ReflectionStructureCodecTarget.s_four = 0;
        ReflectionStructureCodecTarget.s_five = 0;
        ReflectionStructureCodecTarget.s_six = 0.0f;
        ReflectionStructureCodecTarget.s_seven = 0;
        ReflectionStructureCodecTarget.s_eight = 0.0d;
        ReflectionStructureCodecTarget.s_nine = null;
        RootClassCodecTarget.s_one = false;
        RootClassCodecTarget.s_two = 0;
        RootClassCodecTarget.s_three = 0;
        RootClassCodecTarget.s_four = 0;
        RootClassCodecTarget.s_five = 0;
        RootClassCodecTarget.s_six = 0.0f;
        RootClassCodecTarget.s_seven = 0;
        RootClassCodecTarget.s_eight = 0.0d;
    }

    @After
    public void tearDown() {
        Helper.clearTestingState();
    }

    /**
     * Populate 2 target classes and make sure they serialize correctly.
     */
    @Test
    public void serializeClasses() {
        ReflectionStructureCodecTarget.s_one = true;
        ReflectionStructureCodecTarget.s_two = 5;
        ReflectionStructureCodecTarget.s_three = 5;
        ReflectionStructureCodecTarget.s_four = 5;
        ReflectionStructureCodecTarget.s_five = 5;
        ReflectionStructureCodecTarget.s_six = 5.0f;
        ReflectionStructureCodecTarget.s_seven = 5;
        ReflectionStructureCodecTarget.s_eight = 5.0d;
        RootClassCodecTarget.s_one = true;
        RootClassCodecTarget.s_two = 5;
        RootClassCodecTarget.s_three = 5;
        RootClassCodecTarget.s_four = 5;
        RootClassCodecTarget.s_five = 5;
        RootClassCodecTarget.s_six = 5.0f;
        RootClassCodecTarget.s_seven = 5;
        RootClassCodecTarget.s_eight = 5.0d;
        
        KernelApiImpl kernel = new KernelApiImpl();
        byte[] address = new byte[] {1,2,3};
        RootClassCodec.saveClassStaticsToStorage(RootClassCodecTest.class.getClassLoader(), 1, kernel, address, Arrays.asList(ReflectionStructureCodecTarget.class, RootClassCodecTarget.class));
        byte[] result = kernel.getStorage(address, StorageKeys.CLASS_STATICS);
        // These are encoded in-order.  Some are obvious but we will explicitly decode the stub structure since it is harder to verify.
        byte[] expected = {
                // ReflectionStructureCodecTarget
                0x1, //s_one
                0x5, //s_two
                0x0, 0x5, //s_three
                0x0, 0x5, //s_four
                0x0, 0x0, 0x0, 0x5, //s_five
                0x40, (byte)0xa0, 0x0, 0x0, //s_six
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x5, //s_seven
                0x40, 0x14, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, //s_eight
                0x0, 0x0, 0x0, 0x0, //s_nine
                // RootClassCodecTarget
                0x1, //s_one
                0x5, //s_two
                0x0, 0x5, //s_three
                0x0, 0x5, //s_four
                0x0, 0x0, 0x0, 0x5, //s_five
                0x40, (byte)0xa0, 0x0, 0x0, //s_six
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x5, //s_seven
                0x40, 0x14, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, //s_eight
        };
        Assert.assertTrue(Arrays.equals(expected, result));
    }

    /**
     * Load the state of 2 classes and make sure that they deserialized as expected.
     */
    @Test
    public void deserializeClasses() {
        // These are encoded in-order.  Some are obvious but we will explicitly decode the stub structure since it is harder to verify.
        byte[] expected = {
                // ReflectionStructureCodecTarget
                0x1, //s_one
                0x5, //s_two
                0x0, 0x5, //s_three
                0x0, 0x5, //s_four
                0x0, 0x0, 0x0, 0x5, //s_five
                0x40, (byte)0xa0, 0x0, 0x0, //s_six
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x5, //s_seven
                0x40, 0x14, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, //s_eight
                0x0, 0x0, 0x0, 0x0, //s_nine
                // RootClassCodecTarget
                0x1, //s_one
                0x5, //s_two
                0x0, 0x5, //s_three
                0x0, 0x5, //s_four
                0x0, 0x0, 0x0, 0x5, //s_five
                0x40, (byte)0xa0, 0x0, 0x0, //s_six
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x5, //s_seven
                0x40, 0x14, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, //s_eight
        };
        KernelApiImpl kernel = new KernelApiImpl();
        byte[] address = new byte[] {1,2,3};
        kernel.putStorage(address, StorageKeys.CLASS_STATICS, expected);
        
        // Populate the classes.
        RootClassCodec.populateClassStaticsFromStorage(RootClassCodecTest.class.getClassLoader(), kernel, address, Arrays.asList(ReflectionStructureCodecTarget.class, RootClassCodecTarget.class));
        
        // Verify that their static are as we expect.
        Assert.assertEquals(true, ReflectionStructureCodecTarget.s_one);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_two);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_three);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_four);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_five);
        Assert.assertEquals(5.0f, ReflectionStructureCodecTarget.s_six, 0.01f);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_seven);
        Assert.assertEquals(5.0d, ReflectionStructureCodecTarget.s_eight, 0.01f);
        Assert.assertEquals(true, RootClassCodecTarget.s_one);
        Assert.assertEquals(5, RootClassCodecTarget.s_two);
        Assert.assertEquals(5, RootClassCodecTarget.s_three);
        Assert.assertEquals(5, RootClassCodecTarget.s_four);
        Assert.assertEquals(5, RootClassCodecTarget.s_five);
        Assert.assertEquals(5.0f, RootClassCodecTarget.s_six, 0.01f);
        Assert.assertEquals(5, RootClassCodecTarget.s_seven);
        Assert.assertEquals(5.0d, RootClassCodecTarget.s_eight, 0.01f);
    }
}
