package org.aion.avm.core.persistence;

import java.util.Arrays;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.util.NullFeeProcessor;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.IHelper;
import org.aion.kernel.KernelInterfaceImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class LoadedDAppTest {
    // We don't verify fees at this time so just use the "null" utility processor.
    private static NullFeeProcessor FEE_PROCESSOR = new NullFeeProcessor();

    @Before
    public void setup() {
        // Force the initialization of the NodeEnvironment singleton.
        Assert.assertNotNull(NodeEnvironment.singleton);
        
        new Helper(ReflectionStructureCodecTarget.class.getClassLoader(), 1_000_000L, 1);
        // Clear statics, since our tests interact with them.
        clearStaticState();
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
        LoadedDAppTarget.s_one = true;
        LoadedDAppTarget.s_two = 5;
        LoadedDAppTarget.s_three = 5;
        LoadedDAppTarget.s_four = 5;
        LoadedDAppTarget.s_five = 5;
        LoadedDAppTarget.s_six = 5.0f;
        LoadedDAppTarget.s_seven = 5;
        LoadedDAppTarget.s_eight = 5.0d;

        KernelInterfaceImpl kernel = new KernelInterfaceImpl();
        byte[] address = new byte[] {1,2,3};
        long initialInstanceId = 1l;
        long nextInstanceId = new LoadedDApp(null, address, Arrays.asList(ReflectionStructureCodecTarget.class, LoadedDAppTarget.class), ReflectionStructureCodecTarget.class.getName()).saveClassStaticsToStorage(initialInstanceId, FEE_PROCESSOR, kernel);
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
                // LoadedDAppTarget
                0x1, //s_one
                0x5, //s_two
                0x0, 0x5, //s_three
                0x0, 0x5, //s_four
                0x0, 0x0, 0x0, 0x5, //s_five
                0x40, (byte)0xa0, 0x0, 0x0, //s_six
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x5, //s_seven
                0x40, 0x14, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, //s_eight
                0x0, 0x0, 0x0, 0x0, //s_nine (null)
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
                // LoadedDAppTarget
                0x1, //s_one
                0x5, //s_two
                0x0, 0x5, //s_three
                0x0, 0x5, //s_four
                0x0, 0x0, 0x0, 0x5, //s_five
                0x40, (byte)0xa0, 0x0, 0x0, //s_six
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x5, //s_seven
                0x40, 0x14, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, //s_eight
                0x0, 0x0, 0x0, 0x0, //s_nine (null)
        };
        KernelInterfaceImpl kernel = new KernelInterfaceImpl();
        byte[] address = new byte[] {1,2,3};
        kernel.putStorage(address, StorageKeys.CLASS_STATICS, expected);
        
        // Populate the classes.
        new LoadedDApp(LoadedDAppTest.class.getClassLoader(), address, Arrays.asList(ReflectionStructureCodecTarget.class, LoadedDAppTarget.class), ReflectionStructureCodecTarget.class.getName()).populateClassStaticsFromStorage(FEE_PROCESSOR, kernel);
        
        // Verify that their static are as we expect.
        Assert.assertEquals(true, ReflectionStructureCodecTarget.s_one);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_two);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_three);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_four);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_five);
        Assert.assertEquals(5.0f, ReflectionStructureCodecTarget.s_six, 0.01f);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_seven);
        Assert.assertEquals(5.0d, ReflectionStructureCodecTarget.s_eight, 0.01f);
        Assert.assertEquals(true, LoadedDAppTarget.s_one);
        Assert.assertEquals(5, LoadedDAppTarget.s_two);
        Assert.assertEquals(5, LoadedDAppTarget.s_three);
        Assert.assertEquals(5, LoadedDAppTarget.s_four);
        Assert.assertEquals(5, LoadedDAppTarget.s_five);
        Assert.assertEquals(5.0f, LoadedDAppTarget.s_six, 0.01f);
        Assert.assertEquals(5, LoadedDAppTarget.s_seven);
        Assert.assertEquals(5.0d, LoadedDAppTarget.s_eight, 0.01f);
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
        
        KernelInterfaceImpl kernel = new KernelInterfaceImpl();
        byte[] address = new byte[] {1,2,3};
        long initialInstanceId = 1l;
        long nextInstanceId = new LoadedDApp(null, address, Arrays.asList(ReflectionStructureCodecTarget.class), ReflectionStructureCodecTarget.class.getName()).saveClassStaticsToStorage(initialInstanceId, FEE_PROCESSOR, kernel);
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

    /**
     * Serialize and deserialize the target and sub-class and instance, to make sure that we handle the hierarchy properly.
     */
    @Test
    public void serializeDeserializeSubClassAndInstance() {
        ReflectionStructureCodecTarget.s_one = true;
        ReflectionStructureCodecTarget.s_two = 5;
        ReflectionStructureCodecTarget.s_three = 5;
        ReflectionStructureCodecTarget.s_four = 5;
        ReflectionStructureCodecTarget.s_five = 5;
        ReflectionStructureCodecTarget.s_six = 5.0f;
        ReflectionStructureCodecTarget.s_seven = 5;
        ReflectionStructureCodecTarget.s_eight = 5.0d;
        ReflectionStructureCodecTarget.s_nine = new ReflectionStructureCodecTarget();
        ReflectionStructureCodecTarget.s_nine.i_one = ReflectionStructureCodecTarget.s_one;
        ReflectionStructureCodecTarget.s_nine.i_two = ReflectionStructureCodecTarget.s_two;
        ReflectionStructureCodecTarget.s_nine.i_three = ReflectionStructureCodecTarget.s_three;
        ReflectionStructureCodecTarget.s_nine.i_four = ReflectionStructureCodecTarget.s_four;
        ReflectionStructureCodecTarget.s_nine.i_five = ReflectionStructureCodecTarget.s_five;
        ReflectionStructureCodecTarget.s_nine.i_six = ReflectionStructureCodecTarget.s_six;
        ReflectionStructureCodecTarget.s_nine.i_seven = ReflectionStructureCodecTarget.s_seven;
        ReflectionStructureCodecTarget.s_nine.i_eight = ReflectionStructureCodecTarget.s_eight;
        ReflectionStructureCodecTarget.s_nine.i_nine = ReflectionStructureCodecTarget.s_nine;
        
        ReflectionStructureCodecTargetSub.s_one = false;
        ReflectionStructureCodecTargetSub.s_two = 9;
        ReflectionStructureCodecTargetSub.s_three = 9;
        ReflectionStructureCodecTargetSub.s_four = 9;
        ReflectionStructureCodecTargetSub.s_five = 9;
        ReflectionStructureCodecTargetSub.s_six = 9.0f;
        ReflectionStructureCodecTargetSub.s_seven = 9;
        ReflectionStructureCodecTargetSub.s_eight = 9.0d;
        ReflectionStructureCodecTargetSub.s_nine = new ReflectionStructureCodecTargetSub();
        ReflectionStructureCodecTargetSub.s_nine.i_one = ReflectionStructureCodecTargetSub.s_one;
        ReflectionStructureCodecTargetSub.s_nine.i_two = ReflectionStructureCodecTargetSub.s_two;
        ReflectionStructureCodecTargetSub.s_nine.i_three = ReflectionStructureCodecTargetSub.s_three;
        ReflectionStructureCodecTargetSub.s_nine.i_four = ReflectionStructureCodecTargetSub.s_four;
        ReflectionStructureCodecTargetSub.s_nine.i_five = ReflectionStructureCodecTargetSub.s_five;
        ReflectionStructureCodecTargetSub.s_nine.i_six = ReflectionStructureCodecTargetSub.s_six;
        ReflectionStructureCodecTargetSub.s_nine.i_seven = ReflectionStructureCodecTargetSub.s_seven;
        ReflectionStructureCodecTargetSub.s_nine.i_eight = ReflectionStructureCodecTargetSub.s_eight;
        ReflectionStructureCodecTargetSub.s_nine.i_nine = ReflectionStructureCodecTargetSub.s_nine;
        // Verify writing to the superclass shape still works correctly.
        ((ReflectionStructureCodecTarget)ReflectionStructureCodecTargetSub.s_nine).i_five = 42;
        ((ReflectionStructureCodecTarget)ReflectionStructureCodecTargetSub.s_nine).i_nine = ReflectionStructureCodecTarget.s_nine;
        
        KernelInterfaceImpl kernel = new KernelInterfaceImpl();
        byte[] address = new byte[] {1,2,3};
        long initialInstanceId = 1l;
        LoadedDApp dapp = new LoadedDApp(LoadedDAppTest.class.getClassLoader(), address, Arrays.asList(ReflectionStructureCodecTarget.class, ReflectionStructureCodecTargetSub.class), ReflectionStructureCodecTarget.class.getName());
        long nextInstanceId = dapp.saveClassStaticsToStorage(initialInstanceId, FEE_PROCESSOR, kernel);
        // We serialized 2 instances so we expect the nextInstanceId to be advanced.
        Assert.assertEquals(2 + initialInstanceId, nextInstanceId);
        // Check the size of the saved static data (should only store local copies of statics, not superclass statics, per class).
        byte[] result = kernel.getStorage(address, StorageKeys.CLASS_STATICS);
        // (note that this is "309" if the superclass static fields are redundantly stored in sub-classes).
        Assert.assertEquals(207, result.length);
        
        // Now, clear the class states and reload this.
        clearStaticState();
        dapp.populateClassStaticsFromStorage(FEE_PROCESSOR, kernel);
        
        // Verify that their static are as we expect.
        Assert.assertEquals(true, ReflectionStructureCodecTarget.s_one);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_two);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_three);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_four);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_five);
        Assert.assertEquals(5.0f, ReflectionStructureCodecTarget.s_six, 0.01f);
        Assert.assertEquals(5, ReflectionStructureCodecTarget.s_seven);
        Assert.assertEquals(5.0d, ReflectionStructureCodecTarget.s_eight, 0.01f);
        Assert.assertEquals(false, ReflectionStructureCodecTargetSub.s_one);
        Assert.assertEquals(9, ReflectionStructureCodecTargetSub.s_two);
        Assert.assertEquals(9, ReflectionStructureCodecTargetSub.s_three);
        Assert.assertEquals(9, ReflectionStructureCodecTargetSub.s_four);
        Assert.assertEquals(9, ReflectionStructureCodecTargetSub.s_five);
        Assert.assertEquals(9.0f, ReflectionStructureCodecTargetSub.s_six, 0.01f);
        Assert.assertEquals(9, ReflectionStructureCodecTargetSub.s_seven);
        Assert.assertEquals(9.0d, ReflectionStructureCodecTargetSub.s_eight, 0.01f);
        
        // Verify that the instances are as we expect.
        ReflectionStructureCodecTarget parent = ReflectionStructureCodecTarget.s_nine;
        parent.lazyLoad();
        ReflectionStructureCodecTargetSub sub = ReflectionStructureCodecTargetSub.s_nine;
        sub.lazyLoad();
        Assert.assertEquals(true, parent.i_one);
        Assert.assertEquals(5, parent.i_two);
        Assert.assertEquals(5, parent.i_three);
        Assert.assertEquals(5, parent.i_four);
        Assert.assertEquals(5, parent.i_five);
        Assert.assertEquals(5.0f, parent.i_six, 0.01f);
        Assert.assertEquals(5, parent.i_seven);
        Assert.assertEquals(5.0d, parent.i_eight, 0.01f);
        Assert.assertTrue(parent == parent.i_nine);
        Assert.assertEquals(false, sub.i_one);
        Assert.assertEquals(9, sub.i_two);
        Assert.assertEquals(9, sub.i_three);
        Assert.assertEquals(9, sub.i_four);
        Assert.assertEquals(9, sub.i_five);
        Assert.assertEquals(9.0f, sub.i_six, 0.01f);
        Assert.assertEquals(9, sub.i_seven);
        Assert.assertEquals(9.0d, sub.i_eight, 0.01f);
        Assert.assertTrue(sub == sub.i_nine);
        Assert.assertEquals(42, ((ReflectionStructureCodecTarget)ReflectionStructureCodecTargetSub.s_nine).i_five);
        Assert.assertTrue(ReflectionStructureCodecTarget.s_nine == ((ReflectionStructureCodecTarget)ReflectionStructureCodecTargetSub.s_nine).i_nine);
        
        // Re-serialize these to prove that the instanceId doesn't increment (since we aren't adding any new objects to the graph).
        long finalInstanceId = dapp.saveClassStaticsToStorage(nextInstanceId, FEE_PROCESSOR, kernel);
        // We serialized 2 instances so we expect the nextInstanceId to be advanced.
        Assert.assertEquals(nextInstanceId, finalInstanceId);
    }

    /**
     * Populate one of the classes with a reference to one of our JDK constants and demonstrate that the reference is to the same instance, on deserialization.
     */
    @Test
    public void serializeDeserializeReferenceToJdkConstant() {
        LoadedDAppTarget.s_nine = org.aion.avm.shadow.java.math.RoundingMode.avm_HALF_EVEN;
        
        KernelInterfaceImpl kernel = new KernelInterfaceImpl();
        byte[] address = new byte[] {1,2,3};
        long initialInstanceId = 1l;
        LoadedDApp dapp = new LoadedDApp(LoadedDAppTest.class.getClassLoader(), address, Arrays.asList(LoadedDAppTarget.class), LoadedDAppTarget.class.getName());
        long nextInstanceId = dapp.saveClassStaticsToStorage(initialInstanceId, FEE_PROCESSOR, kernel);
        // Note that this attempt to serialize has no instances so the counter should be unchanged.
        Assert.assertEquals(initialInstanceId, nextInstanceId);
        byte[] result = kernel.getStorage(address, StorageKeys.CLASS_STATICS);
        // These are encoded in-order.  Some are obvious but we will explicitly decode the stub structure since it is harder to verify.
        byte[] expected = {
                // LoadedDAppTarget
                0x0, //s_one
                0x0, //s_two
                0x0, 0x0, //s_three
                0x0, 0x0, //s_four
                0x0, 0x0, 0x0, 0x0, //s_five
                0x0, 0x0, 0x0, 0x0, //s_six
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, //s_seven
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, //s_eight
                (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, //s_nine (-1 since this is a constant)
                (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xf0,  //s_nine (constant instanceId is negative)
        };
        Assert.assertTrue(Arrays.equals(expected, result));
        
        // Now, clear the statics, deserialize this, and ensure that we are still pointing at the same constant.
        clearStaticState();
        dapp.populateClassStaticsFromStorage(FEE_PROCESSOR, kernel);
        Assert.assertTrue(org.aion.avm.shadow.java.math.RoundingMode.avm_HALF_EVEN == LoadedDAppTarget.s_nine);
    }

    /**
     * Populate one of the classes with a reference to a class, and verify that it decodes properly, given the special-case.
     */
    @Test
    public void serializeDeserializeReferenceToClass() {
        org.aion.avm.shadow.java.lang.Class<?> originalClassRef = IHelper.currentContractHelper.get().externalWrapAsClass(String.class);
        LoadedDAppTarget.s_nine = originalClassRef;
        
        KernelInterfaceImpl kernel = new KernelInterfaceImpl();
        byte[] address = new byte[] {1,2,3};
        long initialInstanceId = 1l;
        LoadedDApp dapp = new LoadedDApp(LoadedDAppTest.class.getClassLoader(), address, Arrays.asList(LoadedDAppTarget.class), LoadedDAppTarget.class.getName());
        long nextInstanceId = dapp.saveClassStaticsToStorage(initialInstanceId, FEE_PROCESSOR, kernel);
        // Note that this attempt to serialize has no instances so the counter should be unchanged.
        Assert.assertEquals(initialInstanceId, nextInstanceId);
        byte[] result = kernel.getStorage(address, StorageKeys.CLASS_STATICS);
        // These are encoded in-order.  Some are obvious but we will explicitly decode the stub structure since it is harder to verify.
        byte[] expected = {
                // LoadedDAppTarget
                0x0, //s_one
                0x0, //s_two
                0x0, 0x0, //s_three
                0x0, 0x0, //s_four
                0x0, 0x0, 0x0, 0x0, //s_five
                0x0, 0x0, 0x0, 0x0, //s_six
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, //s_seven
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, //s_eight
                (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xfe, //s_nine (-2 since this is a class)
                0x0, 0x0, 0x0, 0x10, //s_nine (length)
                0x6a, 0x61, 0x76, 0x61, 0x2e, 0x6c, 0x61, 0x6e, 0x67, 0x2e, 0x53, 0x74, 0x72, 0x69, 0x6e, 0x67,  //s_nine (name of "java.lang.String")
        };
        Assert.assertTrue(Arrays.equals(expected, result));
        
        // Now, clear the statics, deserialize this, and ensure that we are still pointing at the same constant.
        clearStaticState();
        dapp.populateClassStaticsFromStorage(FEE_PROCESSOR, kernel);
        Assert.assertTrue(originalClassRef == LoadedDAppTarget.s_nine);
    }

    /**
     * Populate one of the classes with a reference to a constant class, and verify that it decodes properly, given the special-case.
     */
    @Test
    public void serializeDeserializeReferenceToConstantClass() {
        LoadedDAppTarget.s_nine = org.aion.avm.shadow.java.lang.Byte.avm_TYPE;
        
        KernelInterfaceImpl kernel = new KernelInterfaceImpl();
        byte[] address = new byte[] {1,2,3};
        long initialInstanceId = 1l;
        LoadedDApp dapp = new LoadedDApp(LoadedDAppTest.class.getClassLoader(), address, Arrays.asList(LoadedDAppTarget.class), LoadedDAppTarget.class.getName());
        long nextInstanceId = dapp.saveClassStaticsToStorage(initialInstanceId, FEE_PROCESSOR, kernel);
        // Note that this attempt to serialize has no instances so the counter should be unchanged.
        Assert.assertEquals(initialInstanceId, nextInstanceId);
        byte[] result = kernel.getStorage(address, StorageKeys.CLASS_STATICS);
        // These are encoded in-order.  Some are obvious but we will explicitly decode the stub structure since it is harder to verify.
        byte[] expected = {
                // LoadedDAppTarget
                0x0, //s_one
                0x0, //s_two
                0x0, 0x0, //s_three
                0x0, 0x0, //s_four
                0x0, 0x0, 0x0, 0x0, //s_five
                0x0, 0x0, 0x0, 0x0, //s_six
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, //s_seven
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, //s_eight
                (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, //s_nine (-1 since this is a constant)
                (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xed,  //s_nine (constant instanceId is negative)
        };
        Assert.assertTrue(Arrays.equals(expected, result));
        
        // Now, clear the statics, deserialize this, and ensure that we are still pointing at the same constant.
        clearStaticState();
        dapp.populateClassStaticsFromStorage(FEE_PROCESSOR, kernel);
        Assert.assertTrue(org.aion.avm.shadow.java.lang.Byte.avm_TYPE == LoadedDAppTarget.s_nine);
    }


    private static void clearStaticState() {
        ReflectionStructureCodecTarget.s_one = false;
        ReflectionStructureCodecTarget.s_two = 0;
        ReflectionStructureCodecTarget.s_three = 0;
        ReflectionStructureCodecTarget.s_four = 0;
        ReflectionStructureCodecTarget.s_five = 0;
        ReflectionStructureCodecTarget.s_six = 0.0f;
        ReflectionStructureCodecTarget.s_seven = 0;
        ReflectionStructureCodecTarget.s_eight = 0.0d;
        ReflectionStructureCodecTarget.s_nine = null;
        
        ReflectionStructureCodecTargetSub.s_one = false;
        ReflectionStructureCodecTargetSub.s_two = 0;
        ReflectionStructureCodecTargetSub.s_three = 0;
        ReflectionStructureCodecTargetSub.s_four = 0;
        ReflectionStructureCodecTargetSub.s_five = 0;
        ReflectionStructureCodecTargetSub.s_six = 0.0f;
        ReflectionStructureCodecTargetSub.s_seven = 0;
        ReflectionStructureCodecTargetSub.s_eight = 0.0d;
        ReflectionStructureCodecTargetSub.s_nine = null;
        
        LoadedDAppTarget.s_one = false;
        LoadedDAppTarget.s_two = 0;
        LoadedDAppTarget.s_three = 0;
        LoadedDAppTarget.s_four = 0;
        LoadedDAppTarget.s_five = 0;
        LoadedDAppTarget.s_six = 0.0f;
        LoadedDAppTarget.s_seven = 0;
        LoadedDAppTarget.s_eight = 0.0d;
    }
}
