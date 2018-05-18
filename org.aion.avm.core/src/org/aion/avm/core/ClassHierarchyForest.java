package org.aion.avm.core;

/**
 * A helper which maintain the class inheritance relations.
 *
 * There is one hierarchy forest struct per each DApp; and the forest may include multiple trees.
 * The hierarchy forest is to record all the inheritance relationships of the DApp's classes, but not the ones of the runtime
 * or java.lang.* ones. However, some DApp classes can have a parent class that is one of runtime or java.lang.*. For these
 * classes, it is still needed to record their parents in this hierarchy.
 * Because of that, after the hierarchy of a DApp is built, it should contain one or several trees; each tree has a root
 * node representing a class of the runtime or java.lang.*; and besides the root node, all other node in the tree should
 * represent a DApp class.
 */
public final class ClassHierarchyForest extends Forest<String, byte[]> {

    public static ClassHierarchyForest createForest() throws Exception {
        return new ClassHierarchyForest();
    }

    private ClassHierarchyForest() {
    }
}