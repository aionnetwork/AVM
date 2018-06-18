package org.aion.avm.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aion.avm.core.util.Assert;


/**
 * Just a utility to simplify interactions with ClassHierarchyForest since most consumers only need to resolve parent names, not know the rest
 * of the data.
 * NOTE:  The class names here are the ".-style"
 * TODO:  This will be where we impose user-class renaming for issue-96.
 */
public class ParentPointers {
    private final Map<String, String> postRenameParentMap;

    public ParentPointers(Set<String> userDefinedClassNames, Forest<String, byte[]> classHierarchy) {
        // Get every user-defined class, find its parent, and add the pair to the map, while renaming them.
        Map<String, String> mapping = new HashMap<>();
        for (String className : userDefinedClassNames) {
            // NOTE:  These are ".-style" names.
            Assert.assertTrue(-1 == className.indexOf("/"));
            
            Forest.Node<String, byte[]> node = classHierarchy.getNodeById(className);
            String superClassName = node.getParent().getId();
            
            mapping.put(className, superClassName);
        }
        this.postRenameParentMap = mapping;
    }

    public String getSuperClassName(String className) {
        // NOTE:  These are ".-style" names.
        Assert.assertTrue(-1 == className.indexOf("/"));
        return this.postRenameParentMap.get(className);
    }
}
