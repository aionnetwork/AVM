package org.aion.avm.core.persistence;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.internal.Helper;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ReflectionStructureCodecTest {
    /**
     * Note that most of these tests aren't interested in recursive serialize so this will skip that.
     */
    private static final Consumer<org.aion.avm.shadow.java.lang.Object> NULL_CONSUMER = (instance) -> {};

    @Before
    public void setup() {
        // Force the initialization of the NodeEnvironment singleton.
        Assert.assertNotNull(NodeEnvironment.singleton);
        
        new Helper(ReflectionStructureCodecTarget.class.getClassLoader(), 1_000_000L, 1);
    }

    @After
    public void tearDown() {
        Helper.clearTestingState();
    }

    /**
     * Create a full class and serialize it to see how each type is serialized.
     */
    @Test
    public void serializeClass() {
        ReflectionStructureCodecTarget.s_one = true;
        ReflectionStructureCodecTarget.s_two = 5;
        ReflectionStructureCodecTarget.s_three = 5;
        ReflectionStructureCodecTarget.s_four = 5;
        ReflectionStructureCodecTarget.s_five = 5;
        ReflectionStructureCodecTarget.s_six = 5.0f;
        ReflectionStructureCodecTarget.s_seven = 5;
        ReflectionStructureCodecTarget.s_eight = 5.0d;
        ReflectionStructureCodecTarget.s_nine = new ReflectionStructureCodecTarget();
        
        ReflectionStructureCodec codec = new ReflectionStructureCodec(new HashMap<>(), null, null, null, 1);
        StreamingPrimitiveCodec.Encoder encoder = StreamingPrimitiveCodec.buildEncoder();
        codec.serializeClass(encoder, ReflectionStructureCodecTarget.class, NULL_CONSUMER);
        byte[] result = encoder.toBytes();
        // These are encoded in-order.  Some are obvious but we will explicitly decode the stub structure since it is harder to verify.
        byte[] expected = {
                0x1, //s_one
                0x5, //s_two
                0x0, 0x5, //s_three
                0x0, 0x5, //s_four
                0x0, 0x0, 0x0, 0x5, //s_five
                0x40, (byte)0xa0, 0x0, 0x0, //s_six
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x5, //s_seven
                0x40, 0x14, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, //s_eight
                0x0, 0x0, 0x0, 0x3c, 0x6f, 0x72, 0x67, 0x2e, 0x61, 0x69, 0x6f, 0x6e, 0x2e, 0x61, 0x76, 0x6d, 0x2e, 0x63, 0x6f, 0x72, 0x65, 0x2e, 0x70, 0x65, 0x72, 0x73, 0x69, 0x73, 0x74, 0x65, 0x6e, 0x63, 0x65, 0x2e, 0x52, 0x65, 0x66, 0x6c, 0x65, 0x63, 0x74, 0x69, 0x6f, 0x6e, 0x53, 0x74, 0x72, 0x75, 0x63, 0x74, 0x75, 0x72, 0x65, 0x43, 0x6f, 0x64, 0x65, 0x63, 0x54, 0x61, 0x72, 0x67, 0x65, 0x74, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x1 //s_nine
        };
        Assert.assertTrue(Arrays.equals(expected, result));
        // We know the stub structure starts with a class name length at offset 30 (4 bytes, big-endian).
        int length = readIntAtOffset(result, 30);
        String className = new String(result, 34, length, StandardCharsets.UTF_8);
        Assert.assertEquals(ReflectionStructureCodecTarget.s_nine.getClass().getName(), className);
    }

    /**
     * Create a full instance and serialize it to see how similar it is to the class.
     */
    @Test
    public void serializeInstance() {
        ReflectionStructureCodecTarget target = new ReflectionStructureCodecTarget();
        target.i_one = true;
        target.i_two = 5;
        target.i_three = 5;
        target.i_four = 5;
        target.i_five = 5;
        target.i_six = 5.0f;
        target.i_seven = 5;
        target.i_eight = 5.0d;
        target.i_nine = new ReflectionStructureCodecTarget();
        
        ReflectionStructureCodec codec = new ReflectionStructureCodec(new HashMap<>(), null, null, null, 1);
        byte[] result = serializeSinceInstanceHelper(codec, target);
        // These are encoded in-order.  Some are obvious but we will explicitly decode the stub structure since it is harder to verify.
        // This is the same as what we got for the class except that this also has a hashcode.
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x1, //i_one
                0x5, //i_two
                0x0, 0x5, //i_three
                0x0, 0x5, //i_four
                0x0, 0x0, 0x0, 0x5, //i_five
                0x40, (byte)0xa0, 0x0, 0x0, //i_six
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x5, //i_seven
                0x40, 0x14, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, //i_eight
                0x0, 0x0, 0x0, 0x3c, 0x6f, 0x72, 0x67, 0x2e, 0x61, 0x69, 0x6f, 0x6e, 0x2e, 0x61, 0x76, 0x6d, 0x2e, 0x63, 0x6f, 0x72, 0x65, 0x2e, 0x70, 0x65, 0x72, 0x73, 0x69, 0x73, 0x74, 0x65, 0x6e, 0x63, 0x65, 0x2e, 0x52, 0x65, 0x66, 0x6c, 0x65, 0x63, 0x74, 0x69, 0x6f, 0x6e, 0x53, 0x74, 0x72, 0x75, 0x63, 0x74, 0x75, 0x72, 0x65, 0x43, 0x6f, 0x64, 0x65, 0x63, 0x54, 0x61, 0x72, 0x67, 0x65, 0x74, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x1 //i_nine
        };
        Assert.assertTrue(Arrays.equals(expected, result));
        // We know the stub structure starts with a class name length at offset 34 (4 bytes, big-endian).
        int length = readIntAtOffset(result, 34);
        String className = new String(result, 38, length, StandardCharsets.UTF_8);
        Assert.assertEquals(target.i_nine.getClass().getName(), className);
    }

    /**
     * Read an instance from our expected byte array and verify the data is what we expected.
     */
    @Test
    public void deserializeInstance() {
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x1, //i_one
                0x5, //i_two
                0x0, 0x5, //i_three
                0x0, 0x5, //i_four
                0x0, 0x0, 0x0, 0x5, //i_five
                0x40, (byte)0xa0, 0x0, 0x0, //i_six
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x5, //i_seven
                0x40, 0x14, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, //i_eight
                0x0, 0x0, 0x0, 0x3c, 0x6f, 0x72, 0x67, 0x2e, 0x61, 0x69, 0x6f, 0x6e, 0x2e, 0x61, 0x76, 0x6d, 0x2e, 0x63, 0x6f, 0x72, 0x65, 0x2e, 0x70, 0x65, 0x72, 0x73, 0x69, 0x73, 0x74, 0x65, 0x6e, 0x63, 0x65, 0x2e, 0x52, 0x65, 0x66, 0x6c, 0x65, 0x63, 0x74, 0x69, 0x6f, 0x6e, 0x53, 0x74, 0x72, 0x75, 0x63, 0x74, 0x75, 0x72, 0x65, 0x43, 0x6f, 0x64, 0x65, 0x63, 0x54, 0x61, 0x72, 0x67, 0x65, 0x74, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x1 //i_nine
        };
        CacheAwareFieldPopulator populator = new CacheAwareFieldPopulator(ReflectionStructureCodecTarget.class.getClassLoader());
        ReflectionStructureCodec deserializer = new ReflectionStructureCodec(new HashMap<>(), populator, null, null, 1);
        // Note that the deserializer always assumes it is operating on stubs so create the instance and pass it in.
        ReflectionStructureCodecTarget target = new ReflectionStructureCodecTarget();
        deserializer.deserializeInstance(target, expected);
        
        Assert.assertEquals(1, target.avm_hashCode());
        Assert.assertEquals(true, target.i_one);
        Assert.assertEquals(5, target.i_two);
        Assert.assertEquals(5, target.i_three);
        Assert.assertEquals(5, target.i_four);
        Assert.assertEquals(5, target.i_five);
        Assert.assertEquals(5.0f, target.i_six, 0.1);
        Assert.assertEquals(5l, target.i_seven);
        Assert.assertEquals(5.0d, target.i_eight, 0.1);
        Assert.assertNotNull(target.i_nine);
    }

    /**
     * Create a few instances, connected together, and serialize them all to show how they differ only in instanceId.
     */
    @Test
    public void serializeInstanceGraph() {
        ReflectionStructureCodecTarget root = new ReflectionStructureCodecTarget();
        ReflectionStructureCodecTarget one = new ReflectionStructureCodecTarget();
        ReflectionStructureCodecTarget two = new ReflectionStructureCodecTarget();
        root.i_nine = one;
        one.i_nine = two;
        
        // We want to verify that these instances only differ in their hashcodes and instanceIds for instance stubs.
        CacheAwareFieldPopulator populator = new CacheAwareFieldPopulator(ReflectionStructureCodecTarget.class.getClassLoader());
        ReflectionStructureCodec codec = new ReflectionStructureCodec(new HashMap<>(), populator, null, null, 1);
        byte[] rootBytes = serializeSinceInstanceHelper(codec, root);
        byte[] oneBytes = serializeSinceInstanceHelper(codec, one);
        byte[] twoBytes = serializeSinceInstanceHelper(codec, two);
        // Compare the middle segments of these to a zero array.
        byte[] zero = new byte[30];
        Assert.assertTrue(Arrays.equals(zero, 0, zero.length-1, rootBytes, 4, zero.length + 4 -1));
        Assert.assertTrue(Arrays.equals(zero, 0, zero.length-1, oneBytes, 4, zero.length + 4 -1));
        Assert.assertTrue(Arrays.equals(zero, 0, zero.length-1, twoBytes, 4, zero.length + 4 -1));
        // Check each hashcode.
        Assert.assertEquals(1, readIntAtOffset(rootBytes, 0));
        Assert.assertEquals(2, readIntAtOffset(oneBytes, 0));
        Assert.assertEquals(3, readIntAtOffset(twoBytes, 0));
        // Check the instance stubs:  root and one should have 2 and 3, as instanceIds, respectively, since they point to one and two
        // (instances 2 and 3) but two should have a shorter array with zero for the "null" (zero-length type name and no instanceId).
        Assert.assertEquals(106, rootBytes.length);
        Assert.assertEquals(106, oneBytes.length);
        Assert.assertEquals(38, twoBytes.length);
        Assert.assertEquals(1, rootBytes[rootBytes.length - 1]);
        Assert.assertEquals(2, oneBytes[oneBytes.length - 1]);
        Assert.assertEquals(0, twoBytes[twoBytes.length - 1]);
    }

    /**
     * Create a simple graph and serialize them all to show how convergent branches have the same instanceId.
     */
    @Test
    public void serializeInstanceOverlap() {
        ReflectionStructureCodecTarget root1 = new ReflectionStructureCodecTarget();
        ReflectionStructureCodecTarget root2 = new ReflectionStructureCodecTarget();
        ReflectionStructureCodecTarget overlap = new ReflectionStructureCodecTarget();
        root1.i_nine = overlap;
        root2.i_nine = overlap;
        
        // We want to verify that these instances only differ in their hashcodes and instanceIds for instance stubs.
        CacheAwareFieldPopulator populator = new CacheAwareFieldPopulator(ReflectionStructureCodecTarget.class.getClassLoader());
        ReflectionStructureCodec codec = new ReflectionStructureCodec(new HashMap<>(), populator, null, null, 1);
        byte[] root1Bytes = serializeSinceInstanceHelper(codec, root1);
        byte[] root2Bytes = serializeSinceInstanceHelper(codec, root2);
        // These are empty and point to the same instance so they should be identical, after the hashcode.
        Assert.assertTrue(Arrays.equals(root1Bytes, 4, root1Bytes.length -4 - 1, root2Bytes, 4, root2Bytes.length -4 - 1));
        // Verify that the last byte is this instanceId of "1".
        Assert.assertEquals(1, root1Bytes[root1Bytes.length - 1]);
    }

    /**
     * Read a few instances which describe an overlap and verify that they have instance-equal stub pointers.
     */
    @Test
    public void deserializeInstanceOverlap() {
        byte[] expected1 = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x1, //i_one
                0x5, //i_two
                0x0, 0x5, //i_three
                0x0, 0x5, //i_four
                0x0, 0x0, 0x0, 0x5, //i_five
                0x40, (byte)0xa0, 0x0, 0x0, //i_six
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x5, //i_seven
                0x40, 0x14, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, //i_eight
                0x0, 0x0, 0x0, 0x3c, 0x6f, 0x72, 0x67, 0x2e, 0x61, 0x69, 0x6f, 0x6e, 0x2e, 0x61, 0x76, 0x6d, 0x2e, 0x63, 0x6f, 0x72, 0x65, 0x2e, 0x70, 0x65, 0x72, 0x73, 0x69, 0x73, 0x74, 0x65, 0x6e, 0x63, 0x65, 0x2e, 0x52, 0x65, 0x66, 0x6c, 0x65, 0x63, 0x74, 0x69, 0x6f, 0x6e, 0x53, 0x74, 0x72, 0x75, 0x63, 0x74, 0x75, 0x72, 0x65, 0x43, 0x6f, 0x64, 0x65, 0x63, 0x54, 0x61, 0x72, 0x67, 0x65, 0x74, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x1 //i_nine
        };
        byte[] expected2 = {
                0x0, 0x0, 0x0, 0x2, //hashcode
                0x1, //i_one
                0x5, //i_two
                0x0, 0x5, //i_three
                0x0, 0x5, //i_four
                0x0, 0x0, 0x0, 0x5, //i_five
                0x40, (byte)0xa0, 0x0, 0x0, //i_six
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x5, //i_seven
                0x40, 0x14, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, //i_eight
                0x0, 0x0, 0x0, 0x3c, 0x6f, 0x72, 0x67, 0x2e, 0x61, 0x69, 0x6f, 0x6e, 0x2e, 0x61, 0x76, 0x6d, 0x2e, 0x63, 0x6f, 0x72, 0x65, 0x2e, 0x70, 0x65, 0x72, 0x73, 0x69, 0x73, 0x74, 0x65, 0x6e, 0x63, 0x65, 0x2e, 0x52, 0x65, 0x66, 0x6c, 0x65, 0x63, 0x74, 0x69, 0x6f, 0x6e, 0x53, 0x74, 0x72, 0x75, 0x63, 0x74, 0x75, 0x72, 0x65, 0x43, 0x6f, 0x64, 0x65, 0x63, 0x54, 0x61, 0x72, 0x67, 0x65, 0x74, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x1 //i_nine
        };
        CacheAwareFieldPopulator populator = new CacheAwareFieldPopulator(ReflectionStructureCodecTarget.class.getClassLoader());
        ReflectionStructureCodec deserializer = new ReflectionStructureCodec(new HashMap<>(), populator, null, null, 1);
        ReflectionStructureCodecTarget target1 = new ReflectionStructureCodecTarget();
        deserializer.deserializeInstance(target1, expected1);
        ReflectionStructureCodecTarget target2 = new ReflectionStructureCodecTarget();
        deserializer.deserializeInstance(target2, expected2);
        Assert.assertTrue(target1.i_nine == target2.i_nine);
    }


    private static byte[] serializeSinceInstanceHelper(ReflectionStructureCodec codec, ReflectionStructureCodecTarget instance) {
        return codec.internalSerializeInstance(instance, NULL_CONSUMER);
    }

    private static int readIntAtOffset(byte[] bytes, int offset) {
        return (bytes[offset + 0] << 24) | (bytes[offset + 1] << 16) | (bytes[offset + 2] << 8) | bytes[offset + 3];
    }
}
