package org.aion.avm.tooling.deploy.renamer;

import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClassRenamer {
    private static boolean printEnabled = false;
    //these restrictions are added to support debug mode, because user classes are not renamed.
    private static Set<String> restrictions = Set.of(new String[]{"H", "C"});

    //NOTE package name is removed
    public static Map<String, String> renameClasses(Map<String, ClassNode> classMap, String mainClassName) {

        // Key should be class name (slash format)
        Map<String, String> classNameMap = new HashMap<>();
        NameGenerator generator = new NameGenerator();

        for (String className : classMap.keySet()) {
            String newClassName;
            if (className.contains("$")) {
                newClassName = classNameMap.get(className.substring(0, className.lastIndexOf('$'))) + "$" + generator.getNextClassName(restrictions);
                classNameMap.put(className, newClassName);
            } else {
                newClassName = className.equals(mainClassName) ? NameGenerator.getNewMainClassName() : generator.getNextClassName(restrictions);
                classNameMap.put(className, newClassName);
            }
            if (printEnabled) {
                System.out.println("Renaming class " + className + " to " + newClassName);
            }
        }
        return classNameMap;
    }

}
