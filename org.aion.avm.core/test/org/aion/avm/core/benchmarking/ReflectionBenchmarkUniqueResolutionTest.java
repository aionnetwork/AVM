package org.aion.avm.core.benchmarking;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Random;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Benchmarking reflection versus {@link java.lang.invoke.MethodHandle}.
 *
 * This benchmark is for:
 *   - Each class instance is from a unique class loaded by a unique ClassLoader.
 *   - Measuring:
 *       constructor resolution/lookup
 *       static method resolution/lookup
 *       instance method resolution/lookup
 *       static field resolution/lookup
 *       instance field resolution/lookup
 */
public class ReflectionBenchmarkUniqueResolutionTest {
    // NOTE:  Output is ONLY produced if REPORT is set to true.
    private static final boolean REPORT = false;
    private static int spins = ReflectionBenchmarkConstants.uniqueSpins;
    private static String targetClassName = ReflectionBenchmarkConstants.targetClassName;
    private static File classpathDirectory = ReflectionBenchmarkConstants.classpathDirectory;

    private String staticField = ReflectionBenchmarkConstants.staticField;
    private String instanceField = ReflectionBenchmarkConstants.instanceField;
    private String staticMethod = ReflectionBenchmarkConstants.staticMethod;
    private String instanceMethod = ReflectionBenchmarkConstants.instanceMethod;

    @BeforeClass
    public static void setup() {
        if (REPORT) {
            System.out.println("Running each benchmark " + spins + " times each.");
        }
    }

    // <----------------------------------------benchmarks----------------------------------------->

    @Test
    public void ReflectionConstructorResolutionTest() throws Exception {
        long sample = sameInstanceReflectionConstructorResolution(spins);
        printTime("Resolve constructor", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleConstructorResolutionTest() throws Exception {
        long sample = sameInstanceMethodHandleConstructorResolution(spins);
        printTime("Resolve constructor", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionInstanceMethodResolutionTest() throws Exception {
        long sample = sameInstanceReflectionInstanceMethodResolution(spins);
        printTime("Resolve instance method", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleInstanceMethodResolutionTest() throws Exception {
        long sample = sameInstanceMethodHandleInstanceMethodResolution(spins);
        printTime("Resolve instance method", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionStaticMethodResolutionTest() throws Exception {
        long sample = sameInstanceReflectionStaticMethodResolution(spins);
        printTime("Resolve static method", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleStaticMethodResolutionTest() throws Exception {
        long sample = sameInstanceMethodHandleStaticMethodResolution(spins);
        printTime("Resolve static method", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionInstanceFieldResolutionTest() throws Exception {
        long sample = sameInstanceReflectionInstanceFieldResolution(spins);
        printTime("Resolve instance field", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldResolutionTest() throws Exception {
        long sample = sameInstanceMethodHandleInstanceFieldResolution(spins);
        printTime("Resolve instance field", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionStaticFieldResolutionTest() throws Exception {
        long sample = sameInstanceReflectionStaticFieldResolution(spins);
        printTime("Resolve static field", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldResolutionTest() throws Exception {
        long sample = sameInstanceMethodHandleStaticFieldResolution(spins);
        printTime("Resolve static field", "MethodHandle", sample, spins);
    }

    // <-----------------------------------------the logic----------------------------------------->

    private long sameInstanceReflectionStaticFieldResolution(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            classes[i].getField(staticField);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionInstanceFieldResolution(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            classes[i].getField(instanceField);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionStaticMethodResolution(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            classes[i].getMethod(staticMethod, Number.class, Random.class);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionInstanceMethodResolution(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            classes[i].getMethod(instanceMethod, Integer.class, Object.class, float.class);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionConstructorResolution(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            classes[i].getConstructor(String.class, Object.class, Character.class, Float[].class);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticFieldResolution(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandles.lookup().findStaticGetter(classes[i], staticField, Boolean.class);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceFieldResolution(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandles.lookup().findGetter(classes[i], instanceField, Object.class);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticMethodResolution(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandles.lookup().findStatic(
                classes[i],
                staticMethod,
                MethodType.methodType(Object.class, Number.class, Random.class));
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceMethodResolution(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandles.lookup().findVirtual(
                classes[i],
                instanceMethod,
                MethodType.methodType(void.class, Integer.class, Object.class, float.class));
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleConstructorResolution(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandles.lookup().findConstructor(
                classes[i],
                MethodType.methodType(void.class, String.class, Object.class, Character.class, Float[].class));
        }
        long end = System.nanoTime();
        return end - start;
    }

    private static void printTime(String title, String measureName, long measure, long invokes) {
        BigDecimal measure1BD = BigDecimal.valueOf(measure).setScale(2, RoundingMode.HALF_UP);
        BigDecimal scaledMeasure1 = measure1BD.divide(BigDecimal.valueOf(invokes), RoundingMode.HALF_UP);
        if (REPORT) {
            System.out.println(title + " " + "\n\t" + measureName + ": " + scaledMeasure1.toPlainString() + "ns\n");
        }
    }
}
