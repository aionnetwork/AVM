package org.aion.avm.core.persistence;


/**
 * The data required to describe a Class reference, in the serialization layer.
 */
public class ClassNode implements INode {
    public final String className;

    public ClassNode(String className) {
        this.className = className;
    }

    public String getClassName() {
        return this.className;
    }
}
