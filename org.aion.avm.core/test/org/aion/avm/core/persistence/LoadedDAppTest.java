package org.aion.avm.core.persistence;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.CommonInstrumentation;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.IRuntimeSetup;
import org.aion.avm.internal.InstrumentationHelpers;
import org.aion.avm.internal.InternedClasses;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class LoadedDAppTest {
    private static final int MAX_GRAPH_SIZE = 1000;
    
    private IInstrumentation instrumentation;
    private AvmClassLoader loader;
    private IRuntimeSetup runtimeSetup;

    // Debug mode MUST be enabled for these tests, otherwise the storage layer will transform the
    // user-defined names, which we do not want!
    private boolean preserveDebuggability = true;

    @Before
    public void setup() {
        // Force the initialization of the NodeEnvironment singleton.
        Assert.assertNotNull(NodeEnvironment.singleton);
        
        Map<String, byte[]> classAndHelper = Helpers.mapIncludingHelperBytecode(Collections.emptyMap(), Helpers.loadDefaultHelperBytecode());
        this.loader = NodeEnvironment.singleton.createInvocationClassLoader(classAndHelper);
        
        this.instrumentation = new CommonInstrumentation();
        InstrumentationHelpers.attachThread(this.instrumentation);
        this.runtimeSetup = Helpers.getSetupForLoader(this.loader);
        InstrumentationHelpers.pushNewStackFrame(this.runtimeSetup, this.loader, 1_000_000L, 1, new InternedClasses());

        // Clear statics, since our tests interact with them.
        clearStaticState();
    }

    @After
    public void tearDown() {
        InstrumentationHelpers.popExistingStackFrame(this.runtimeSetup);
        InstrumentationHelpers.detachThread(this.instrumentation);
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

        LoadedDApp dapp = new LoadedDApp(this.loader, Arrays.asList(ReflectionStructureCodecTarget.class, LoadedDAppTarget.class), ReflectionStructureCodecTarget.class.getName(), this.preserveDebuggability);
        byte[] result = dapp.saveEntireGraph(1, MAX_GRAPH_SIZE);
        String expectedHex = ""
                // hashcode
                + "00000001"
                // LoadedDAppTarget
                // (alphabetically sorted)
                // double s_eight
                + "4014000000000000"
                // int s_five
                + "00000005"
                // char s_four
                + "0005"
                // instance s_nine - null
                + "00"
                // boolean s_one
                + "01"
                // long s_seven
                + "0000000000000005"
                // float s_six
                + "40a00000"
                // short s_three
                + "0005"
                // byte s_two
                + "05"
                // ReflectionStructureCodecTarget
                // (alphabetically sorted)
                // double s_eight
                + "4014000000000000"
                // int s_five
                + "00000005"
                // char s_four
                + "0005"
                // instance s_nine - null
                + "00"
                // boolean s_one
                + "01"
                // long s_seven
                + "0000000000000005"
                // float s_six
                + "40a00000"
                // short s_three
                + "0005"
                // byte s_two
                + "05"
                ;
        byte[] expected = Helpers.hexStringToBytes(expectedHex);
        Assert.assertArrayEquals(expected, result);
    }

    /**
     * Load the state of 2 classes and make sure that they deserialized as expected.
     */
    @Test
    public void deserializeClasses() {
        String expectedHex = ""
                // hashcode
                + "00000001"
                // LoadedDAppTarget
                // (alphabetically sorted)
                // double s_eight
                + "4014000000000000"
                // int s_five
                + "00000005"
                // char s_four
                + "0005"
                // instance s_nine - null
                + "00"
                // boolean s_one
                + "01"
                // long s_seven
                + "0000000000000005"
                // float s_six
                + "40a00000"
                // short s_three
                + "0005"
                // byte s_two
                + "05"
                // ReflectionStructureCodecTarget
                // (alphabetically sorted)
                // double s_eight
                + "4014000000000000"
                // int s_five
                + "00000005"
                // char s_four
                + "0005"
                // instance s_nine - null
                + "00"
                // boolean s_one
                + "01"
                // long s_seven
                + "0000000000000005"
                // float s_six
                + "40a00000"
                // short s_three
                + "0005"
                // byte s_two
                + "05"
                ;
        byte[] expected = Helpers.hexStringToBytes(expectedHex);
        
        // Populate the classes.
        new LoadedDApp(this.loader, Arrays.asList(ReflectionStructureCodecTarget.class, LoadedDAppTarget.class), ReflectionStructureCodecTarget.class.getName(), this.preserveDebuggability)
            .loadEntireGraph(new InternedClasses(), expected);
        
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
        
        LoadedDApp dapp = new LoadedDApp(this.loader, Arrays.asList(ReflectionStructureCodecTarget.class), ReflectionStructureCodecTarget.class.getName(), this.preserveDebuggability);
        byte[] result = dapp.saveEntireGraph(1, MAX_GRAPH_SIZE);
        String expectedHex = ""
                // hashcode
                + "00000001"
                // ReflectionStructureCodecTarget
                // (alphabetically sorted)
                // double s_eight
                + "4014000000000000"
                // int s_five
                + "00000005"
                // char s_four
                + "0005"
                // instance s_nine - index 0
                + "0300000000"
                // boolean s_one
                + "01"
                // long s_seven
                + "0000000000000005"
                // float s_six
                + "40a00000"
                // short s_three
                + "0005"
                // byte s_two
                + "05"
                // ReflectionStructureCodecTarget instance
                // Name "org.aion.avm.core.persistence.ReflectionStructureCodecTarget"
                + "3c6f72672e61696f6e2e61766d2e636f72652e70657273697374656e63652e5265666c656374696f6e537472756374757265436f646563546172676574"
                // Hashcode
                + "00000001"
                // double s_eight
                + "0000000000000000"
                // int s_five
                + "00000000"
                // char s_four
                + "0000"
                // instance s_nine - null
                + "00"
                // boolean s_one
                + "00"
                // long s_seven
                + "0000000000000000"
                // float s_six
                + "00000000"
                // short s_three
                + "0000"
                // byte s_two
                + "00"
                ;
        byte[] expected = Helpers.hexStringToBytes(expectedHex);
        Assert.assertArrayEquals(expected, result);
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
        
        LoadedDApp dapp = new LoadedDApp(this.loader, Arrays.asList(ReflectionStructureCodecTarget.class, ReflectionStructureCodecTargetSub.class), ReflectionStructureCodecTarget.class.getName(), this.preserveDebuggability);
        int hashCode = 1;
        byte[] result = dapp.saveEntireGraph(hashCode, MAX_GRAPH_SIZE);
        
        // We always have a hashcode - both at the beginning of the buffer (next), and in each instance, before fields.
        int hashCodeSize = 4;
        // Target size:  1 ref + primitives.
        int primitiveSize = 1 + Byte.BYTES + Short.BYTES + Character.BYTES + Integer.BYTES + Float.BYTES + Long.BYTES + Double.BYTES;
        // Ref encoding:  1 byte type and 4 byte index.
        int refSize = 1 + 4;
        int targetNameSize = 1 + ReflectionStructureCodecTarget.class.getName().getBytes(StandardCharsets.UTF_8).length;
        int targetSubNameSize = 1 + ReflectionStructureCodecTargetSub.class.getName().getBytes(StandardCharsets.UTF_8).length;
        int targetInstanceSize = targetNameSize + hashCodeSize + primitiveSize + refSize;
        int targetSubInstanceSize = targetSubNameSize + hashCodeSize + primitiveSize + refSize + (primitiveSize + refSize);
        // The statics are both primitives and 1 ref, each instance is a name, all primitives and one ref (and there are 2).
        int expectedSize = hashCodeSize + 2 * (primitiveSize + refSize) + targetInstanceSize + targetSubInstanceSize;
        Assert.assertEquals(expectedSize, result.length);
        
        // Now, clear the class states and reload this.
        clearStaticState();
        int nextHashCode = dapp.loadEntireGraph(new InternedClasses(), result);
        Assert.assertEquals(hashCode, nextHashCode);
        
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
    }

    /**
     * Populate one of the classes with a reference to one of our JDK constants and demonstrate that the reference is to the same instance, on deserialization.
     */
    @Test
    public void serializeDeserializeReferenceToJdkConstant() {
        LoadedDAppTarget.s_nine = org.aion.avm.shadow.java.math.RoundingMode.avm_HALF_EVEN;
        
        LoadedDApp dapp = new LoadedDApp(this.loader, Arrays.asList(LoadedDAppTarget.class), LoadedDAppTarget.class.getName(), this.preserveDebuggability);
        int hashcode = 1;
        byte[] result = dapp.saveEntireGraph(hashcode, MAX_GRAPH_SIZE);
        String expectedHex = ""
                // hashcode
                + "00000001"
                // LoadedDAppTarget
                // (alphabetically sorted)
                // double s_eight
                + "0000000000000000"
                // int s_five
                + "00000000"
                // char s_four
                + "0000"
                // instance s_nine - pointing at constant 0x0d
                + "020000000d"
                // boolean s_one
                + "00"
                // long s_seven
                + "0000000000000000"
                // float s_six
                + "00000000"
                // short s_three
                + "0000"
                // byte s_two
                + "00"
                ;
        byte[] expected = Helpers.hexStringToBytes(expectedHex);
        Assert.assertArrayEquals(expected, result);
        
        // Now, clear the statics, deserialize this, and ensure that we are still pointing at the same constant.
        clearStaticState();
        int nextHashCode = dapp.loadEntireGraph(new InternedClasses(), expected);
        Assert.assertEquals(hashcode, nextHashCode);
        Assert.assertTrue(org.aion.avm.shadow.java.math.RoundingMode.avm_HALF_EVEN == LoadedDAppTarget.s_nine);
    }

    /**
     * Populate one of the classes with a reference to a class, and verify that it decodes properly, given the special-case.
     */
    @Test
    public void serializeDeserializeReferenceToClass() {
        InternedClasses internedClasses = new InternedClasses();
        org.aion.avm.shadow.java.lang.Class<?> originalClassRef = internedClasses.get(org.aion.avm.shadow.java.lang.String.class);
        LoadedDAppTarget.s_nine = originalClassRef;
        
        LoadedDApp dapp = new LoadedDApp(this.loader, Arrays.asList(LoadedDAppTarget.class), LoadedDAppTarget.class.getName(), this.preserveDebuggability);
        byte[] result = dapp.saveEntireGraph(1, MAX_GRAPH_SIZE);
        String expectedHex = ""
                // hashcode
                + "00000001"
                // LoadedDAppTarget
                // (alphabetically sorted)
                // double s_eight
                + "0000000000000000"
                // int s_five
                + "00000000"
                // char s_four
                + "0000"
                // instance s_nine - pointing at class "java.lang.String" (16)
                + "01106a6176612e6c616e672e537472696e67"
                // boolean s_one
                + "00"
                // long s_seven
                + "0000000000000000"
                // float s_six
                + "00000000"
                // short s_three
                + "0000"
                // byte s_two
                + "00"
                ;
        byte[] expected = Helpers.hexStringToBytes(expectedHex);
        Assert.assertArrayEquals(expected, result);
        
        // Now, clear the statics, deserialize this, and ensure that we are still pointing at the same constant.
        clearStaticState();
        int nextHashCode = dapp.loadEntireGraph(internedClasses, expected);
        Assert.assertEquals(1, nextHashCode);
        Assert.assertTrue(originalClassRef == LoadedDAppTarget.s_nine);
    }

    /**
     * Populate one of the classes with a reference to a constant class, and verify that it decodes properly, given the special-case.
     */
    @Test
    public void serializeDeserializeReferenceToConstantClass() {
        LoadedDAppTarget.s_nine = org.aion.avm.shadow.java.lang.Byte.avm_TYPE;
        
        LoadedDApp dapp = new LoadedDApp(this.loader, Arrays.asList(LoadedDAppTarget.class), LoadedDAppTarget.class.getName(), this.preserveDebuggability);
        byte[] result = dapp.saveEntireGraph(1, MAX_GRAPH_SIZE);
        String expectedHex = ""
                // hashcode
                + "00000001"
                // LoadedDAppTarget
                // (alphabetically sorted)
                // double s_eight
                + "0000000000000000"
                // int s_five
                + "00000000"
                // char s_four
                + "0000"
                // instance s_nine - pointing at constant 0x10
                + "0200000010"
                // boolean s_one
                + "00"
                // long s_seven
                + "0000000000000000"
                // float s_six
                + "00000000"
                // short s_three
                + "0000"
                // byte s_two
                + "00"
                ;
        byte[] expected = Helpers.hexStringToBytes(expectedHex);
        Assert.assertArrayEquals(expected, result);
        
        // Now, clear the statics, deserialize this, and ensure that we are still pointing at the same constant.
        clearStaticState();
        int nextHashCode = dapp.loadEntireGraph(new InternedClasses(), expected);
        Assert.assertEquals(1, nextHashCode);
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
