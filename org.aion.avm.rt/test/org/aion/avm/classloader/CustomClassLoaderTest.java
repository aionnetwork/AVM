package org.aion.avm.classloader;

public class CustomClassLoaderTest {

    public static void main(String[] args) throws Exception {
        CustomClassLoader loader = new CustomClassLoader(CustomClassLoaderTest.class.getClassLoader());
        Class<?> clazz = loader.loadClass("org.aion.avm.classloader.App");
        clazz.getMethod("main", new Class[]{String[].class}).invoke(null, (Object) args);
    }
}
