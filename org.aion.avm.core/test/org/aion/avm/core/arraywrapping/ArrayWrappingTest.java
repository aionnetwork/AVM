package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.TestClassLoader;
import org.aion.avm.core.shadowing.TestResource;
import org.aion.avm.arraywrapper.IntArray;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ArrayWrappingTest {

    private Logger logger = LoggerFactory.getLogger(ArrayWrappingTest.class);

    private static void writeBytesToFile(byte[] bytes, String file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testArrayWrapping() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String name = "org.aion.avm.core.arraywrapping.TestResource";
        TestClassLoader loader = new TestClassLoader(TestResource.class.getClassLoader(), name, (inputBytes) -> {
            ClassReader in = new ClassReader(inputBytes);
            ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

            ArrayWrapping cs = new ArrayWrapping(out, Testing.CLASS_NAME);
            in.accept(cs, ClassReader.SKIP_DEBUG);

            byte[] transformed = out.toByteArray();
            writeBytesToFile(transformed, "/tmp/output.class");
            return transformed;
        });
        Class<?> clazz = loader.loadClass(name);
        Object obj = clazz.getConstructor().newInstance();

        Method method = clazz.getMethod("increaseFirstElement");
        Object ret = method.invoke(obj);
        logger.info("Return: {}", ret);
    }


    public static class Testing {
        public static String CLASS_NAME = ArrayWrappingTest.class.getCanonicalName().replaceAll("\\.", "/") + "$Testing";

        public static IntArray newIntArray(int size) {
            return new IntArray(new int[size]);
        }
    }
}
