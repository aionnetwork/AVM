package org.aion.avm.core.invokedynamic;

class InvokedynamicUtils {
    private InvokedynamicUtils() {
    }

    static String getSlashClassNameFrom(String dotName) {
        return dotName.replaceAll("\\.", "/") + ".class";
    }
}