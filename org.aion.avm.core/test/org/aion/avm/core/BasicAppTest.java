package org.aion.avm.core;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.rt.BlockchainRuntime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * As part of issue-77, we want to see what a more typical application can see, from inside our environment.
 * This test operates on BasicAppTestTarget to observe what we are doing, from the inside.
 * Eventually, this will change into a shape where we will use the standard AvmImpl to completely run this
 * life-cycle, but we want to prove that it works, in isolation, before changing its details to account for
 * this design (especially considering that the entry-point interface is likely temporary).
 */
public class BasicAppTest {
    private static AvmSharedClassLoader sharedClassLoader;

    @BeforeClass
    public static void setupClass() throws Exception {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
    }

    private Class<?> clazz;
    private Method decodeMethod;
    private BlockchainRuntime runtime;

    @Before
    public void setup() throws Exception {
        // NOTE:  This boiler-plate is pulled directly from HashCodeTest but will eventually be cut-over to using AvmImpl, differently.
        String className = BasicAppTestTarget.class.getName();
        byte[] raw = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        
        Forest<String, byte[]> classHierarchy = new HierarchyTreeBuilder()
                .addClass(className, "java.lang.Object", raw)
                .asMutableForest();
        
        AvmImpl avm = new AvmImpl(sharedClassLoader);
        Map<String, Integer> runtimeObjectSizes = AvmImpl.computeRuntimeObjectSizes();
        Map<String, Integer> allObjectSizes = avm.computeObjectSizes(classHierarchy, runtimeObjectSizes);
        Function<byte[], byte[]> transformer = (inputBytes) -> {
            return avm.transformClasses(Collections.singletonMap(className, inputBytes), classHierarchy, allObjectSizes).get(className);
        };
        Map<String, byte[]> classes = Helpers.mapIncludingHelperBytecode(Collections.singletonMap(className, transformer.apply(raw)));
        AvmClassLoader loader = new AvmClassLoader(sharedClassLoader, classes);
        this.clazz = loader.loadClass(className);
        // NOTE:  The user's side is pre-shadow so it uses "byte[]" whereas we look up "ByteArray", here.
        this.decodeMethod = this.clazz.getMethod("decode", BlockchainRuntime.class, ByteArray.class);
        Assert.assertEquals(loader, this.clazz.getClassLoader());
        
        BlockchainRuntime externalRuntime = new SimpleRuntime(new byte[0], new byte[0], 10000);
        Helpers.instantiateHelper(loader, externalRuntime);
        // Create the wrapper for the runtime object, now that the external one has been used to create the Helper required to instantiate shadow objects.
        this.runtime = new ContractRuntimeWrapper(externalRuntime);
    }

    @Test
    public void testIdentity() throws Exception {
        ByteArray input = new ByteArray(new byte[] {BasicAppTestTarget.kMethodIdentity, 42, 13});
        ByteArray output = (ByteArray)this.decodeMethod.invoke(null, this.runtime, input);
        // These should be the same instance.
        Assert.assertEquals(input, output);
    }

    @Test
    public void testSumInput() throws Exception {
        ByteArray input = new ByteArray(new byte[] {BasicAppTestTarget.kMethodSum, 42, 13});
        ByteArray output = (ByteArray)this.decodeMethod.invoke(null, this.runtime, input);
        // Should be just 1 byte, containing the sum.
        Assert.assertEquals(1, output.length());
        Assert.assertEquals(BasicAppTestTarget.kMethodSum + 42 + 13, output.get(0));
    }

    @Test
    public void testLowOrderByteArrayHash() throws Exception {
        ByteArray input = new ByteArray(new byte[] {BasicAppTestTarget.kMethodLowOrderByteArrayHash, 42, 13});
        ByteArray output = (ByteArray)this.decodeMethod.invoke(null, this.runtime, input);
        // Should be just 1 byte, containing the low hash byte.
        Assert.assertEquals(1, output.length());
        byte result = output.get(0);
        // This should match the input we gave them.
        Assert.assertEquals(input.avm_hashCode(), result);
    }

    @Test
    public void testLowOrderRuntimeHash() throws Exception {
        ByteArray input = new ByteArray(new byte[] {BasicAppTestTarget.kMethodLowOrderRuntimeHash, 42, 13});
        ByteArray output = (ByteArray)this.decodeMethod.invoke(null, this.runtime, input);
        // Should be just 1 byte, containing the low hash byte.
        Assert.assertEquals(1, output.length());
        byte result = output.get(0);
        // We know that the runtime was the first object we created so its hash will be 1.
        Assert.assertEquals(1, result);
    }

    /**
     * This test makes multiple calls to the same contract instance, proving that static state survives between the calls.
     * It is mostly just a test to make sure that this property continues to be true, in the future, once we decide how
     * to save and resume state.
     */
    @Test
    public void testRepeatedSwaps() throws Exception {
        ByteArray input1 = new ByteArray(new byte[] {BasicAppTestTarget.kMethodSwapInputsFromLastCall, 1});
        ByteArray input2 = new ByteArray(new byte[] {BasicAppTestTarget.kMethodSwapInputsFromLastCall, 2});
        ByteArray output1 = (ByteArray)this.decodeMethod.invoke(null, this.runtime, input1);
        Assert.assertNull(output1);
        ByteArray output2 = (ByteArray)this.decodeMethod.invoke(null, this.runtime, input2);
        Assert.assertEquals(input1.get(1), output2.get(1));
        ByteArray output3 = (ByteArray)this.decodeMethod.invoke(null, this.runtime, input1);
        Assert.assertEquals(input2.get(1), output3.get(1));
    }
}
