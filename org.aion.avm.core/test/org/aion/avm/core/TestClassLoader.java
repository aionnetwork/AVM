package org.aion.avm.core;

import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

/**
 * We use this classloader, within the test, to get the raw bytes of the test we want to modify and then pass
 * into the ClassRewriter, for the test.
 */
public class TestClassLoader extends ClassLoader {
    private final String classNameToProvide;
    private final Function<byte[], byte[]> loadHandler;

    public TestClassLoader(ClassLoader parent, String classNameToProvide, Function<byte[], byte[]> loadHandler) {
        super(parent);
        this.classNameToProvide = classNameToProvide;
        this.loadHandler = loadHandler;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> result = null;
        if (this.classNameToProvide.equals(name)) {
            InputStream stream = getParent().getResourceAsStream(name.replaceAll("\\.", "/") + ".class");
            byte[] raw = null;
            try {
                raw = stream.readAllBytes();
            } catch (IOException e) {
                e.printStackTrace();
                Assert.fail();
            }
            byte[] rewrittten = this.loadHandler.apply(raw);
            result = defineClass(name, rewrittten, 0, rewrittten.length);
        } else {
            result = getParent().loadClass(name);
        }
        return result;
    }
}
