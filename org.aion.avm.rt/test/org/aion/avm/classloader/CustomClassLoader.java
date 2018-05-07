package org.aion.avm.classloader;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class CustomClassLoader extends ClassLoader {

    public CustomClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        System.out.println("loading class '" + name + "'");

        if (name.startsWith("org.aion.")) {
            return getClass(name);
        }

        return super.loadClass(name);
    }

    private Class<?> getClass(String name) {
        String file = name.replace('.', File.separatorChar) + ".class";
        try {
            byte[] b = loadClassData(file);
            Class<?> c = defineClass(name, b, 0, b.length);
            resolveClass(c);
            return c;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] loadClassData(String fileName) throws IOException {
        File file = new File("./out/test/org.aion.avm.rt", fileName);
        int size = (int) file.length();

        byte buff[] = new byte[size];
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        in.readFully(buff);
        in.close();

        return buff;
    }
}