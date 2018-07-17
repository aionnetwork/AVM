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
        long initialInstanceId = 1l;
        long nextInstanceId = RootClassCodec.saveClassStaticsToStorage(RootClassCodecTest.class.getClassLoader(), initialInstanceId, kernel, address, Arrays.asList(ReflectionStructureCodecTarget.class, RootClassCodecTarget.class));
        // Note that this attempt to serialize has no instances so the counter should be unchanged.
        Assert.assertEquals(initialInstanceId, nextInstanceId);
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

    /**
     * Serialize a single class which references an instance to verify correct shape but also correct nextInstanceId accounting.
     */
    @Test
    public void serializeClassWithInstance() {
        ReflectionStructureCodecTarget.s_one = true;
        ReflectionStructureCodecTarget.s_two = 5;
        ReflectionStructureCodecTarget.s_three = 5;
        ReflectionStructureCodecTarget.s_four = 5;
        ReflectionStructureCodecTarget.s_five = 5;
        ReflectionStructureCodecTarget.s_six = 5.0f;
        ReflectionStructureCodecTarget.s_seven = 5;
        ReflectionStructureCodecTarget.s_eight = 5.0d;
        ReflectionStructureCodecTarget.s_nine = new ReflectionStructureCodecTarget();
        
        KernelApiImpl kernel = new KernelApiImpl();
        byte[] address = new byte[] {1,2,3};
        long initialInstanceId = 1l;
        long nextInstanceId = RootClassCodec.saveClassStaticsToStorage(RootClassCodecTest.class.getClassLoader(), initialInstanceId, kernel, address, Arrays.asList(ReflectionStructureCodecTarget.class));
        // We serialized a single instance so we expect the nextInstanceId to be advanced.
        Assert.assertEquals(1 + initialInstanceId, nextInstanceId);
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
                //s_nine:
                0x0, 0x0, 0x0, 0x3c, //s_nine (class name length)
                0x6f, 0x72, 0x67, 0x2e, 0x61, 0x69, 0x6f, 0x6e, 0x2e, 0x61, 0x76, 0x6d, 0x2e, 0x63, 0x6f, 0x72, 0x65, 0x2e, 0x70, 0x65, 0x72, 0x73, 0x69, 0x73, 0x74, 0x65, 0x6e, 0x63, 0x65, 0x2e, 0x52, 0x65, 0x66, 0x6c, 0x65, 0x63, 0x74, 0x69, 0x6f, 0x6e, 0x53, 0x74, 0x72, 0x75, 0x63, 0x74, 0x75, 0x72, 0x65, 0x43, 0x6f, 0x64, 0x65, 0x63, 0x54, 0x61, 0x72, 0x67, 0x65, 0x74, //s_nine (class name UTF8)
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x1, //s_nine (instanceId)
        };
        Assert.assertTrue(Arrays.equals(expected, result));
    }
}
