package org.aion.avm.core.stacktracking;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.OutOfStackException;
import org.aion.avm.internal.Helper.StackWatcher;
import org.junit.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public class StackWatcherTest {
    private static AvmSharedClassLoader sharedClassLoader;

    @BeforeClass
    public static void setupClass() {
        sharedClassLoader = new AvmSharedClassLoader(CommonGenerators.generateShadowJDK());
    }

    private Class<?> clazz;

    @Before
    // We only need to load the instrumented class once.
    public void getInstrumentedClass() throws ClassNotFoundException {
        String className = TestResource.class.getName();
        byte[] raw = Helpers.loadRequiredResourceAsBytes(className.replaceAll("\\.", "/") + ".class");
        Function<byte[], byte[]> transformer = (inputBytes) -> {
            byte[] transformed = new ClassToolchain.Builder(inputBytes, ClassReader.EXPAND_FRAMES)
                    .addNextVisitor(new StackWatcherClassAdapter())
                    .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES))
                    .build()
                    .runAndGetBytecode();
            return transformed;
        };
        Map<String, byte[]> classes = new HashMap<>();
        classes.put(className, transformer.apply(raw));
        AvmClassLoader loader = new AvmClassLoader(sharedClassLoader, classes);
        clazz = loader.loadClass(className);
    }

    @After
    public void clearTestingState() {
        Helper.clearTestingState();
    }

    @Test
    public void testDepthOverflow() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        StackWatcher.reset();
        StackWatcher.setPolicy(StackWatcher.POLICY_DEPTH);
        StackWatcher.setMaxStackDepth(500);
        StackWatcher.setMaxStackSize(20000);

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testStackOverflow");

        try{
            method.invoke(obj);
            Assert.fail();
        }catch(InvocationTargetException e){
            Assert.assertTrue(e.getCause() instanceof OutOfStackException);
        }
    }

    @Test
    public void testSizeOverflow() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        StackWatcher.reset();
        StackWatcher.setPolicy(StackWatcher.POLICY_SIZE);
        StackWatcher.setMaxStackDepth(500);
        StackWatcher.setMaxStackSize(20000);

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testStackOverflow");

        try{
            method.invoke(obj);
            Assert.fail();
        }catch(InvocationTargetException e){
            Assert.assertTrue(e.getCause() instanceof OutOfStackException);
        }
    }

    @Test
    public void testStackOverflowConsistency() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        StackWatcher.reset();
        StackWatcher.setPolicy(StackWatcher.POLICY_SIZE);
        StackWatcher.setMaxStackDepth(600);
        StackWatcher.setMaxStackSize(20000);

        Object obj;
        Method method;
        Field counter;
        Object ret;
        Boolean expectedError;
        Object prev = -1;
        Object cur = -1;

        for (int i = 0; i < 50; i++){
            StackWatcher.reset();
            Helper.clearTestingState();
            obj = clazz.getConstructor().newInstance();
            method = clazz.getMethod("testStackOverflowConsistency");
            counter = clazz.getDeclaredField("upCounter");
            try{
                ret = method.invoke(obj);
            }catch(InvocationTargetException e){
                Assert.assertTrue(e.getCause() instanceof OutOfStackException);
                cur = counter.get(obj);
                if ((int)prev != -1){
                    Assert.assertEquals(cur, prev);
                }else{
                    prev = cur;
                }
            }
        }

        prev = -1;
        cur = -1;
        StackWatcher.setPolicy(StackWatcher.POLICY_DEPTH);
        for (int i = 0; i < 50; i++){
            StackWatcher.reset();
            Helper.clearTestingState();
            obj = clazz.getConstructor().newInstance();
            method = clazz.getMethod("testStackOverflowConsistency");
            counter = clazz.getDeclaredField("upCounter");
            try{
                ret = method.invoke(obj);
            }catch(InvocationTargetException e){
                Assert.assertTrue(e.getCause() instanceof OutOfStackException);
                cur = counter.get(obj);
                if ((int)prev != -1){
                    Assert.assertEquals(cur, prev);
                }else{
                    prev = cur;
                }
            }
        }
    }

    @Test
    public void testStackTrackingConsistency() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        StackWatcher.reset();
        StackWatcher.setPolicy(StackWatcher.POLICY_SIZE | StackWatcher.POLICY_DEPTH);
        StackWatcher.setMaxStackDepth(200);
        StackWatcher.setMaxStackSize(20000);

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testStackTrackingConsistency");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }

    @Test
    public void testLocalTryCatch() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        StackWatcher.reset();
        StackWatcher.setPolicy(StackWatcher.POLICY_SIZE | StackWatcher.POLICY_DEPTH);
        StackWatcher.setMaxStackDepth(200);
        StackWatcher.setMaxStackSize(20000);

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testLocalTryCatch");

        method.invoke(obj);
        //Assert.assertEquals(ret, true);
    }

    @Test
    public void testRemoteTryCatch() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        StackWatcher.reset();
        StackWatcher.setPolicy(StackWatcher.POLICY_SIZE | StackWatcher.POLICY_DEPTH);
        StackWatcher.setMaxStackDepth(200);
        StackWatcher.setMaxStackSize(20000);

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testRemoteTryCatch");

        Object ret = method.invoke(obj);
        Assert.assertEquals(ret, true);
    }
}
