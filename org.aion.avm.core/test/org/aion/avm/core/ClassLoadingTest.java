package org.aion.avm.core;

import org.aion.avm.core.util.Helpers;
import org.junit.Ignore;
import org.junit.Test;

public class ClassLoadingTest {

    private String clazzName = ClassLoadingResource.class.getName();
    private byte[] clazzBytes = Helpers.loadRequiredResourceAsBytes(clazzName.replaceAll("\\.", "/") + ".class");

    /**
     * This test is more like a benchmark, rather than functionality test.
     *
     * Disabled by default to speed up build process.
     */
    @Test
    @Ignore
    public void test() throws InterruptedException {

        for (int i = 0; i < 4; i++) {
            new Thread(() -> {
                while (true) {
                    ClassLoader cl = new ClassLoader() {
                        @Override
                        public Class<?> loadClass(String name) throws ClassNotFoundException {
                            if (name.equals(clazzName)) {
                                return super.defineClass(name, clazzBytes, 0, clazzBytes.length);
                            } else {
                                return super.loadClass(name);
                            }
                        }
                    };
                    try {
                        cl.loadClass(clazzName);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        Thread.sleep(30 * 1000);
    }
}
