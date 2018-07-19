package org.aion.avm.core.miscvisitors;

import org.junit.Assert;


/**
 * A utility to load a single class from a one-off loader.
 */
public class SingleLoader extends ClassLoader {
    public static Class<?> loadClass(String name, byte[] bytecode) throws ClassNotFoundException {
        SingleLoader loader = new SingleLoader(name, bytecode);
        Class<?> clazz = Class.forName(name, true, loader);
        Assert.assertNotNull(clazz);
        Assert.assertEquals(loader, clazz.getClassLoader());
        return clazz;
    }
    
    
    private final String name;
    private final byte[] bytecode;
    
    public SingleLoader(String name, byte[] bytecode) {
        this.name = name;
        this.bytecode = bytecode;
    }
    
    // NOTE:  We override loadClass, instead of findClass, since we want to force this class loader to get the first crack at loading.
    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = null;
        if (this.name.equals(name)) {
            clazz = this.defineClass(this.name, this.bytecode, 0, this.bytecode.length);
            if (resolve) {
                this.resolveClass(clazz);
            }
        } else {
            clazz = super.loadClass(name, resolve);
        }
        return clazz;
    }
}
