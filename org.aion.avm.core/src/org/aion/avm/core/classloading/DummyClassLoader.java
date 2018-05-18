package org.aion.avm.core.classloading;

import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.rt.BlockchainRuntime;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DummyClassLoader extends ClassLoader {

    private static final Set<String> WHITELISTED_PACKAGES = new HashSet<>();

    static {
        WHITELISTED_PACKAGES.add("org.aion.avm.arraywrapper.");
        WHITELISTED_PACKAGES.add("org.aion.avm.exceptionwrapper.");
        WHITELISTED_PACKAGES.add("org.aion.avm.internal.");
        WHITELISTED_PACKAGES.add("org.aion.avm.java.lang.");
        WHITELISTED_PACKAGES.add("org.aion.avm.rt.");
    }

    private Map<String, byte[]> classes;

    public DummyClassLoader(Map<String, byte[]> classes) {
        this.classes = classes;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {

        System.out.println(name);

        if (WHITELISTED_PACKAGES.stream().anyMatch(k -> name.startsWith(k))) {

            // runtime classes
            return super.loadClass(name);

        } else if (classes.containsKey(name)) {

            // user-defined classes
            byte[] bytecode = classes.get(name);
            return defineClass(name, bytecode, 0, bytecode.length);

        } else {
            throw new ClassNotFoundException();
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        // read class file
        String className = "org.aion.avm.examples.helloworld.HelloWorld";
        String classFile = "/home/yulong/workspace/aion_vm/out/production/org.aion.avm.examples/org/aion/avm/examples/helloworld/HelloWorld.class";
        byte[] classBytecode = new FileInputStream(classFile).readAllBytes();

        // transform
        ClassReader cr = new ClassReader(classBytecode);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassShadowing cs = new ClassShadowing(cw, "org/aion/avm/internal/Helper");
        cr.accept(cs, ClassReader.SKIP_DEBUG);
        classBytecode = cw.toByteArray();

        // construct class loader
        Map<String, byte[]> classes = new HashMap<>();
        classes.put(className, classBytecode);
        DummyClassLoader classLoader = new DummyClassLoader(classes);

        // load class
        Class<?> clazz = classLoader.loadClass(className);
        Object obj = clazz.getConstructor().newInstance();

        // invoke run method
        Method method = clazz.getMethod("run", byte[].class, BlockchainRuntime.class);
        Object ret = method.invoke(obj, new byte[0], new BlockchainRuntime() {
            @Override
            public byte[] getSender() {
                return new byte[0];
            }

            @Override
            public byte[] getAddress() {
                return new byte[0];
            }

            @Override
            public long getEnergyLimit() {
                return 1000000;
            }

            @Override
            public byte[] getStorage(byte[] key) {
                return new byte[0];
            }

            @Override
            public void putStorage(byte[] key, byte[] value) {
            }
        });
        System.out.println(new String((byte[])ret));
    }
}