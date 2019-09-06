package org.aion.avm.core.benchmarking;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Benchmarking reflection versus {@link java.lang.invoke.MethodHandle}.
 *
 * This benchmark is for:
 *   - Each class instance is from the same ClassLoader.
 *   - Measuring:
 *       constructor resolution/lookup
 *       static method resolution/lookup
 *       instance method resolution/lookup
 *       static field resolution/lookup
 *       instance field resolution/lookup
 */
public class ReflectionBenchmarkSameResolutionTest {
    private static int spins = ReflectionBenchmarkConstants.sameSpins;
    private static String targetClassName = ReflectionBenchmarkConstants.targetClassName;

    private String staticField = ReflectionBenchmarkConstants.staticField;
    private String instanceField = ReflectionBenchmarkConstants.instanceField;
    private String staticMethod = ReflectionBenchmarkConstants.staticMethod;
    private String instanceMethod = ReflectionBenchmarkConstants.instanceMethod;

    private void warmup() throws Exception {
        this.getClass().getClassLoader().loadClass(targetClassName);
    }

    @BeforeClass
    public static void setup() {
        System.out.println("Running each benchmark " + spins + " times each.");
    }

    // <----------------------------------------benchmarks----------------------------------------->

    @Test
    public void ReflectionConstructorResolutionTest() throws Exception {
        warmup();
        long sample = sameInstanceReflectionConstructorResolution(spins);
        printTime("Resolve constructor", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleConstructorResolutionTest() throws Exception {
        warmup();
        long sample = sameInstanceMethodHandleConstructorResolution(spins);
        printTime("Resolve constructor", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionInstanceMethodResolutionTest() throws Exception {
        warmup();
        long sample = sameInstanceReflectionInstanceMethodResolution(spins);
        printTime("Resolve instance method", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleInstanceMethodResolutionTest() throws Exception {
        warmup();
        long sample = sameInstanceMethodHandleInstanceMethodResolution(spins);
        printTime("Resolve instance method", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionStaticMethodResolutionTest() throws Exception {
        warmup();
        long sample = sameInstanceReflectionStaticMethodResolution(spins);
        printTime("Resolve static method", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleStaticMethodResolutionTest() throws Exception {
        warmup();
        long sample = sameInstanceMethodHandleStaticMethodResolution(spins);
        printTime("Resolve static method", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionInstanceFieldResolutionTest() throws Exception {
        warmup();
        long sample = sameInstanceReflectionInstanceFieldResolution(spins);
        printTime("Resolve instance field", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldResolutionTest() throws Exception {
        warmup();
        long sample = sameInstanceMethodHandleInstanceFieldResolution(spins);
        printTime("Resolve instance field", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionStaticFieldResolutionTest() throws Exception {
        warmup();
        long sample = sameInstanceReflectionStaticFieldResolution(spins);
        printTime("Resolve static field", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldResolutionTest() throws Exception {
        warmup();
        long sample = sameInstanceMethodHandleStaticFieldResolution(spins);
        printTime("Resolve static field", "MethodHandle", sample, spins);
    }

    // <-----------------------------------------the logic----------------------------------------->

    private long sameInstanceReflectionStaticFieldResolution(int spins) throws Exception {
        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            ReflectionTarget.class.getField(staticField);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionInstanceFieldResolution(int spins) throws Exception {
        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            ReflectionTarget.class.getField(instanceField);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionStaticMethodResolution(int spins) throws Exception {
        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            ReflectionTarget.class.getMethod(staticMethod, Number.class, Random.class);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionInstanceMethodResolution(int spins) throws Exception {
        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            ReflectionTarget.class.getMethod(instanceMethod, Integer.class, Object.class, float.class);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionConstructorResolution(int spins) throws Exception {
        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            ReflectionTarget.class.getConstructor(
                String.class,
                Object.class,
                Character.class,
                Float[].class);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticFieldResolution(int spins) throws Exception {
        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandles.lookup().findStaticGetter(ReflectionTarget.class, staticField, Boolean.class);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceFieldResolution(int spins) throws Exception {
        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandles.lookup().findGetter(ReflectionTarget.class, instanceField, Object.class);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticMethodResolution(int spins) throws Exception {
        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandles.lookup().findStatic(
                ReflectionTarget.class,
                staticMethod,
                MethodType.methodType(Object.class, Number.class, Random.class));
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceMethodResolution(int spins) throws Exception {
        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandles.lookup().findVirtual(
                ReflectionTarget.class,
                instanceMethod,
                MethodType.methodType(void.class, Integer.class, Object.class, float.class));
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleConstructorResolution(int spins) throws Exception {
        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandles.lookup().findConstructor(ReflectionTarget.class, MethodType.methodType(
                void.class,
                String.class,
                Object.class,
                Character.class,
                Float[].class));
        }
        long end = System.nanoTime();
        return end - start;
    }

    private static void printTime(String title, String measureName, long measure, long invokes) {
        BigDecimal measure1BD = BigDecimal.valueOf(measure).setScale(2, RoundingMode.HALF_UP);
        BigDecimal scaledMeasure1 = measure1BD.divide(BigDecimal.valueOf(invokes), RoundingMode.HALF_UP);
        System.out.println(title + " " + "\n\t" + measureName + ": " + scaledMeasure1.toPlainString() + "ns\n");
    }
}
