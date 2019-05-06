package org.aion.avm.core.invokedynamic;

import org.aion.avm.core.ClassRenamer;
import org.aion.avm.core.miscvisitors.PreRenameClassAccessRules;

import java.util.Set;
import org.aion.avm.core.types.ClassHierarchy;

class InvokedynamicUtils {
    private InvokedynamicUtils() {
    }

    static String getSlashClassNameFrom(String dotName) {
        return dotName.replaceAll("\\.", "/") + ".class";
    }

    static PreRenameClassAccessRules buildSingletonAccessRules(ClassHierarchy classHierarchy, ClassRenamer classRenamer) {
        Set<String> preRenameUserDefinedClasses = classHierarchy.getPreRenameUserDefinedClassesOnly(classRenamer);
        Set<String> preRenameUserClassAndInterfaceSet = classHierarchy.getPreRenameUserDefinedClassesAndInterfaces();
        return new PreRenameClassAccessRules(preRenameUserDefinedClasses, preRenameUserClassAndInterfaceSet);
    }
}
