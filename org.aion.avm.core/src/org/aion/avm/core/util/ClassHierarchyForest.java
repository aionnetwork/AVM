package org.aion.avm.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A helper which maintain te class inheritance relations.
 */
public class ClassHierarchyForest {

    private Map<String, String> parents = new HashMap<>();

    public ClassHierarchyForest() {
    }

    /**
     * Returns the parent class of the given class.
     *
     * @param clazz the full path
     * @return
     */
    public String getParent(String clazz) {
        return parents.get(clazz);
    }

    /**
     * Returns the parent class of this class, and parent of parent class.
     *
     * @param clazz
     * @return
     */
    public List<String> getParents(String clazz) {
        List<String> list = new ArrayList<>();

        for (String parent; (parent = getParent(clazz)) != null; ) {
            list.add(parent);
            clazz = parent;
        }

        return list;
    }

    /**
     * Adds a child-parent relation to the hierarchy.
     *
     * @param child
     * @param parent
     */
    public void addInheritance(String child, String parent) {
        parents.put(child, parent);
    }
}
