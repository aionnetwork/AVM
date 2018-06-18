package org.aion.avm.core;

import org.aion.avm.core.util.Assert;


/**
 * Just a utility to simplify interactions with ClassHierarchyForest since most consumers only need to resolve parent names, not know the rest
 * of the data.
 * NOTE:  The class names here are the ".-style"
 * TODO:  This will be where we impose user-class renaming for issue-96.
 */
public class ParentPointers {
    private final Forest<String, byte[]> classHierarchy;
    public ParentPointers(Forest<String, byte[]> classHierarchy) {
        this.classHierarchy = classHierarchy;
    }

    public String getSuperClassName(String className) {
        // NOTE:  These are ".-style" names.
        Assert.assertTrue(-1 == className.indexOf("/"));
        String superClassName = null;
        Forest.Node<String, byte[]> node = classHierarchy.getNodeById(className);
        if (null != node) {
            Forest.Node<String, byte[]> parentNode = node.getParent();
            if (null != parentNode) {
                superClassName = parentNode.getId();
            }
        }
        return superClassName;
    }
}
