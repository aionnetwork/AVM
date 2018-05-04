package org.aion.avm.core;

/**
 * @author Roman Katerinenko
 */
public class AvmClassLoader extends ClassLoader {
    public AvmClassLoader() {
        super("AVM Class loader", ClassLoader.getSystemClassLoader());
    }
}