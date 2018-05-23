package org.aion.avm.core.classloading;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.shadowing.ClassShadowing;
import org.aion.avm.rt.BlockchainRuntime;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

public class AvmClassLoader extends ClassLoader {

    private static final Set<String> WHITELISTED_PACKAGES = new HashSet<>();

    static {
        WHITELISTED_PACKAGES.add("org.aion.avm.arraywrapper.");
        WHITELISTED_PACKAGES.add("org.aion.avm.exceptionwrapper.");
        WHITELISTED_PACKAGES.add("org.aion.avm.internal.");
        WHITELISTED_PACKAGES.add("org.aion.avm.java.lang.");
        WHITELISTED_PACKAGES.add("org.aion.avm.rt.");
    }

    private Map<String, byte[]> classes;
    private List<Function<String, byte[]>> handlers;

    /**
     * Constructs a new AVM class loader.
     *
     * @param classes the transformed bytecode
     * @param handlers a list of handlers which can generate byte code for the given name.
     */
    public AvmClassLoader(Map<String, byte[]> classes, List<Function<String, byte[]>> handlers) {
        this.classes = classes;
        this.handlers = handlers;
    }

    public AvmClassLoader(Map<String, byte[]> classes) {
        this(classes, Collections.emptyList());
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

            // dynamically generated classes
            for (Function<String, byte[]> handler : handlers) {
                byte[] code = handler.apply(name);
                if (code != null) {
                    return defineClass(name, code, 0, code.length);
                }
            }

            throw new ClassNotFoundException();
        }
    }
}