package org.aion.avm.core.instrument;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.SimpleRuntime;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.junit.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public class ClassMeteringTest {
    private static AvmSharedClassLoader sharedClassLoader;

    private Class<?> clazz;
    // We keep this here just because a lot of cases want to use this sort of "do no instrumentation" cost builder for testing other rewrites.
    private final Function<byte[], byte[]> commonCostBuilder = (inputBytes) ->
            new ClassToolchain.Builder(inputBytes, ClassReader.SKIP_DEBUG)
                    .addNextVisitor(new ClassMetering(TestEnergy.CLASS_NAME, null))
                    .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                    .build()
                    .runAndGetBytecode();

    @BeforeClass
    public static void setupClass() {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateExceptionShadowsAndWrappers());
    }

    @Before
    public void setup() throws Exception {
        // Clear the state of our static test class.
        TestEnergy.totalCost = 0;
        TestEnergy.totalCharges = 0;
        TestEnergy.totalArrayElements = 0;
        TestEnergy.totalArrayInstances = 0;

        // Setup and rewrite the class.
        String className = TestResource.class.getName();
        byte[] raw = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        Map<String, byte[]> classes = new HashMap<>();
        classes.put(className, this.commonCostBuilder.apply(raw));
        AvmClassLoader loader = new AvmClassLoader(sharedClassLoader, classes);
        this.clazz = loader.loadClass(className);
        
        // We only need to install a BlockchainRuntime which can afford our energy.
        new Helper(loader, new SimpleRuntime(null, null, 1000));
    }

    @After
    public void teardown() throws Exception {
        Helper.clearTestingState();
    }

    /**
     * Tests that we can add a instrumented callout to the beginning of each block.
     */
    @Test
    public void testWrittenBlockPrefix() throws Exception {
        // By this point, we should still have 0 charges.
        Assert.assertEquals(0, TestEnergy.totalCharges);
        Object target = clazz.getConstructor(int.class).newInstance(6);
        // We expect to see 1 charge for init - it is only 1 block.
        Assert.assertEquals(1, TestEnergy.totalCharges);
        long expectedCost = getFees(
                Opcodes.ALOAD
                , Opcodes.INVOKESPECIAL
                , Opcodes.ALOAD
                , Opcodes.ILOAD
                , Opcodes.PUTFIELD
                , Opcodes.RETURN
        );
        Assert.assertEquals(expectedCost, TestEnergy.totalCost);
        target.hashCode();
        // Now, we should see the additional charge for the hashcode block.
        Assert.assertEquals(2, TestEnergy.totalCharges);
        expectedCost += getFees(
                Opcodes.ALOAD
                , Opcodes.GETFIELD
                , Opcodes.IRETURN
        );
        Assert.assertEquals(expectedCost, TestEnergy.totalCost);
    }

    /**
     * Tests that we can replace anewarray bytecodes with call-out routines.
     */
    @Test
    public void testAnewarrayCallOut() throws Exception {
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Object target = clazz.getConstructor(int.class).newInstance(6);
        Method buildStringArray = clazz.getMethod("buildStringArray", int.class);

        // Check that we haven't yet called the TestEnergy to create an array.
        Assert.assertEquals(0, TestEnergy.totalArrayElements);
        Assert.assertEquals(0, TestEnergy.totalArrayInstances);
        
        // Create an array and make sure it is correct.
        String[] one = (String[]) buildStringArray.invoke(target, 2);
        Assert.assertEquals(2, one.length);
        Assert.assertEquals(2, TestEnergy.totalArrayElements);
        Assert.assertEquals(1, TestEnergy.totalArrayInstances);
        
        // Create another.
        String[] two = (String[]) buildStringArray.invoke(target, 25);
        Assert.assertEquals(25, two.length);
        Assert.assertEquals(27, TestEnergy.totalArrayElements);
        Assert.assertEquals(2, TestEnergy.totalArrayInstances);
    }

    /**
     * Tests that we can replace multianewarray bytecodes with call-out routines.
     */
    @Test
    public void testMultianewarrayCallOut() throws Exception {
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Object target = clazz.getConstructor(int.class).newInstance(6);
        Method buildMultiStringArray3 = clazz.getMethod("buildMultiStringArray3", int.class, int.class, int.class);

        // Check that we haven't yet called the TestEnergy to create an array.
        Assert.assertEquals(0, TestEnergy.totalArrayElements);
        Assert.assertEquals(0, TestEnergy.totalArrayInstances);
        
        // Create an array and make sure it is correct.
        String[][][] one = (String[][][]) buildMultiStringArray3.invoke(target, 2, 3, 4);
        Assert.assertEquals(2, one.length);
        Assert.assertEquals(3, one[0].length);
        Assert.assertEquals(4, one[0][1].length);
        Assert.assertEquals(24, TestEnergy.totalArrayElements);
        Assert.assertEquals(1, TestEnergy.totalArrayInstances);
        
        // Verify our assumption that this is the same as the original implementation.
        String[][][] original = new TestResource(5).buildMultiStringArray3(2, 3, 4);
        Assert.assertEquals(2, original.length);
        Assert.assertEquals(3, original[0].length);
        Assert.assertEquals(4, original[0][1].length);
        // We shouldn't see an energy increase in the original class.
        Assert.assertEquals(24, TestEnergy.totalArrayElements);
        Assert.assertEquals(1, TestEnergy.totalArrayInstances);
    }

    /**
     * Tests that we can replace primitive multianewarray bytecodes with call-out routines.
     */
    @Test
    public void testMultianewarrayPrimitive() throws Exception {
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Object target = clazz.getConstructor(int.class).newInstance(6);
        Method buildLongArray2 = clazz.getMethod("buildLongArray2", int.class, int.class);

        // Check that we haven't yet called the TestEnergy to create an array.
        Assert.assertEquals(0, TestEnergy.totalArrayElements);
        Assert.assertEquals(0, TestEnergy.totalArrayInstances);
        
        // Create an array and make sure it is correct.
        long[][] one = (long[][]) buildLongArray2.invoke(target, 2, 3);
        Assert.assertEquals(2, one.length);
        Assert.assertEquals(3, one[0].length);
        Assert.assertEquals(6, TestEnergy.totalArrayElements);
        Assert.assertEquals(1, TestEnergy.totalArrayInstances);
        
        // Verify our assumption that this is the same as the original implementation.
        long[][] original = (long[][]) new TestResource(5).buildLongArray2(2, 3);
        Assert.assertEquals(2, original.length);
        Assert.assertEquals(3, original[0].length);
        // We shouldn't see an energy increase in the original class.
        Assert.assertEquals(6, TestEnergy.totalArrayElements);
        Assert.assertEquals(1, TestEnergy.totalArrayInstances);
    }

    /**
     * Tests that we can replace primitive newarray bytecodes with call-out routines.
     */
    @Test
    public void testNewarrayChar() throws Exception {
        // We need to use reflection to call this, since the class was loaded by this other classloader.
        Object target = clazz.getConstructor(int.class).newInstance(6);
        Method buildCharArray = clazz.getMethod("buildCharArray", int.class);

        // Check that we haven't yet called the TestEnergy to create an array.
        Assert.assertEquals(0, TestEnergy.totalArrayElements);
        Assert.assertEquals(0, TestEnergy.totalArrayInstances);
        
        // Create an array and make sure it is correct.
        char[] one = (char[]) buildCharArray.invoke(target, 2);
        Assert.assertEquals(2, one.length);
        Assert.assertEquals(2, TestEnergy.totalArrayElements);
        Assert.assertEquals(1, TestEnergy.totalArrayInstances);
        
        // Verify our assumption that this is the same as the original implementation.
        char[] original = (char[]) new TestResource(5).buildCharArray(2);
        Assert.assertEquals(2, original.length);
        // We shouldn't see an energy increase in the original class.
        Assert.assertEquals(2, TestEnergy.totalArrayElements);
        Assert.assertEquals(1, TestEnergy.totalArrayInstances);
    }


    private long getFees(int... opcodes) {
        long total = 0;
        
        BytecodeFeeScheduler fees = new BytecodeFeeScheduler();
        fees.initialize();
        for (int opcode : opcodes) {
            total += fees.getFee(opcode);
        }
        return total;
    }


    /**
     * NOTE:  This class is used for the "testWrittenBlockPrefix()" test.
     */
    public static class TestEnergy {
        public static String CLASS_NAME = TestEnergy.class.getName().replaceAll("\\.", "/");
        public static long totalCost;
        public static int totalCharges;
        public static int totalArrayElements;
        public static int totalArrayInstances;

        public static void chargeEnergy(long cost) {
            TestEnergy.totalCost += cost;
            TestEnergy.totalCharges += 1;
            Helper.chargeEnergy(cost);
        }

        public static Object multianewarray1(int d1, Class<?> cl) {
            TestEnergy.totalArrayElements += d1;
            TestEnergy.totalArrayInstances += 1;
            return Helper.multianewarray1(d1, cl);
        }

        public static Object multianewarray2(int d1, int d2, Class<?> cl) {
            TestEnergy.totalArrayElements += d1 * d2;
            TestEnergy.totalArrayInstances += 1;
            return Helper.multianewarray2(d1, d2, cl);
        }

        public static Object multianewarray3(int d1, int d2, int d3, Class<?> cl) {
            TestEnergy.totalArrayElements += d1 * d2 * d3;
            TestEnergy.totalArrayInstances += 1;
            return Helper.multianewarray3(d1, d2, d3, cl);
        }
    }
}
