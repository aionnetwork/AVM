package org.aion.avm.core.invokedynamic;

import org.aion.avm.core.ClassWhiteList;
import org.aion.avm.core.miscvisitors.PreRenameClassAccessRules;
import org.aion.avm.core.types.ClassInfo;
import org.aion.avm.core.types.Forest;

import java.util.Set;

class InvokedynamicUtils {
    private InvokedynamicUtils() {
    }

    static String getSlashClassNameFrom(String dotName) {
        return dotName.replaceAll("\\.", "/") + ".class";
    }

    static PreRenameClassAccessRules buildSingletonAccessRules(final Forest<String, ClassInfo> classHierarchy, final String... className) {
        Set<String> preRenameUserDefinedClasses = ClassWhiteList.extractDeclaredClasses(classHierarchy);
        Set<String> preRenameUserClassAndInterfaceSet = Set.of(className);
        return new PreRenameClassAccessRules(preRenameUserDefinedClasses, preRenameUserClassAndInterfaceSet);
    }
}