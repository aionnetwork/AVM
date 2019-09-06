package org.aion.avm.core.benchmarking;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
 *       constructor invocation
 *       static method invocations
 *       instance method invocations
 *       static field read/writes
 *       instance field read/writes
 *
 * This benchmark measures these accesses in two ways:
 *   1. Only the access itself (the invocation/read/write) and nothing else.
 *   2. The lookup/resolution as well as the access.
 */
public class ReflectionBenchmarkUniqueAccessTest {
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
    public void ReflectionStaticFieldWriteTest() throws Throwable {
        long sample = uniqueInstanceReflectionStaticFieldWriteAccess(spins);
        printTime("Static field resolve & write", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldWriteTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleStaticFieldWriteAccess(spins);
        printTime("Static field resolve & write", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldWriteExactTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleStaticFieldWriteAccessExact(spins);
        printTime("Static field resolve & write (invokeExact)", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionStaticFieldWriteOnlyTest() throws Throwable {
        long sample = uniqueInstanceReflectionStaticFieldWriteAccessInvokeOnly(spins);
        printTime("Static field write only", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldWriteOnlyTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleStaticFieldWriteAccessInvokeOnly(spins);
        printTime("Static field write only", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldWriteExactOnlyTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleStaticFieldWriteAccessInvokeExactOnly(spins);
        printTime("Static field write only (invokeExact)", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionStaticFieldReadTest() throws Throwable {
        long sample = uniqueInstanceReflectionStaticFieldReadAccess(spins);
        printTime("Static field resolve & read", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldReadTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleStaticFieldReadAccess(spins);
        printTime("Static field resolve & read", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldReadExactTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleStaticFieldReadAccessExact(spins);
        printTime("Static field resolve & read (invokeExact)", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionStaticFieldReadOnlyTest() throws Throwable {
        long sample = uniqueInstanceReflectionStaticFieldReadAccessInvokeOnly(spins);
        printTime("Static field read", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldReadOnlyTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleStaticFieldReadAccessInvokeOnly(spins);
        printTime("Static field read", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldReadExactOnlyTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleStaticFieldReadAccessInvokeExactOnly(spins);
        printTime("Static field read (invokeExact)", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionInstanceFieldWriteTest() throws Throwable {
        long sample = uniqueInstanceReflectionInstanceFieldWriteAccess(spins);
        printTime("Instance field resolve & write", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldWriteTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleInstanceFieldWriteAccess(spins);
        printTime("Instance field resolve & write", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldWriteExactTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleInstanceFieldWriteAccessExact(spins);
        printTime("Instance field resolve & write (invokeExact)", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionInstanceFieldWriteOnlyTest() throws Throwable {
        long sample = uniqueInstanceReflectionInstanceFieldWriteAccessInvokeOnly(spins);
        printTime("Instance field write", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldWriteOnlyTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleInstanceFieldWriteAccessInvokeOnly(spins);
        printTime("Instance field write", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldWriteExactOnlyTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleInstanceFieldWriteAccessInvokeExactOnly(spins);
        printTime("Instance field write (invokeExact)", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionInstanceFieldReadTest() throws Throwable {
        long sample = uniqueInstanceReflectionInstanceFieldReadAccess(spins);
        printTime("Instance field resolve & read", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldReadTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleInstanceFieldReadAccess(spins);
        printTime("Instance field resolve & read", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldReadExactTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleInstanceFieldReadAccessExact(spins);
        printTime("Instance field resolve & read (invokeExact)", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionInstanceFieldReadOnlyTest() throws Throwable {
        long sample = uniqueInstanceReflectionInstanceFieldReadAccessInvokeOnly(spins);
        printTime("Instance field read", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldReadOnlyTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleInstanceFieldReadAccessInvokeOnly(spins);
        printTime("Instance field read", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldReadExactOnlyTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleInstanceFieldReadAccessInvokeExactOnly(spins);
        printTime("Instance field read (invokeExact)", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionStaticMethodAccessTest() throws Throwable {
        long sample = uniqueInstanceReflectionStaticMethodAccess(spins);
        printTime("Static method resolve & call", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleStaticMethodAccessTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleStaticMethodAccess(spins);
        printTime("Static method resolve & invoke", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleStaticMethodAccessExactTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleStaticMethodAccessExact(spins);
        printTime("Static method resolve & invokeExact", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionStaticMethodAccessOnlyTest() throws Throwable {
        long sample = uniqueInstanceReflectionStaticMethodAccessInvokeOnly(spins);
        printTime("Static method invoke", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleStaticMethodAccessOnlyTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleStaticMethodAccessInvokeOnly(spins);
        printTime("Static method invoke", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleStaticMethodAccessExactOnlyTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleStaticMethodAccessInvokeExactOnly(spins);
        printTime("Static method invokeExact", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionInstanceMethodAccessTest() throws Throwable {
        long sample = uniqueInstanceReflectionInstanceMethodAccess(spins);
        printTime("Instance method resolve & call", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleInstanceMethodAccessTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleInstanceMethodAccess(spins);
        printTime("Instance method resolve & invoke", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleInstanceMethodAccessExactTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleInstanceMethodAccessExact(spins);
        printTime("Instance method resolve & invokeExact", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionInstanceMethodAccessOnlyTest() throws Throwable {
        long sample = uniqueInstanceReflectionInstanceMethodAccessInvokeOnly(spins);
        printTime("Instance method call", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleInstanceMethodAccessOnlyTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleInstanceMethodAccessInvokeOnly(spins);
        printTime("Instance method invoke", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleInstanceMethodAccessExactOnlyTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleInstanceMethodAccessInvokeExactOnly(spins);
        printTime("Instance method invokeExact", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionConstructorAccessTest() throws Throwable {
        long sample = uniqueInstanceReflectionConstructorAccess(spins);
        printTime("Constructor resolve & call", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleConstructorAccessTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleConstructorAccess(spins);
        printTime("Constructor resolve & invoke", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleConstructorAccessExactTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleConstructorAccessExact(spins);
        printTime("Constructor resolve & invokeExact", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionConstructorAccessOnlyTest() throws Throwable {
        long sample = uniqueInstanceReflectionConstructorAccessInvokeOnly(spins);
        printTime("Constructor call", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleConstructorAccessOnlyTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleConstructorAccessInvokeOnly(spins);
        printTime("Constructor invoke", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleConstructorAccessExactOnlyTest() throws Throwable {
        long sample = uniqueInstanceMethodHandleConstructorAccessInvokeExactOnly(spins);
        printTime("Constructor invokeExact", "MethodHandle", sample, spins);
    }

    // <-----------------------------------------the logic----------------------------------------->

    private long uniqueInstanceReflectionConstructorAccess(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        String string = ReflectionBenchmarkConstants.constructorArg1;
        Object object = ReflectionBenchmarkConstants.constructorArg2;
        Character character = ReflectionBenchmarkConstants.constructorArg3;
        Float[] floats = ReflectionBenchmarkConstants.constructorArg4;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Constructor<?> constructor = classes[i].getConstructor(
                String.class,
                Object.class,
                Character.class,
                Float[].class);
            constructor.newInstance(string, object, character, floats);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceReflectionConstructorAccessInvokeOnly(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];
        Constructor<?>[] constructors = new Constructor[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
            constructors[i] = classes[i].getConstructor(
                String.class,
                Object.class,
                Character.class,
                Float[].class);
        }

        String string = ReflectionBenchmarkConstants.constructorArg1;
        Object object = ReflectionBenchmarkConstants.constructorArg2;
        Character character = ReflectionBenchmarkConstants.constructorArg3;
        Float[] floats = ReflectionBenchmarkConstants.constructorArg4;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            constructors[i].newInstance(string, object, character, floats);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceReflectionInstanceMethodAccess(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];
        ReflectionTarget[] targets = new ReflectionTarget[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
            targets[i] = (ReflectionTarget) classes[i].getConstructor().newInstance();
        }

        Integer integer = ReflectionBenchmarkConstants.instanceMethodArg1;
        Object object = ReflectionBenchmarkConstants.instanceMethodArg2;
        float f = ReflectionBenchmarkConstants.instanceMethodArg3;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Method method = classes[i].getMethod(instanceMethod, Integer.class, Object.class, float.class);
            method.invoke(targets[i], integer, object, f);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceReflectionInstanceMethodAccessInvokeOnly(int spins) throws Exception {
        ReflectionTarget[] targets = new ReflectionTarget[spins];
        Method[] methods = new Method[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            Class<?> clazz = loader.loadClass(targetClassName);
            targets[i] = (ReflectionTarget) clazz.getConstructor().newInstance();
            methods[i] = clazz.getMethod(instanceMethod, Integer.class, Object.class, float.class);
        }

        Integer integer = ReflectionBenchmarkConstants.instanceMethodArg1;
        Object object = ReflectionBenchmarkConstants.instanceMethodArg2;
        float f = ReflectionBenchmarkConstants.instanceMethodArg3;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            methods[i].invoke(targets[i], integer, object, f);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceReflectionStaticMethodAccess(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        Number number = ReflectionBenchmarkConstants.staticMethodArg1;
        Random random = ReflectionBenchmarkConstants.staticMethodArg2;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Method method = classes[i].getMethod(staticMethod, Number.class, Random.class);
            method.invoke(classes[i], number, random);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceReflectionStaticMethodAccessInvokeOnly(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];
        Method[] methods = new Method[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
            methods[i] = classes[i].getMethod(staticMethod, Number.class, Random.class);
        }

        Number number = ReflectionBenchmarkConstants.staticMethodArg1;
        Random random = ReflectionBenchmarkConstants.staticMethodArg2;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            methods[i].invoke(classes[i], number, random);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceReflectionInstanceFieldReadAccess(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];
        ReflectionTarget[] targets = new ReflectionTarget[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
            targets[i] = (ReflectionTarget) classes[i].getConstructor().newInstance();
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Field field = classes[i].getField(instanceField);
            field.get(targets[i]);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceReflectionInstanceFieldReadAccessInvokeOnly(int spins) throws Exception {
        ReflectionTarget[] targets = new ReflectionTarget[spins];
        Field[] fields = new Field[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            Class<?> clazz = loader.loadClass(targetClassName);
            targets[i] = (ReflectionTarget) clazz.getConstructor().newInstance();
            fields[i] = clazz.getField(instanceField);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            fields[i].get(targets[i]);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceReflectionInstanceFieldWriteAccess(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];
        ReflectionTarget[] targets = new ReflectionTarget[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
            targets[i] = (ReflectionTarget) classes[i].getConstructor().newInstance();
        }

        Object value = new Object();

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Field field = classes[i].getField(instanceField);
            field.set(targets[i], value);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceReflectionInstanceFieldWriteAccessInvokeOnly(int spins) throws Exception {
        ReflectionTarget[] targets = new ReflectionTarget[spins];
        Field[] fields = new Field[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            Class<?> clazz = loader.loadClass(targetClassName);
            targets[i] = (ReflectionTarget) clazz.getConstructor().newInstance();
            fields[i] = clazz.getField(instanceField);
        }

        Object value = new Object();

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            fields[i].set(targets[i], value);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceReflectionStaticFieldReadAccessInvokeOnly(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];
        Field[] fields = new Field[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
            fields[i] = classes[i].getField(staticField);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            fields[i].get(classes[i]);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceReflectionStaticFieldReadAccess(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Field field = classes[i].getField(staticField);
            field.get(classes[i]);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceReflectionStaticFieldWriteAccess(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        Boolean bool = ReflectionBenchmarkConstants.booleanValue;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Field field = classes[i].getField(staticField);
            field.set(classes[i], bool);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceReflectionStaticFieldWriteAccessInvokeOnly(int spins) throws Exception {
        Class<?>[] classes = new Class[spins];
        Field[] fields = new Field[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
            fields[i] = classes[i].getField(staticField);
        }

        Boolean bool = ReflectionBenchmarkConstants.booleanValue;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            fields[i].set(classes[i], bool);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleConstructorAccessExact(int spins) throws Throwable {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        String string = ReflectionBenchmarkConstants.constructorArg1;
        Object object = ReflectionBenchmarkConstants.constructorArg2;
        Character character = ReflectionBenchmarkConstants.constructorArg3;
        Float[] floats = ReflectionBenchmarkConstants.constructorArg4;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle constructor = MethodHandles.lookup().findConstructor(classes[i], MethodType
                .methodType(
                    void.class,
                    String.class,
                    Object.class,
                    Character.class,
                    Float[].class));
            ReflectionTarget t = (ReflectionTarget) constructor.invokeExact(string, object, character, floats);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleConstructorAccess(int spins) throws Throwable {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        String string = ReflectionBenchmarkConstants.constructorArg1;
        Object object = ReflectionBenchmarkConstants.constructorArg2;
        Character character = ReflectionBenchmarkConstants.constructorArg3;
        Float[] floats = ReflectionBenchmarkConstants.constructorArg4;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle constructor = MethodHandles.lookup().findConstructor(classes[i], MethodType
                .methodType(
                    void.class,
                    String.class,
                    Object.class,
                    Character.class,
                    Float[].class));
            constructor.invoke(string, object, character, floats);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleConstructorAccessInvokeExactOnly(int spins) throws Throwable {
        MethodHandle[] constructors = new MethodHandle[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            Class<?> clazz = loader.loadClass(targetClassName);
            constructors[i] = MethodHandles.lookup().findConstructor(clazz, MethodType.methodType(
                void.class,
                String.class,
                Object.class,
                Character.class,
                Float[].class));
        }

        String string = ReflectionBenchmarkConstants.constructorArg1;
        Object object = ReflectionBenchmarkConstants.constructorArg2;
        Character character = ReflectionBenchmarkConstants.constructorArg3;
        Float[] floats = ReflectionBenchmarkConstants.constructorArg4;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            ReflectionTarget t = (ReflectionTarget) constructors[i].invokeExact(string, object, character, floats);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleConstructorAccessInvokeOnly(int spins) throws Throwable {
        MethodHandle[] constructors = new MethodHandle[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            Class<?> clazz = loader.loadClass(targetClassName);
            constructors[i] = MethodHandles.lookup().findConstructor(clazz, MethodType.methodType(
                void.class,
                String.class,
                Object.class,
                Character.class,
                Float[].class));
        }

        String string = ReflectionBenchmarkConstants.constructorArg1;
        Object object = ReflectionBenchmarkConstants.constructorArg2;
        Character character = ReflectionBenchmarkConstants.constructorArg3;
        Float[] floats = ReflectionBenchmarkConstants.constructorArg4;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            constructors[i].invoke(string, object, character, floats);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleInstanceMethodAccessExact(int spins) throws Throwable {
        Class<?>[] classes = new Class[spins];
        ReflectionTarget[] targets = new ReflectionTarget[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
            targets[i] = (ReflectionTarget) MethodHandles.lookup().findConstructor(classes[i], MethodType.methodType(void.class)).invoke();
        }

        Integer integer = ReflectionBenchmarkConstants.instanceMethodArg1;
        Object object = ReflectionBenchmarkConstants.instanceMethodArg2;
        float f = ReflectionBenchmarkConstants.instanceMethodArg3;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle method = MethodHandles.lookup().findVirtual(
                classes[i],
                instanceMethod,
                MethodType.methodType(void.class, Integer.class, Object.class, float.class));
            method.invokeExact(targets[i], integer, object, f);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleInstanceMethodAccess(int spins) throws Throwable {
        Class<?>[] classes = new Class[spins];
        ReflectionTarget[] targets = new ReflectionTarget[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
            targets[i] = (ReflectionTarget) MethodHandles.lookup().findConstructor(classes[i], MethodType.methodType(void.class)).invoke();
        }

        Integer integer = ReflectionBenchmarkConstants.instanceMethodArg1;
        Object object = ReflectionBenchmarkConstants.instanceMethodArg2;
        float f = ReflectionBenchmarkConstants.instanceMethodArg3;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle method = MethodHandles.lookup().findVirtual(
                classes[i],
                instanceMethod,
                MethodType.methodType(void.class, Integer.class, Object.class, float.class));
            method.invoke(targets[i], integer, object, f);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleInstanceMethodAccessInvokeExactOnly(int spins) throws Throwable {
        ReflectionTarget[] targets = new ReflectionTarget[spins];
        MethodHandle[] methods = new MethodHandle[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            Class<?> clazz = loader.loadClass(targetClassName);
            targets[i] = (ReflectionTarget) MethodHandles.lookup().findConstructor(clazz, MethodType.methodType(void.class)).invoke();
            methods[i] = MethodHandles.lookup().findVirtual(
                clazz,
                instanceMethod,
                MethodType.methodType(void.class, Integer.class, Object.class, float.class));
        }

        Integer integer = ReflectionBenchmarkConstants.instanceMethodArg1;
        Object object = ReflectionBenchmarkConstants.instanceMethodArg2;
        float f = ReflectionBenchmarkConstants.instanceMethodArg3;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            methods[i].invokeExact(targets[i], integer, object, f);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleInstanceMethodAccessInvokeOnly(int spins) throws Throwable {
        ReflectionTarget[] targets = new ReflectionTarget[spins];
        MethodHandle[] methods = new MethodHandle[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            Class<?> clazz = loader.loadClass(targetClassName);
            targets[i] = (ReflectionTarget) MethodHandles.lookup().findConstructor(clazz, MethodType.methodType(void.class)).invoke();
            methods[i] = MethodHandles.lookup().findVirtual(
                clazz,
                instanceMethod,
                MethodType.methodType(void.class, Integer.class, Object.class, float.class));
        }

        Integer integer = ReflectionBenchmarkConstants.instanceMethodArg1;
        Object object = ReflectionBenchmarkConstants.instanceMethodArg2;
        float f = ReflectionBenchmarkConstants.instanceMethodArg3;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            methods[i].invoke(targets[i], integer, object, f);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleStaticMethodAccessExact(int spins) throws Throwable {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        Number number = ReflectionBenchmarkConstants.staticMethodArg1;
        Random random = ReflectionBenchmarkConstants.staticMethodArg2;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle method = MethodHandles.lookup().findStatic(
                classes[i],
                staticMethod,
                MethodType.methodType(Object.class, Number.class, Random.class));
            Object o = method.invokeExact(number, random);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleStaticMethodAccess(int spins) throws Throwable {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        Number number = ReflectionBenchmarkConstants.staticMethodArg1;
        Random random = ReflectionBenchmarkConstants.staticMethodArg2;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle method = MethodHandles.lookup().findStatic(
                classes[i],
                staticMethod,
                MethodType.methodType(Object.class, Number.class, Random.class));
            method.invoke(number, random);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleStaticMethodAccessInvokeExactOnly(int spins) throws Throwable {
        MethodHandle[] methods = new MethodHandle[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            Class<?> clazz = loader.loadClass(targetClassName);
            methods[i] = MethodHandles.lookup().findStatic(
                clazz,
                staticMethod,
                MethodType.methodType(Object.class, Number.class, Random.class));
        }

        Number number = ReflectionBenchmarkConstants.staticMethodArg1;
        Random random = ReflectionBenchmarkConstants.staticMethodArg2;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Object o = methods[i].invokeExact(number, random);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleStaticMethodAccessInvokeOnly(int spins) throws Throwable {
        MethodHandle[] methods = new MethodHandle[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            Class<?> clazz = loader.loadClass(targetClassName);
            methods[i] = MethodHandles.lookup().findStatic(
                clazz,
                staticMethod,
                MethodType.methodType(Object.class, Number.class, Random.class));
        }

        Number number = ReflectionBenchmarkConstants.staticMethodArg1;
        Random random = ReflectionBenchmarkConstants.staticMethodArg2;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            methods[i].invoke(number, random);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleInstanceFieldReadAccessExact(int spins) throws Throwable {
        Class<?>[] classes = new Class[spins];
        ReflectionTarget[] targets = new ReflectionTarget[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
            targets[i] = (ReflectionTarget) MethodHandles.lookup().findConstructor(classes[i], MethodType.methodType(void.class)).invoke();
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle field = MethodHandles.lookup().findGetter(classes[i], instanceField, Object.class);
            Object o = field.invokeExact(targets[i]);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleInstanceFieldReadAccess(int spins) throws Throwable {
        Class<?>[] classes = new Class[spins];
        ReflectionTarget[] targets = new ReflectionTarget[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
            targets[i] = (ReflectionTarget) MethodHandles.lookup().findConstructor(classes[i], MethodType.methodType(void.class)).invoke();
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle field = MethodHandles.lookup().findGetter(classes[i], instanceField, Object.class);
            field.invoke(targets[i]);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleInstanceFieldReadAccessInvokeExactOnly(int spins) throws Throwable {
        ReflectionTarget[] targets = new ReflectionTarget[spins];
        MethodHandle[] fields = new MethodHandle[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            Class<?> clazz = loader.loadClass(targetClassName);
            targets[i] = (ReflectionTarget) MethodHandles.lookup().findConstructor(clazz, MethodType.methodType(void.class)).invoke();
            fields[i] = MethodHandles.lookup().findGetter(clazz, instanceField, Object.class);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Object o = fields[i].invokeExact(targets[i]);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleInstanceFieldReadAccessInvokeOnly(int spins) throws Throwable {
        ReflectionTarget[] targets = new ReflectionTarget[spins];
        MethodHandle[] fields = new MethodHandle[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            Class<?> clazz = loader.loadClass(targetClassName);
            targets[i] = (ReflectionTarget) MethodHandles.lookup().findConstructor(clazz, MethodType.methodType(void.class)).invoke();
            fields[i] = MethodHandles.lookup().findGetter(clazz, instanceField, Object.class);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            fields[i].invoke(targets[i]);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleInstanceFieldWriteAccessExact(int spins) throws Throwable {
        Class<?>[] classes = new Class[spins];
        ReflectionTarget[] targets = new ReflectionTarget[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
            targets[i] = (ReflectionTarget) MethodHandles.lookup().findConstructor(classes[i], MethodType.methodType(void.class)).invoke();
        }

        Object object = new Object();

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle field = MethodHandles.lookup().findSetter(classes[i], instanceField, Object.class);
            field.invokeExact(targets[i], object);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleInstanceFieldWriteAccess(int spins) throws Throwable {
        Class<?>[] classes = new Class[spins];
        ReflectionTarget[] targets = new ReflectionTarget[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
            targets[i] = (ReflectionTarget) MethodHandles.lookup().findConstructor(classes[i], MethodType.methodType(void.class)).invoke();
        }

        Object object = new Object();

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle field = MethodHandles.lookup().findSetter(classes[i], instanceField, Object.class);
            field.invoke(targets[i], object);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleInstanceFieldWriteAccessInvokeExactOnly(int spins) throws Throwable {
        ReflectionTarget[] targets = new ReflectionTarget[spins];
        MethodHandle[] fields = new MethodHandle[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            Class<?> clazz = loader.loadClass(targetClassName);
            targets[i] = (ReflectionTarget) MethodHandles.lookup().findConstructor(clazz, MethodType.methodType(void.class)).invoke();
            fields[i] = MethodHandles.lookup().findSetter(clazz, instanceField, Object.class);
        }

        Object object = new Object();

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            fields[i].invokeExact(targets[i], object);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleInstanceFieldWriteAccessInvokeOnly(int spins) throws Throwable {
        ReflectionTarget[] targets = new ReflectionTarget[spins];
        MethodHandle[] fields = new MethodHandle[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            Class<?> clazz = loader.loadClass(targetClassName);
            targets[i] = (ReflectionTarget) MethodHandles.lookup().findConstructor(clazz, MethodType.methodType(void.class)).invoke();
            fields[i] = MethodHandles.lookup().findSetter(clazz, instanceField, Object.class);
        }

        Object object = new Object();

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            fields[i].invoke(targets[i], object);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleStaticFieldReadAccessInvokeExactOnly(int spins) throws Throwable {
        MethodHandle[] fields = new MethodHandle[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            Class<?> clazz = loader.loadClass(targetClassName);
            fields[i] = MethodHandles.lookup().findStaticGetter(clazz, staticField, Boolean.class);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Boolean b = (Boolean) fields[i].invokeExact();
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleStaticFieldReadAccessInvokeOnly(int spins) throws Throwable {
        MethodHandle[] fields = new MethodHandle[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            Class<?> clazz = loader.loadClass(targetClassName);
            fields[i] = MethodHandles.lookup().findStaticGetter(clazz, staticField, Boolean.class);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            fields[i].invoke();
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleStaticFieldReadAccessExact(int spins) throws Throwable {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle field = MethodHandles.lookup().findStaticGetter(classes[i], staticField, Boolean.class);
            Boolean b = (Boolean) field.invokeExact();
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleStaticFieldReadAccess(int spins) throws Throwable {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle field = MethodHandles.lookup().findStaticGetter(classes[i], staticField, Boolean.class);
            field.invoke();
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleStaticFieldWriteAccessExact(int spins) throws Throwable {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        Boolean bool = ReflectionBenchmarkConstants.booleanValue;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle field = MethodHandles.lookup().findStaticSetter(classes[i], staticField, Boolean.class);
            field.invokeExact(bool);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleStaticFieldWriteAccess(int spins) throws Throwable {
        Class<?>[] classes = new Class[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            classes[i] = loader.loadClass(targetClassName);
        }

        Boolean bool = ReflectionBenchmarkConstants.booleanValue;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle field = MethodHandles.lookup().findStaticSetter(classes[i], staticField, Boolean.class);
            field.invoke(bool);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleStaticFieldWriteAccessInvokeExactOnly(int spins) throws Throwable {
        MethodHandle[] fields = new MethodHandle[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            Class<?> clazz = loader.loadClass(targetClassName);
            fields[i] = MethodHandles.lookup().findStaticSetter(clazz, staticField, Boolean.class);
        }

        Boolean bool = ReflectionBenchmarkConstants.booleanValue;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            fields[i].invokeExact(bool);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long uniqueInstanceMethodHandleStaticFieldWriteAccessInvokeOnly(int spins) throws Throwable {
        MethodHandle[] fields = new MethodHandle[spins];

        for (int i = 0; i < spins; i++) {
            ClassLoader loader = new URLClassLoader(new URL[]{ classpathDirectory.toURI().toURL() });
            Class<?> clazz = loader.loadClass(targetClassName);
            fields[i] = MethodHandles.lookup().findStaticSetter(clazz, staticField, Boolean.class);
        }

        Boolean bool = ReflectionBenchmarkConstants.booleanValue;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            fields[i].invoke(bool);
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
