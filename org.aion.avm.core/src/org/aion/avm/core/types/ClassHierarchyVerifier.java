package org.aion.avm.core.types;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * A class that verifies that a given class hierarchy is complete and has no inconsistencies.
 *
 * Note that this verifier is looking for logical inconsistencies only. It does not have the classes
 * being referenced in its hands, and therefore is not ensuring that the nodes are faithful
 * representations of the actual types they correspond to!
 */
public final class ClassHierarchyVerifier {

    /**
     * Verifies that the specified hierarchy is a valid hierarchy.
     *
     * This verifier will return an unsuccessful verification result if any of the following faults
     * in the hierarchy are discovered:
     *
     * 1. There exists a ghost node in the hierarchy.
     * 2. There exists an interface that is a child of a non-interface.
     * 3. There exists a node with multiple non-interface parents.
     * 4. There exists a node that is not a descendant of the root node, java.lang.Object
     *
     * If none of these faults are discovered, then the verifier will return a successful result.
     *
     * @param hierarchy The hierarchy to be verified.
     */
    public HierarchyVerificationResult verifyHierarchy(ClassHierarchy hierarchy) {
        if (hierarchy == null) {
            throw new NullPointerException("Cannot verify a null hierarchy.");
        }

        Set<IHierarchyNode> visited = new HashSet<>();

        LinkedList<IHierarchyNode> nodesToVisit = new LinkedList<>();
        nodesToVisit.add(hierarchy.getRoot());

        while (!nodesToVisit.isEmpty()) {
            IHierarchyNode currentNode = nodesToVisit.poll();

            // Verify that the node is not a ghost node. This should never happen.
            if (currentNode.isGhostNode()) {
                return HierarchyVerificationResult.foundGhostNode(currentNode.getDotName());
            }

            for (IHierarchyNode child : currentNode.getChildren()) {

                // Verify no interface is a child of a non-interface.
                if ((child.getClassInfo().isInterface) && (!currentNode.getClassInfo().isInterface)) {

                    // The only exception to this rule is when parent is java/lang/Object!
                    if (!currentNode.getClassInfo().dotName.equals(CommonType.JAVA_LANG_OBJECT.dotName)) {
                        return HierarchyVerificationResult.foundInterfaceWithConcreteSuperClass(child.getDotName());
                    }
                }

                nodesToVisit.addFirst(child);
            }

            // Verify this node does not have multiple non-interface parents.
            int numberOfNonInterfaceParents = 0;
            for (IHierarchyNode parent : currentNode.getParents()) {

                if (parent.isGhostNode()) {
                    return HierarchyVerificationResult.foundGhostNode(parent.getDotName());
                }

                if (!parent.getClassInfo().isInterface) {
                    numberOfNonInterfaceParents++;
                }
            }

            if (numberOfNonInterfaceParents > 1) {
                return HierarchyVerificationResult.foundMultipleNonInterfaceSuperClasses(currentNode.getDotName());
            }

            visited.add(currentNode);
        }

        // Verify that every node was in fact reached.
        if (visited.size() != hierarchy.size()) {
            return HierarchyVerificationResult.foundUnreachableNodes(hierarchy.size() - visited.size());
        }
        
        return HierarchyVerificationResult.successful();
    }

}
