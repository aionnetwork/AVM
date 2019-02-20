package org.aion.avm.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aion.avm.core.types.ClassInfo;
import org.aion.avm.core.types.Forest;
import org.aion.avm.core.util.DebugNameResolver;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.internal.RuntimeAssertionError;


/**
 * Just a utility to simplify interactions with ClassHierarchyForest since most consumers only need to resolve parent names, not know the rest
 * of the data.
 * NOTE:  The class names here are the ".-style"
 */
public class ParentPointers {
    private final Map<String, String> postRenameParentMap;

    public ParentPointers(Set<String> userDefinedClassNames, Forest<String, ClassInfo> classHierarchy, boolean preserveDebuggability) {
        // Get every user-defined class, find its parent, and add the pair to the map, while renaming them.
        Map<String, String> mapping = new HashMap<>();
        for (String className : userDefinedClassNames) {
            // NOTE:  These are ".-style" names.
            RuntimeAssertionError.assertTrue(-1 == className.indexOf("/"));
            
            Forest.Node<String, ClassInfo> node = classHierarchy.getNodeById(className);
            String superClassName = node.getParent().getId();
            
            String newName = DebugNameResolver.getUserPackageDotPrefix(className, preserveDebuggability);
            String newSuperName = userDefinedClassNames.contains(superClassName)
                    ? DebugNameResolver.getUserPackageDotPrefix(superClassName, preserveDebuggability)
                    : (superClassName.startsWith(PackageConstants.kPublicApiDotPrefix) ? PackageConstants.kShadowApiDotPrefix + superClassName : (PackageConstants.kShadowDotPrefix + superClassName));
            mapping.put(newName, newSuperName);
        }
        this.postRenameParentMap = mapping;
    }

    public String getSuperClassName(String className) {
        // NOTE:  These are ".-style" names.
        RuntimeAssertionError.assertTrue(-1 == className.indexOf("/"));
        return this.postRenameParentMap.get(className);
    }
}
