package org.aion.avm.core;

import java.util.Stack;

import org.aion.avm.core.types.Forest;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.Helpers;
import org.objectweb.asm.ClassWriter;


/**
 * We extend the ClassWriter to override their implementation of getCommonSuperClass() with an implementation which knows how
 * to compute this relationship between our generated classes, before they can be loaded.
 */
public class TypeAwareClassWriter extends ClassWriter {
    private static final String IOBJECT_SLASH_NAME = "org/aion/avm/internal/IObject";

    private final ParentPointers staticClassHierarchy;
    // WARNING:  This dynamicHierarchyBuilder is changing, externally, while we hold a reference to it.
    private final HierarchyTreeBuilder dynamicHierarchyBuilder;

    public TypeAwareClassWriter(int flags, ParentPointers parentClassResolver, HierarchyTreeBuilder dynamicHierarchyBuilder) {
        super(flags);
        this.staticClassHierarchy = parentClassResolver;
        this.dynamicHierarchyBuilder = dynamicHierarchyBuilder;
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        // NOTE:  The types we are receiving and returning here use slash-style names.
        String commonRoot = null;
        
        // TODO (issue-176): Generalize this interface handling instead of just using this IObject special-case.
        if (IOBJECT_SLASH_NAME.equals(type1) || IOBJECT_SLASH_NAME.equals(type2)) {
            commonRoot = IOBJECT_SLASH_NAME;
        } else {
            // We will use a relatively simple approach, here:  build a stack for each type (root on top), then find the common prefix by popping.
            Stack<String> stack1 = builTypeListFor(type1);
            Stack<String> stack2 = builTypeListFor(type2);
            
            while (!stack1.isEmpty() && !stack2.isEmpty() && stack1.peek().equals(stack2.peek())) {
                commonRoot = stack1.pop();
                stack2.pop();
            }
        }
        return commonRoot;
    }


    private Stack<String> builTypeListFor(String type) {
        // NOTE:  These are "/-style" names.
        Assert.assertTrue(-1 == type.indexOf("."));
        
        // The complexity here is that we have 3 different sources of truth to consult, and they are non-overlapping (only meeting at edges):
        // 1) Static class hierarchy within the contract.
        // 2) Dynamic class hierarchy, built as we operate on the contract code.
        // 3) The JDK classes, themselves (which should probably only apply to "java/lang" exceptions and "Object".
        // (we consult these in that order).
        
        Stack<String> stack = new Stack<>();
        String nextType = type;
        while (!"java/lang/Object".equals(nextType)) {
            stack.push(nextType);
            
            String nextDotType = Helpers.internalNameToFulllyQualifiedName(nextType);
            String superDotName = this.staticClassHierarchy.getSuperClassName(nextDotType);
            if (null == superDotName) {
                superDotName = getSuper(this.dynamicHierarchyBuilder.asMutableForest(), nextDotType);
            }
            if (null == superDotName) {
                superDotName = getSuperAsJdkType(nextDotType);
            }
            
            // If we didn't find it by now, there is something very wrong.
            Assert.assertNotNull(superDotName);
            Assert.assertTrue(-1 == superDotName.indexOf("/"));
            
            String superName = Helpers.fulllyQualifiedNameToInternalName(superDotName);
            Assert.assertTrue(-1 == superName.indexOf("."));
            nextType = superName;
        }
        stack.push(nextType);
        return stack;
    }

    private String getSuper(Forest<String, byte[]> forest, String name) {
        String superName = null;
        Forest.Node<String, byte[]> node = forest.getNodeById(name);
        if (null != node) {
            Forest.Node<String, byte[]> superNode = node.getParent();
            if (null != superNode) {
                superName = superNode.getId();
            }
        }
        return superName;
    }

    /**
     * NOTE:  This takes and returns .-style names.
     */
    private String getSuperAsJdkType(String name) {
        // NOTE:  These are ".-style" names.
        Assert.assertTrue(-1 == name.indexOf("/"));
        
        String superName = null;
        try {
            Class<?> clazz = NodeEnvironment.singleton.loadSharedClass(name);
            superName = clazz.getSuperclass().getName();
        } catch (ClassNotFoundException e) {
            // We can return null, in this case.
        }
        return superName;
    }
}
