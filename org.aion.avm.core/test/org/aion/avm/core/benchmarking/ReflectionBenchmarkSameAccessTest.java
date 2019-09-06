package org.aion.avm.core.benchmarking;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
public class ReflectionBenchmarkSameAccessTest {
    // NOTE:  Output is ONLY produced if REPORT is set to true.
    private static final boolean REPORT = false;
    private static int spins = ReflectionBenchmarkConstants.sameSpins;
    private static String targetClassName = ReflectionBenchmarkConstants.targetClassName;

    private static String staticField = ReflectionBenchmarkConstants.staticField;
    private static String instanceField = ReflectionBenchmarkConstants.instanceField;
    private static String staticMethod = ReflectionBenchmarkConstants.staticMethod;
    private static String instanceMethod = ReflectionBenchmarkConstants.instanceMethod;

    private static final MethodHandle STATIC_FIELD_WRITE, STATIC_FIELD_READ, INSTANCE_FIELD_WRITE,
        INSTANCE_FIELD_READ, STATIC_METHOD, INSTANCE_METHOD, CONSTRUCTOR;

    static {
        try {
            STATIC_FIELD_WRITE = MethodHandles.lookup().findStaticSetter(
                ReflectionTarget.class,
                staticField,
                Boolean.class);
            STATIC_FIELD_READ = MethodHandles.lookup().findStaticGetter(
                ReflectionTarget.class,
                staticField,
                Boolean.class);
            INSTANCE_FIELD_WRITE = MethodHandles.lookup().findSetter(
                ReflectionTarget.class,
                instanceField,
                Object.class);
            INSTANCE_FIELD_READ = MethodHandles.lookup().findGetter(
                ReflectionTarget.class,
                instanceField,
                Object.class);
            STATIC_METHOD = MethodHandles.lookup().findStatic(
                ReflectionTarget.class,
                staticMethod,
                MethodType.methodType(Object.class, Number.class, Random.class));
            INSTANCE_METHOD = MethodHandles.lookup().findVirtual(
                ReflectionTarget.class,
                instanceMethod,
                MethodType.methodType(void.class, Integer.class, Object.class, float.class));
            CONSTRUCTOR = MethodHandles.lookup().findConstructor(
                ReflectionTarget.class,
                MethodType.methodType(void.class, String.class, Object.class, Character.class, Float[].class));
        } catch (Exception e) {
            throw new ExceptionInInitializerError();
        }
    }

    private void warmup() throws Exception {
        this.getClass().getClassLoader().loadClass(targetClassName);
    }

    @BeforeClass
    public static void setup() {
        if (REPORT) {
            System.out.println("Running each benchmark " + spins + " times each.");
        }
    }

    // <----------------------------------------benchmarks----------------------------------------->

    @Test
    public void MethodHandleConstructorInlineTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleConstructorInline(spins);
        printTime("Constructor invokeExact (inline)", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionConstructorAccessTest() throws Throwable {
        warmup();
        long sample = sameInstanceReflectionConstructorAccess(spins);
        printTime("Constructor resolve & call", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleConstructorAccessTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleConstructorAccess(spins);
        printTime("Constructor resolve & invoke", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleConstructorAccessExactTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleConstructorAccessExact(spins);
        printTime("Constructor resolve & invokeExact", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionConstructorAccessInvokeOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceReflectionConstructorAccessInvokeOnly(spins);
        printTime("Constructor call", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleConstructorAccessInvokeOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleConstructorAccessInvokeOnly(spins);
        printTime("Constructor invoke", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleConstructorAccessInvokeExactOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleConstructorAccessInvokeExactOnly(spins);
        printTime("Constructor invokeExact", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionInstanceMethodAccessTest() throws Throwable {
        warmup();
        long sample = sameInstanceReflectionInstanceMethodAccess(spins);
        printTime("Instance method resolve & call", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleInstanceMethodAccessTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleInstanceMethodAccess(spins);
        printTime("Instance method resolve & invoke", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleInstanceMethodAccessExactTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleInstanceMethodAccessExact(spins);
        printTime("Instance method resolve & invokeExact", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleInstanceMethodAccessInlineTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleInstanceMethodInline(spins);
        printTime("Instance method invokeExact (inline)", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionInstanceMethodAccessInvokeOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceReflectionInstanceMethodAccessInvokeOnly(spins);
        printTime("Instance method call", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleInstanceMethodAccessInvokeExactOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleInstanceMethodAccessInvokeExactOnly(spins);
        printTime("Instance method invokeExact", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleInstanceMethodAccessInvokeOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleInstanceMethodAccessInvokeOnly(spins);
        printTime("Instance method invoke", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionStaticMethodAccessTest() throws Throwable {
        warmup();
        long sample = sameInstanceReflectionStaticMethodAccess(spins);
        printTime("Static method resolve & call", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleStaticMethodInlineTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleStaticMethodInline(spins);
        printTime("Static method invokeExact (inline)", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleStaticMethodAccessTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleStaticMethodAccess(spins);
        printTime("Static method resolve & invoke", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleStaticMethodAccessExactTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleStaticMethodAccessExact(spins);
        printTime("Static method resolve & invokeExact", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionStaticMethodAccessInvokeOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceReflectionStaticMethodAccessInvokeOnly(spins);
        printTime("Static method call", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleStaticMethodAccessInvokeOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleStaticMethodAccessInvokeOnly(spins);
        printTime("Static method invoke", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleStaticMethodAccessInvokeExactOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleStaticMethodAccessInvokeExactOnly(spins);
        printTime("Static method invokeExact", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionInstanceFieldReadTest() throws Throwable {
        warmup();
        long sample = sameInstanceReflectionInstanceFieldReadAccess(spins);
        printTime("Instance field resolve & read", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldReadInlineTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleInstanceFieldReadInline(spins);
        printTime("Instance field read (inline)", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldReadTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleInstanceFieldReadAccess(spins);
        printTime("Instance field resolve & read", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldReadExactTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleInstanceFieldReadAccessExact(spins);
        printTime("Instance field resolve & read (invokeExact)", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionInstanceFieldReadOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceReflectionInstanceFieldReadAccessInvokeOnly(spins);
        printTime("Instance field read", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldReadOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleInstanceFieldReadAccessInvokeOnly(spins);
        printTime("Instance field read", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldReadExactOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleInstanceFieldReadAccessInvokeExactOnly(spins);
        printTime("Instance field read (invokeExact)", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionInstanceFieldWriteTest() throws Throwable {
        warmup();
        long sample = sameInstanceReflectionInstanceFieldWriteAccess(spins);
        printTime("Instance field resolve & write", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldWriteInlineTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleInstanceFieldWriteInline(spins);
        printTime("Instance field write (inline)", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldWriteTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleInstanceFieldWriteAccess(spins);
        printTime("Instance field resolve & write", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldWriteExactTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleInstanceFieldWriteAccessExact(spins);
        printTime("Instance field resolve & write (invokeExact)", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionInstanceFieldWriteInvokeOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceReflectionInstanceFieldWriteAccessInvokeOnly(spins);
        printTime("Instance field write", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldWriteInvokeOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleInstanceFieldWriteAccessInvokeOnly(spins);
        printTime("Instance field write", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleInstanceFieldWriteInvokeExactOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleInstanceFieldWriteAccessInvokeExactOnly(spins);
        printTime("Instance field write (invokeExact)", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionStaticFieldReadTest() throws Throwable {
        warmup();
        long sample = sameInstanceReflectionStaticFieldReadAccess(spins);
        printTime("Static field resolve & read", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldReadInlineTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleStaticFieldReadInline(spins);
        printTime("Static field read (inline)", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldReadAccessTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleStaticFieldReadAccess(spins);
        printTime("Static field resolve & read", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldReadAccessExactTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleStaticFieldReadAccessExact(spins);
        printTime("Static field resolve & read (invokeExact)", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionStaticFieldReadOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceReflectionStaticFieldReadAccessInvokeOnly(spins);
        printTime("Static field read", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldReadOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleStaticFieldReadAccessInvokeOnly(spins);
        printTime("Static field read", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldReadExactOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleStaticFieldReadAccessInvokeExactOnly(spins);
        printTime("Static field read (invokeExact)", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionStaticFieldWriteTest() throws Throwable {
        warmup();
        long sample = sameInstanceReflectionStaticFieldWriteAccess(spins);
        printTime("Static field resolve & write", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldWriteInlineTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleStaticFieldWriteInline(spins);
        printTime("Static field write (inline)", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldWriteTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleStaticFieldWriteAccess(spins);
        printTime("Static field resolve & write", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldWriteExactTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleStaticFieldWriteAccessExact(spins);
        printTime("Static field resolve & write (invokeExact)", "MethodHandle", sample, spins);
    }

    @Test
    public void ReflectionStaticFieldWriteInvokeOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceReflectionStaticFieldWriteAccessInvokeOnly(spins);
        printTime("Static field write", "Reflection", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldWriteInvokeOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleStaticFieldWriteAccessInvokeOnly(spins);
        printTime("Static field write", "MethodHandle", sample, spins);
    }

    @Test
    public void MethodHandleStaticFieldWriteInvokeExactOnlyTest() throws Throwable {
        warmup();
        long sample = sameInstanceMethodHandleStaticFieldWriteAccessInvokeExactOnly(spins);
        printTime("Static field write (invokeExact)", "MethodHandle", sample, spins);
    }

    // <-----------------------------------------the logic----------------------------------------->

    private long sameInstanceReflectionStaticFieldWriteAccess(int spins) throws Exception {
        Boolean bool = ReflectionBenchmarkConstants.booleanValue;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Field field = ReflectionTarget.class.getField(staticField);
            field.set(ReflectionTarget.class, bool);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionStaticFieldWriteAccessInvokeOnly(int spins) throws Exception {
        Field field = ReflectionTarget.class.getField(staticField);
        Boolean bool = ReflectionBenchmarkConstants.booleanValue;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            field.set(ReflectionTarget.class, bool);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionStaticFieldReadAccess(int spins) throws Exception {
        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Field field = ReflectionTarget.class.getField(staticField);
            field.get(ReflectionTarget.class);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionStaticFieldReadAccessInvokeOnly(int spins) throws Exception {
        Field field = ReflectionTarget.class.getField(staticField);

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            field.get(ReflectionTarget.class);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionInstanceFieldWriteAccess(int spins) throws Exception {
        ReflectionTarget target = ReflectionTarget.class.getConstructor().newInstance();
        Object value = new Object();

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Field field = ReflectionTarget.class.getField(instanceField);
            field.set(target, value);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionInstanceFieldWriteAccessInvokeOnly(int spins) throws Exception {
        ReflectionTarget target = ReflectionTarget.class.getConstructor().newInstance();
        Field field = ReflectionTarget.class.getField(instanceField);
        Object value = new Object();

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            field.set(target, value);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionInstanceFieldReadAccess(int spins) throws Exception {
        ReflectionTarget target = ReflectionTarget.class.getConstructor().newInstance();

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Field field = ReflectionTarget.class.getField(instanceField);
            field.get(target);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionInstanceFieldReadAccessInvokeOnly(int spins) throws Exception {
        ReflectionTarget target = ReflectionTarget.class.getConstructor().newInstance();
        Field field = ReflectionTarget.class.getField(instanceField);

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            field.get(target);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionConstructorAccess(int spins) throws Exception {
        String string = ReflectionBenchmarkConstants.constructorArg1;
        Object object = ReflectionBenchmarkConstants.constructorArg2;
        Character character = ReflectionBenchmarkConstants.constructorArg3;
        Float[] floats = ReflectionBenchmarkConstants.constructorArg4;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Constructor<ReflectionTarget> constuctor = ReflectionTarget.class.getConstructor(
                String.class,
                Object.class,
                Character.class,
                Float[].class);
            constuctor.newInstance(string, object, character, floats);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionConstructorAccessInvokeOnly(int spins) throws Exception {
        Constructor<ReflectionTarget> constuctor = ReflectionTarget.class.getConstructor(
            String.class,
            Object.class,
            Character.class,
            Float[].class);
        String string = ReflectionBenchmarkConstants.constructorArg1;
        Object object = ReflectionBenchmarkConstants.constructorArg2;
        Character character = ReflectionBenchmarkConstants.constructorArg3;
        Float[] floats = ReflectionBenchmarkConstants.constructorArg4;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            constuctor.newInstance(string, object, character, floats);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionInstanceMethodAccess(int spins) throws Exception {
        ReflectionTarget target = ReflectionTarget.class.getConstructor().newInstance();

        Integer integer = ReflectionBenchmarkConstants.instanceMethodArg1;
        Object object = ReflectionBenchmarkConstants.instanceMethodArg2;
        float f = ReflectionBenchmarkConstants.instanceMethodArg3;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Method method = ReflectionTarget.class.getMethod(instanceMethod, Integer.class, Object.class, float.class);
            method.invoke(target, integer, object, f);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionInstanceMethodAccessInvokeOnly(int spins) throws Exception {
        ReflectionTarget target = ReflectionTarget.class.getConstructor().newInstance();
        Method method = ReflectionTarget.class.getMethod(instanceMethod, Integer.class, Object.class, float.class);

        Integer integer = ReflectionBenchmarkConstants.instanceMethodArg1;
        Object object = ReflectionBenchmarkConstants.instanceMethodArg2;
        float f = ReflectionBenchmarkConstants.instanceMethodArg3;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            method.invoke(target, integer, object, f);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionStaticMethodAccess(int spins) throws Exception {
        Number number = ReflectionBenchmarkConstants.staticMethodArg1;
        Random random = ReflectionBenchmarkConstants.staticMethodArg2;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Method method = ReflectionTarget.class.getMethod(staticMethod, Number.class, Random.class);
            method.invoke(ReflectionTarget.class, number, random);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceReflectionStaticMethodAccessInvokeOnly(int spins) throws Exception {
        Method method = ReflectionTarget.class.getMethod(staticMethod, Number.class, Random.class);
        Number number = ReflectionBenchmarkConstants.staticMethodArg1;
        Random random = ReflectionBenchmarkConstants.staticMethodArg2;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            method.invoke(ReflectionTarget.class, number, random);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticFieldWriteInline(int spins) throws Throwable {
        Boolean bool = ReflectionBenchmarkConstants.booleanValue;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            STATIC_FIELD_WRITE.invokeExact(bool);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticFieldWriteAccessExact(int spins) throws Throwable {
        Boolean bool = ReflectionBenchmarkConstants.booleanValue;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle field = MethodHandles.lookup().findStaticSetter(
                ReflectionTarget.class,
                staticField,
                Boolean.class);
            field.invokeExact(bool);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticFieldWriteAccess(int spins) throws Throwable {
        Boolean bool = ReflectionBenchmarkConstants.booleanValue;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle field = MethodHandles.lookup().findStaticSetter(
                ReflectionTarget.class,
                staticField,
                Boolean.class);
            field.invoke(bool);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticFieldWriteAccessInvokeExactOnly(int spins) throws Throwable {
        MethodHandle field = MethodHandles.lookup().findStaticSetter(
            ReflectionTarget.class,
            staticField, Boolean.class);
        Boolean bool = ReflectionBenchmarkConstants.booleanValue;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            field.invokeExact(bool);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticFieldWriteAccessInvokeOnly(int spins) throws Throwable {
        MethodHandle field = MethodHandles.lookup().findStaticSetter(
            ReflectionTarget.class,
            staticField, Boolean.class);
        Boolean bool = ReflectionBenchmarkConstants.booleanValue;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            field.invoke(bool);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticFieldReadInline(int spins) throws Throwable {
        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Boolean b = (Boolean) STATIC_FIELD_READ.invokeExact();
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticFieldReadAccessExact(int spins) throws Throwable {
        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle field = MethodHandles.lookup().findStaticGetter(
                ReflectionTarget.class,
                staticField,
                Boolean.class);
            Boolean b = (Boolean) field.invokeExact();
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticFieldReadAccess(int spins) throws Throwable {
        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle field = MethodHandles.lookup().findStaticGetter(
                ReflectionTarget.class,
                staticField,
                Boolean.class);
            field.invoke();
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticFieldReadAccessInvokeExactOnly(int spins) throws Throwable {
        MethodHandle field = MethodHandles.lookup().findStaticGetter(
            ReflectionTarget.class,
            staticField,
            Boolean.class);

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Boolean b = (Boolean) field.invokeExact();
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticFieldReadAccessInvokeOnly(int spins) throws Throwable {
        MethodHandle field = MethodHandles.lookup().findStaticGetter(
            ReflectionTarget.class,
            staticField,
            Boolean.class);

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            field.invoke();
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceFieldWriteInline(int spins) throws Throwable {
        MethodHandle constructor = MethodHandles.lookup().findConstructor(
            ReflectionTarget.class,
            MethodType.methodType(void.class));
        ReflectionTarget target = (ReflectionTarget) constructor.invoke();
        Object object = new Object();

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            INSTANCE_FIELD_WRITE.invokeExact(target, object);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceFieldWriteAccessExact(int spins) throws Throwable {
        MethodHandle constructor = MethodHandles.lookup().findConstructor(
            ReflectionTarget.class,
            MethodType.methodType(void.class));
        ReflectionTarget target = (ReflectionTarget) constructor.invoke();
        Object object = new Object();

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle field = MethodHandles.lookup().findSetter(
                ReflectionTarget.class,
                instanceField,
                Object.class);
            field.invokeExact(target, object);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceFieldWriteAccess(int spins) throws Throwable {
        MethodHandle constructor = MethodHandles.lookup().findConstructor(
            ReflectionTarget.class,
            MethodType.methodType(void.class));
        ReflectionTarget target = (ReflectionTarget) constructor.invoke();
        Object object = new Object();

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle field = MethodHandles.lookup().findSetter(
                ReflectionTarget.class,
                instanceField,
                Object.class);
            field.invoke(target, object);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceFieldWriteAccessInvokeExactOnly(int spins) throws Throwable {
        MethodHandle constructor = MethodHandles.lookup().findConstructor(
            ReflectionTarget.class,
            MethodType.methodType(void.class));
        ReflectionTarget target = (ReflectionTarget) constructor.invoke();
        MethodHandle field = MethodHandles.lookup().findSetter(
            ReflectionTarget.class,
            instanceField,
            Object.class);
        Object object = new Object();

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            field.invokeExact(target, object);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceFieldWriteAccessInvokeOnly(int spins) throws Throwable {
        MethodHandle constructor = MethodHandles.lookup().findConstructor(
            ReflectionTarget.class,
            MethodType.methodType(void.class));
        ReflectionTarget target = (ReflectionTarget) constructor.invoke();
        MethodHandle field = MethodHandles.lookup().findSetter(
            ReflectionTarget.class,
            instanceField,
            Object.class);
        Object object = new Object();

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            field.invoke(target, object);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceFieldReadAccessExact(int spins) throws Throwable {
        MethodHandle constructor = MethodHandles.lookup().findConstructor(
            ReflectionTarget.class,
            MethodType.methodType(void.class));
        ReflectionTarget target = (ReflectionTarget) constructor.invoke();

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle field = MethodHandles.lookup().findGetter(
                ReflectionTarget.class,
                instanceField,
                Object.class);
            Object o = field.invokeExact(target);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceFieldReadInline(int spins) throws Throwable {
        MethodHandle constructor = MethodHandles.lookup().findConstructor(
            ReflectionTarget.class,
            MethodType.methodType(void.class));
        ReflectionTarget target = (ReflectionTarget) constructor.invoke();

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            INSTANCE_FIELD_READ.invoke(target);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceFieldReadAccess(int spins) throws Throwable {
        MethodHandle constructor = MethodHandles.lookup().findConstructor(
            ReflectionTarget.class,
            MethodType.methodType(void.class));
        ReflectionTarget target = (ReflectionTarget) constructor.invoke();

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle field = MethodHandles.lookup().findGetter(
                ReflectionTarget.class,
                instanceField,
                Object.class);
            field.invoke(target);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceFieldReadAccessInvokeExactOnly(int spins) throws Throwable {
        MethodHandle constructor = MethodHandles.lookup().findConstructor(
            ReflectionTarget.class,
            MethodType.methodType(void.class));
        ReflectionTarget target = (ReflectionTarget) constructor.invoke();
        MethodHandle field = MethodHandles.lookup().findGetter(
            ReflectionTarget.class,
            instanceField,
            Object.class);

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Object o = field.invokeExact(target);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceFieldReadAccessInvokeOnly(int spins) throws Throwable {
        MethodHandle constructor = MethodHandles.lookup().findConstructor(
            ReflectionTarget.class,
            MethodType.methodType(void.class));
        ReflectionTarget target = (ReflectionTarget) constructor.invoke();
        MethodHandle field = MethodHandles.lookup().findGetter(
            ReflectionTarget.class,
            instanceField,
            Object.class);

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            field.invoke(target);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticMethodAccessExact(int spins) throws Throwable {
        Number number = ReflectionBenchmarkConstants.staticMethodArg1;
        Random random = ReflectionBenchmarkConstants.staticMethodArg2;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle method = MethodHandles.lookup().findStatic(
                ReflectionTarget.class,
                staticMethod,
                MethodType.methodType(Object.class, Number.class, Random.class));
            Object o = method.invokeExact(number, random);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticMethodInline(int spins) throws Throwable {
        Number number = ReflectionBenchmarkConstants.staticMethodArg1;
        Random random = ReflectionBenchmarkConstants.staticMethodArg2;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            STATIC_METHOD.invoke(number, random);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticMethodAccess(int spins) throws Throwable {
        Number number = ReflectionBenchmarkConstants.staticMethodArg1;
        Random random = ReflectionBenchmarkConstants.staticMethodArg2;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle method = MethodHandles.lookup().findStatic(
                ReflectionTarget.class,
                staticMethod,
                MethodType.methodType(Object.class, Number.class, Random.class));
            method.invoke(number, random);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticMethodAccessInvokeExactOnly(int spins) throws Throwable {
        MethodHandle method = MethodHandles.lookup().findStatic(
            ReflectionTarget.class,
            staticMethod,
            MethodType.methodType(Object.class, Number.class, Random.class));
        Number number = ReflectionBenchmarkConstants.staticMethodArg1;
        Random random = ReflectionBenchmarkConstants.staticMethodArg2;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            Object o = method.invokeExact(number, random);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleStaticMethodAccessInvokeOnly(int spins) throws Throwable {
        MethodHandle method = MethodHandles.lookup().findStatic(
            ReflectionTarget.class,
            staticMethod,
            MethodType.methodType(Object.class, Number.class, Random.class));
        Number number = ReflectionBenchmarkConstants.staticMethodArg1;
        Random random = ReflectionBenchmarkConstants.staticMethodArg2;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            method.invoke(number, random);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceMethodAccessExact(int spins) throws Throwable {
        MethodHandle constructor = MethodHandles.lookup().findConstructor(
            ReflectionTarget.class,
            MethodType.methodType(void.class));
        ReflectionTarget target = (ReflectionTarget) constructor.invoke();

        Integer integer = ReflectionBenchmarkConstants.instanceMethodArg1;
        Object object = ReflectionBenchmarkConstants.instanceMethodArg2;
        float f = ReflectionBenchmarkConstants.instanceMethodArg3;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle method = MethodHandles.lookup().findVirtual(
                ReflectionTarget.class,
                instanceMethod,
                MethodType.methodType(void.class, Integer.class, Object.class, float.class));
            method.invokeExact(target, integer, object, f);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceMethodInline(int spins) throws Throwable {
        MethodHandle constructor = MethodHandles.lookup().findConstructor(
            ReflectionTarget.class,
            MethodType.methodType(void.class));
        ReflectionTarget target = (ReflectionTarget) constructor.invoke();

        Integer integer = ReflectionBenchmarkConstants.instanceMethodArg1;
        Object object = ReflectionBenchmarkConstants.instanceMethodArg2;
        float f = ReflectionBenchmarkConstants.instanceMethodArg3;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            INSTANCE_METHOD.invoke(target, integer, object, f);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceMethodAccess(int spins) throws Throwable {
        MethodHandle constructor = MethodHandles.lookup().findConstructor(
            ReflectionTarget.class,
            MethodType.methodType(void.class));
        ReflectionTarget target = (ReflectionTarget) constructor.invoke();

        Integer integer = ReflectionBenchmarkConstants.instanceMethodArg1;
        Object object = ReflectionBenchmarkConstants.instanceMethodArg2;
        float f = ReflectionBenchmarkConstants.instanceMethodArg3;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle method = MethodHandles.lookup().findVirtual(
                ReflectionTarget.class,
                instanceMethod,
                MethodType.methodType(void.class, Integer.class, Object.class, float.class));
            method.invoke(target, integer, object, f);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceMethodAccessInvokeExactOnly(int spins) throws Throwable {
        MethodHandle constructor = MethodHandles.lookup().findConstructor(ReflectionTarget.class, MethodType.methodType(void.class));
        ReflectionTarget target = (ReflectionTarget) constructor.invoke();
        MethodHandle method = MethodHandles.lookup().findVirtual(
            ReflectionTarget.class,
            instanceMethod,
            MethodType.methodType(void.class, Integer.class, Object.class, float.class));

        Integer integer = ReflectionBenchmarkConstants.instanceMethodArg1;
        Object object = ReflectionBenchmarkConstants.instanceMethodArg2;
        float f = ReflectionBenchmarkConstants.instanceMethodArg3;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            method.invokeExact(target, integer, object, f);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleInstanceMethodAccessInvokeOnly(int spins) throws Throwable {
        MethodHandle constructor = MethodHandles.lookup().findConstructor(ReflectionTarget.class, MethodType.methodType(void.class));
        ReflectionTarget target = (ReflectionTarget) constructor.invoke();
        MethodHandle method = MethodHandles.lookup().findVirtual(
            ReflectionTarget.class,
            instanceMethod,
            MethodType.methodType(void.class, Integer.class, Object.class, float.class));

        Integer integer = ReflectionBenchmarkConstants.instanceMethodArg1;
        Object object = ReflectionBenchmarkConstants.instanceMethodArg2;
        float f = ReflectionBenchmarkConstants.instanceMethodArg3;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            method.invoke(target, integer, object, f);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleConstructorAccessExact(int spins) throws Throwable {
        String string = ReflectionBenchmarkConstants.constructorArg1;
        Object object = ReflectionBenchmarkConstants.constructorArg2;
        Character character = ReflectionBenchmarkConstants.constructorArg3;
        Float[] floats = ReflectionBenchmarkConstants.constructorArg4;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle constructor = MethodHandles.lookup().findConstructor(
                ReflectionTarget.class,
                MethodType.methodType(void.class, String.class, Object.class, Character.class, Float[].class));
            ReflectionTarget t = (ReflectionTarget) constructor.invokeExact(string, object, character, floats);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleConstructorInline(int spins) throws Throwable {
        String string = ReflectionBenchmarkConstants.constructorArg1;
        Object object = ReflectionBenchmarkConstants.constructorArg2;
        Character character = ReflectionBenchmarkConstants.constructorArg3;
        Float[] floats = ReflectionBenchmarkConstants.constructorArg4;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            CONSTRUCTOR.invoke(string, object, character, floats);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleConstructorAccess(int spins) throws Throwable {
        String string = ReflectionBenchmarkConstants.constructorArg1;
        Object object = ReflectionBenchmarkConstants.constructorArg2;
        Character character = ReflectionBenchmarkConstants.constructorArg3;
        Float[] floats = ReflectionBenchmarkConstants.constructorArg4;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            MethodHandle constructor = MethodHandles.lookup().findConstructor(
                ReflectionTarget.class,
                MethodType.methodType(void.class, String.class, Object.class, Character.class, Float[].class));
            constructor.invoke(string, object, character, floats);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleConstructorAccessInvokeExactOnly(int spins) throws Throwable {
        MethodHandle constructor = MethodHandles.lookup().findConstructor(ReflectionTarget.class, MethodType.methodType(
            void.class,
            String.class,
            Object.class,
            Character.class,
            Float[].class));
        String string = ReflectionBenchmarkConstants.constructorArg1;
        Object object = ReflectionBenchmarkConstants.constructorArg2;
        Character character = ReflectionBenchmarkConstants.constructorArg3;
        Float[] floats = ReflectionBenchmarkConstants.constructorArg4;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            ReflectionTarget t = (ReflectionTarget) constructor.invokeExact(string, object, character, floats);
        }
        long end = System.nanoTime();
        return end - start;
    }

    private long sameInstanceMethodHandleConstructorAccessInvokeOnly(int spins) throws Throwable {
        MethodHandle constructor = MethodHandles.lookup().findConstructor(ReflectionTarget.class, MethodType.methodType(
            void.class,
            String.class,
            Object.class,
            Character.class,
            Float[].class));
        String string = ReflectionBenchmarkConstants.constructorArg1;
        Object object = ReflectionBenchmarkConstants.constructorArg2;
        Character character = ReflectionBenchmarkConstants.constructorArg3;
        Float[] floats = ReflectionBenchmarkConstants.constructorArg4;

        long start = System.nanoTime();
        for (int i = 0; i < spins; i++) {
            constructor.invoke(string, object, character, floats);
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