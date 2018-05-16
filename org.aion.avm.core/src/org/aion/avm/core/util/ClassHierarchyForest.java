package org.aion.avm.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A helper which maintain te class inheritance relations.
 */
public class ClassHierarchyForest {

    private class TreeNode {
        String className;
        TreeNode parentNode;
        List<TreeNode> childNodes;

        TreeNode(String name) {
            this.className = name;
            this.childNodes = new ArrayList<>();
        }

        public String getClassName() {
            return className;
        }

        public void setParent(TreeNode parentNode) {
            if (this.parentNode == null) {
                this.parentNode = parentNode;
            }
            else if (this.parentNode != parentNode) {
                throw new IllegalArgumentException("A class can only have one parent Class.");
            }
        }

        public void addChild(TreeNode childNode) {
            // add it to the child list if not there yet
            if (!(childNodes.contains(childNode))) {
                childNodes.add(childNode);
            }
        }
    }

    private List<TreeNode> treeRoots;

    private Map<String, TreeNode> searchMap;

    public ClassHierarchyForest() {
        treeRoots = new ArrayList<>();
        searchMap = new HashMap<>();
    }

    /**
     * Returns the parent class of the given class.
     *
     * @param clazz the full path
     * @return
     */
    public String getParent(String clazz) {
        if (!searchMap.containsKey(clazz)){
            throw new IllegalArgumentException("class is not in the hierarchy");
        }
        else if (searchMap.get(clazz).parentNode != null) {
            // it has a parent
            return searchMap.get(clazz).parentNode.getClassName();
        }
        else {
            // it does not have a parent
            return null;
        }
    }

    /**
     * Returns the child classes of the given class.
     *
     * @param clazz the full path
     * @return
     */
    public List<String> getChildren(String clazz) {
        if (!searchMap.containsKey(clazz)){
            throw new IllegalArgumentException("class is not in the hierarchy");
        }
        else {
            List<String> children = new ArrayList<>();

            for (TreeNode childNode : searchMap.get(clazz).childNodes) {
                children.add(childNode.getClassName());
            }

            return children;
        }
    }

    /**
     * Adds a child-parent relation to the hierarchy.
     *
     * @param child
     * @param parent
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
        if (parentNode.parentNode == null && !(treeRoots.contains(parentNode))) {
            treeRoots.add(parentNode);
        }
        if (treeRoots.contains(childNode)) {
            treeRoots.remove(childNode); // There should not be duplicates.
        }
    }
}
