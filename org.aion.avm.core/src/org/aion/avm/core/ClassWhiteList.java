package org.aion.avm.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aion.avm.core.util.Helpers;


/**
 * A high-level abstraction over the questions related to "what classes can be referenced by this contract".
 * This encompasses 3 kinds of classes:
 * 1)  The JDK types we have shadowed.
 * 2)  The AVM runtime package.
 * 3)  The types defined within the user contract, itself.
 * 
 * Note that all predicates here are requested in terms of "slash-style" (aka "internal") class names.
 * TODO:  We need to add some additional restrictions to our JDK filter since we won't shadow all java/lang sub-packages ("ref", for example).
 */
public class ClassWhiteList {
    private static final String JAVA_LANG = "java/lang/";
    private static final String JAVA_UTIL_FUNCTION = "java/util/function";
    private static final String AION_RT = "org/aion/avm/rt/";
    private static final String AION_SHADOWING = "org/aion/avm/core/shadowing/";

    private final Set<String> contractClassNames;
    
    private ClassWhiteList(Set<String> contractClassNames) {
        this.contractClassNames = contractClassNames;
    }

    /**
     * Checks if the class given is in any of our white-lists.
     * 
     * @param slashClassName The class to check.
     * @return True if we are allowed to access this class by any means we know.
     */
    public boolean isInWhiteList(String slashClassName) {
        return (this.contractClassNames.contains(slashClassName)
                || slashClassName.startsWith(JAVA_LANG)
                || slashClassName.startsWith(JAVA_UTIL_FUNCTION)
                || slashClassName.startsWith(AION_RT)
                || slashClassName.startsWith(AION_SHADOWING)
                );
    }

    /**
     * Checks if the given class is in our JDK white-list.
     *
     * @param slashClassName The class to check.
     * @return True if we are allowed to access this class due to it being in our JDK white-list.
     */
    public boolean isJdkClass(String slashClassName) {
        return slashClassName.startsWith(JAVA_LANG) || slashClassName.startsWith(JAVA_UTIL_FUNCTION);
    }


    /**
     * Factory method to build a white-list from a populated hierarchy of classes in the user contract.
     * This is the most typical usage.
     * 
     * @param classHierarchy The hierarchy of classes within the user contract.
     * @return A white-list instance which knows about these types and our built-in JDK and runtime types.
     */
    public static ClassWhiteList buildFromClassHierarchy(Forest<String, byte[]> classHierarchy) {
        Set<String> providedClassNames = new HashSet<>();
        // We will build this set by walking the roots (note that roots, by definition, are not provided by the application, but the JDK)
        // and recursively collecting all reachable children.
        for (Forest.Node<String, byte[]> root : classHierarchy.getRoots()) {
            // Note that we don't add the roots, just walk their children.
            deepAddChildrenToSet(providedClassNames, root);
        }
        return new ClassWhiteList(Collections.unmodifiableSet(providedClassNames));
    }

    /**
     * Factory method to build a white-list which only includes our built-in JDK and runtime types.
     * This usage is only appropriate for testing, really.
     * 
     * @return A white-list instance which only knows about our built-in JDK and runtime types.
     */
    public static ClassWhiteList buildForEmptyContract() {
        return new ClassWhiteList(Collections.emptySet());
    }

    public static ClassWhiteList build(String ...classes) {
        return new ClassWhiteList(Stream.of(classes).map(clazz -> clazz.replaceAll("\\.", "/")).collect(Collectors.toSet()));
    }

    private static void deepAddChildrenToSet(Set<String> providedClassNames, Forest.Node<String, byte[]> node) {
        for (Forest.Node<String, byte[]> child : node.getChildren()) {
            // We want to use the slash-style but the forest sees dot-style.
            String slashChildName = Helpers.fulllyQualifiedNameToInternalName(child.getId());
            providedClassNames.add(slashChildName);
            deepAddChildrenToSet(providedClassNames, child);
        }
    }
}
