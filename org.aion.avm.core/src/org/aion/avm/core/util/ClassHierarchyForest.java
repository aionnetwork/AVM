package org.aion.avm.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A helper which maintain te class inheritance relations.
 */
public class ClassHierarchyForest {

    /**
     * A helper class that defines the TreeNode.
     * Each TreeNode corresponds to a runtime or smart contract class.
     * The TreeNode maintains the className, a parentNode and a list of childNodes.
     */
    private class TreeNode {
        String className;
        TreeNode parentNode;
        List<TreeNode> childNodes;

        /**
         * Constructor.
         * @param name The internal name of the class. See ASM ClassNode.name or Type.getInternalName().
         */
        TreeNode(String name) {
            this.className = name;
            this.childNodes = new ArrayList<>();
        }

        /**
         * Connect the parentNode.
         * @param parentNode A TreeNode to be connected as the parent class of this class
         */
        public void setParent(TreeNode parentNode) {
            if (this.parentNode == null) {
                this.parentNode = parentNode;
            }
            else if (this.parentNode != parentNode) {
                throw new IllegalArgumentException("A class can only have one parent Class.");
            }
        }

        /**
         * Connect another childNode.
         * @param childNode A TreeNode to be connected as one of the child class of this class
         */
        public void addChild(TreeNode childNode) {
            // add it to the child list if not there yet
            if (!(childNodes.contains(childNode))) {
                childNodes.add(childNode);
            }
        }
    }

    /**
     * A list to maintain all the tree root nodes in this forest.
     * After all the runtime and contract classes are added, there should be only one tree thus one root node in this list,
     * which represents java.lang.object (the shadowing one of ASM)
     * Before the hierarchy is completely constructed, temporarily there may be multiple trees thus multiple root nodes.
     */
    private List<TreeNode> treeRoots;

    /**
     * A helper hash map to quickly locate a class's TreeNode. Need maintenance at every time a new node is added to the
     * hierarchy or a node is removed from it.
     */
    private Map<String, TreeNode> searchMap;

    /**
     * Constructor.
     */
    public ClassHierarchyForest() {
        treeRoots = new ArrayList<>();
        searchMap = new HashMap<>();
    }

    /**
     * Returns the parent class of the given class.
     * @param clazz the internal name of a class
     * @return its parent class's internal name; null if it does not have a parent class
     */
    public String getParent(String clazz) {
        if (!searchMap.containsKey(clazz)){
            throw new IllegalArgumentException("class is not in the hierarchy");
        }
        else if (searchMap.get(clazz).parentNode != null) {
            // it has a parent
            return searchMap.get(clazz).parentNode.className;
        }
        else {
            // it does not have a parent
            return null;
        }
    }

    /**
     * Returns the child classes of the given class.
     * @param clazz the internal name of a class
     * @return a list of all its child class's internal name
     */
    public List<String> getChildren(String clazz) {
        if (!searchMap.containsKey(clazz)){
            throw new IllegalArgumentException("class is not in the hierarchy");
        }
        else {
            List<String> children = new ArrayList<>();

            // walk the childNodes list
            for (TreeNode childNode : searchMap.get(clazz).childNodes) {
                children.add(childNode.className);
            }

            return children;
        }
    }

    /**
     * Adds a child-parent relation to the hierarchy.
     * If the passed-in classes are not in the hierarchy, add them to it.
     * @param child the internal name of a child class
     * @param parent the internal name of a parent class
     */
    public void addInheritance(String child, String parent) {
        if (child == null || parent == null) {
            return;
        }

        TreeNode childNode;
        TreeNode parentNode;

        // get the childNode from the search map, if not there, create a new one and add to the map
        if (searchMap.containsKey(child)) {
            childNode = searchMap.get(child);
        }
        else {
            childNode = new TreeNode(child);
            searchMap.put(child, childNode);
        }

        // get the parentNode from the search map, if not there, create a new one and add to the map
        if (searchMap.containsKey(parent)) {
            parentNode = searchMap.get(parent);
        }
        else {
            parentNode = new TreeNode(parent);
            searchMap.put(parent, parentNode);
        }

        // update the 2 treeNode to reflect the inheritance
        childNode.setParent(parentNode);
        parentNode.addChild(childNode);

        // update treeRoots
        // if the parentNode does not have superclasses and is not in treeRoots yet, add it
        if (parentNode.parentNode == null && !(treeRoots.contains(parentNode))) {
            treeRoots.add(parentNode);
        }
        // if the childNode is in the treeRoots, remove it
        if (treeRoots.contains(childNode)) {
            treeRoots.remove(childNode); // There should be no duplicates in the treeRoots list; removing once should work.
        }
    }

    /**
     * return all the tree roots' class name.
     * @return a list of the class internal names of all tree roots
     */
    public List<String> getTreeRoots() {
        List<String> rootClassNames = new ArrayList<>();

        // walk the treeRoots list
        for (TreeNode rootNode : treeRoots) {
            rootClassNames.add(rootNode.className);
        }
        return rootClassNames;
    }
}
