package org.aion.avm.core;

import java.util.HashMap;
import java.util.Map;

import org.aion.avm.core.util.Assert;


/**
 * Provides a minimal interface for quickly building the Forest() objects in tests.
 * Returns itself from addClass for easy chaining in boiler-plate test code.
 */
public class HierarchyTreeBuilder {
    private final Forest<String, byte[]> classHierarchy = new Forest<>();
    private final Map<String, Forest.Node<String, byte[]>> nameCache = new HashMap<>();

    public HierarchyTreeBuilder addClass(String name, String superclass, byte[] code) {
        // NOTE:  These are ".-style" names.
        Assert.assertTrue(-1 == name.indexOf("/"));
        Assert.assertTrue(-1 == superclass.indexOf("/"));
        
        // This better be new.
        Assert.assertTrue(!this.nameCache.containsKey(name));
        
        // Get the parent node.
        Forest.Node<String, byte[]> parent = this.nameCache.get(superclass);
        if (null == parent) {
            // Must be a root.
            parent = new Forest.Node<>(superclass, null);
            this.nameCache.put(superclass,  parent);
        }
        
        // Inject into tree.
        Forest.Node<String, byte[]> child = new Forest.Node<>(name, code);
        
        // Cache result.
        this.nameCache.put(name,  child);
        
        // Add connection.
        this.classHierarchy.add(parent, child);
        
        return this;
    }

    public Forest<String, byte[]> asMutableForest() {
        return this.classHierarchy;
    }
}
