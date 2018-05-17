package org.aion.avm.core.arraywrapping;

import org.aion.avm.core.TestClassLoader;
import org.aion.avm.core.shadowing.TestResource;
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

            ArrayWrapping cs = new ArrayWrapping(out, null);
            in.accept(cs, ClassReader.SKIP_DEBUG);

            byte[] transformed = out.toByteArray();
            writeBytesToFile(transformed, "output.class");
            return transformed;
        });
        Class<?> clazz = loader.loadClass(name);
        Object obj = clazz.getConstructor().newInstance();

        Method method = clazz.getMethod("increaseFirstElement", byte[].class);
        Object ret = method.invoke(obj, new byte[]{1});
        logger.info("Return: {}", ret);
    }


}
