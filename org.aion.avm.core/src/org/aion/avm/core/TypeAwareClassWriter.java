package org.aion.avm.core;

import org.aion.avm.core.classgeneration.CommonGenerators;
import org.objectweb.asm.ClassWriter;


/**
 * TODO:  This implementation is sufficient only for current testing but we will need to generalize it.
 * This implementation assumes that this is only being used because the exception table was duplicated to handle wrapper types
 * so we only check for those occurrences, then decide the common class must be throwable.
 */
public class TypeAwareClassWriter extends ClassWriter {
    public TypeAwareClassWriter(int flags) {
        super(flags);
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        String superclass = null;
        if (type1.startsWith(CommonGenerators.kSlashWrapperClassLibraryPrefix) || type2.startsWith(CommonGenerators.kSlashWrapperClassLibraryPrefix)) {
            superclass = "java/lang/Throwable";
        } else {
            superclass = super.getCommonSuperClass(type1, type2);
        }
        return superclass;
    }
}
