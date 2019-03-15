package org.aion.avm.core;

import java.util.Stack;

import org.aion.avm.core.exceptionwrapping.ExceptionWrapperNameMapper;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.internal.RuntimeAssertionError;
import org.objectweb.asm.ClassWriter;


/**
 * We extend the ClassWriter to override their implementation of getCommonSuperClass() with an implementation which knows how
 * to compute this relationship between our generated classes, before they can be loaded.
 */
public class TypeAwareClassWriter extends ClassWriter {
    private static final String IOBJECT_SLASH_NAME = PackageConstants.kInternalSlashPrefix + "IObject";
    // Note that we handle the wrapper of shadow "java.lang.Throwable" as a special-case, since that one is our manually-implemented root.
    private static final Class<?> WRAPPER_ROOT_THROWABLE = org.aion.avm.exceptionwrapper.org.aion.avm.shadow.java.lang.Throwable.class;

    private final ParentPointers staticClassHierarchy;

    public TypeAwareClassWriter(int flags, ParentPointers parentClassResolver) {
        super(flags);
        this.staticClassHierarchy = parentClassResolver;
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
        RuntimeAssertionError.assertTrue(-1 == type.indexOf("."));
        
        // The complexity here is that we have 3 different sources of truth to consult, and they are non-overlapping (only meeting at edges):
        // 1) Static class hierarchy within the contract.
        // 2) Dynamic class hierarchy, built as we operate on the contract code.
        // 3) The JDK classes, themselves (which should probably only apply to "java/lang" exceptions and "Object".
        // (we consult these in that order).
        
        Stack<String> stack = new Stack<>();
        String nextType = type;
        while (!"java/lang/Object".equals(nextType)) {
            stack.push(nextType);
            
            // Exception wrappers work the same as their underlying type, but all relationships are mapped into the wrapper space.
            boolean isWrapper = ExceptionWrapperNameMapper.isExceptionWrapper(nextType);
            // The rest of the generated wrappers may not yet have been generated when we need to unify the types but they do have a very simple
            // structure, so we can infer their relationships from the original underlying types.
            // The hand-written root must be handled differently, and its superclass read directly (since, being the root, it has this special
            // relationship with java.lang.Throwable).
            boolean isThrowableWrapper = false;
            if (isWrapper) {
                isThrowableWrapper = WRAPPER_ROOT_THROWABLE.getName().equals(Helpers.internalNameToFulllyQualifiedName(nextType));
                nextType = ExceptionWrapperNameMapper.slashClassNameForWrapperName(nextType);
            }
            String nextDotType = Helpers.internalNameToFulllyQualifiedName(nextType);
            // The manual throwable wrapper should be asked for its superclass, directly, instead of inferring it from the underlying type.
            String superDotName = isThrowableWrapper
                    ? WRAPPER_ROOT_THROWABLE.getSuperclass().getName()
                    : this.staticClassHierarchy.getSuperClassName(nextDotType);
            if (null == superDotName) {
                superDotName = getSuperAsJdkType(nextDotType);
            }
            
            // If we didn't find it by now, there is something very wrong.
            RuntimeAssertionError.assertTrue(null != superDotName);
            RuntimeAssertionError.assertTrue(-1 == superDotName.indexOf("/"));
            
            String superName = Helpers.fulllyQualifiedNameToInternalName(superDotName);
            // We now wrap the super-class, if we started with a wrapper and this is NOT the shadow throwable wrapper.
            if (isWrapper && !isThrowableWrapper) {
                superName = ExceptionWrapperNameMapper.slashWrapperNameForClassName(superName);
            }
            RuntimeAssertionError.assertTrue(-1 == superName.indexOf("."));
            nextType = superName;
        }
        stack.push(nextType);
        return stack;
    }

    /**
     * NOTE:  This takes and returns .-style names.
     */
    private String getSuperAsJdkType(String name) {
        // NOTE:  These are ".-style" names.
        RuntimeAssertionError.assertTrue(-1 == name.indexOf("/"));
        
        String superName = null;
        try {
            Class<?> clazz = NodeEnvironment.singleton.loadSharedClass(name);
            // issue-362: temporarily, force interfaces to drop directly to java.lang.Object (interface relationships will be fleshed out later under this item).
            if (clazz.isInterface()) {
                superName = "java.lang.Object";
            } else {
                superName = clazz.getSuperclass().getName();
            }
        } catch (ClassNotFoundException e) {
            // We can return null, in this case.
        }
        return superName;
    }
}
