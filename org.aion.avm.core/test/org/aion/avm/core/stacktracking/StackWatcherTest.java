package org.aion.avm.core.stacktracking;

import org.aion.avm.core.TestClassLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.commons.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StackWatcherTest {
    private Class<?> clazz;

    private static void writeBytesToFile(byte[] bytes, String file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void getInstructmentedClass()throws IOException, ClassNotFoundException{
        String name = "org.aion.avm.core.stacktracking.TestResource";
        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), name, (inputBytes) -> {
            ClassReader in = new ClassReader(inputBytes);
            ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

            //ClassShadowing cs = new ClassShadowing(out, Testing.CLASS_NAME);

            StackWatcherClassAdapter swc = new StackWatcherClassAdapter(out);
            in.accept(swc, ClassReader.EXPAND_FRAMES);

            byte[] transformed = out.toByteArray();
            writeBytesToFile(transformed, "output.class");
            return transformed;
        });

        clazz = loader.loadClass(name);
    }



    @Test
    public void testDepthOverflow() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        StackWatcher.reset();
        StackWatcher.setPolicy(StackWatcher.POLICY_DEPTH);
        StackWatcher.setMaxStackDepth(200);
        StackWatcher.setMaxStackSize(20000);

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testStackOverflow");

        try{
            Object ret = method.invoke(obj);
        }catch(InvocationTargetException e){
            Boolean expectedError =  e.getCause().getMessage().contains("AVM stack overflow") ? true : false;
            Assert.assertEquals(expectedError, true);
        }
    }

    @Test
    public void testSizeOverflow() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        StackWatcher.reset();
        StackWatcher.setPolicy(StackWatcher.POLICY_SIZE);
        StackWatcher.setMaxStackDepth(200);
        StackWatcher.setMaxStackSize(20000);

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testStackOverflow");

        try{
            Object ret = method.invoke(obj);
        }catch(InvocationTargetException e){
            Boolean expectedError =  e.getCause().getMessage().contains("AVM stack overflow") ? true : false;
            //System.out.println(StackWatcher.getCurStackDepth());
            //System.out.println(StackWatcher.getCurStackSize());
            Assert.assertEquals(expectedError, true);
        }
    }

    @Test
    public void testStackTrackingConsistency() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
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
    public void testLocalTryCatch() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        StackWatcher.reset();
        StackWatcher.setPolicy(StackWatcher.POLICY_SIZE | StackWatcher.POLICY_DEPTH);
        StackWatcher.setMaxStackDepth(200);
        StackWatcher.setMaxStackSize(20000);

        Object obj = clazz.getConstructor().newInstance();
        Method method = clazz.getMethod("testLocalTryCatch");

        Object ret = method.invoke(obj);
        //Assert.assertEquals(ret, true);
    }

    @Test
    public void testRemoteTryCatch() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
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
