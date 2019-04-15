package org.aion.avm.core.types;

import org.aion.avm.ClassNameExtractor;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.internal.RuntimeAssertionError;

public final class NonWrapperClassRenamer {

    private enum ShadowJclPackage {
        JAVA_LANG("java.lang"),
        JAVA_UTIL("java.util"),
        JAVA_MATH("java.math"),
        JAVA_IO("java.io"),
        ;

        public String packageDotName;

        ShadowJclPackage(String packageDotName) {
            this.packageDotName = packageDotName;
        }
    }

    /**
     * This method will rename {@code preRenameDotName} to its appropriate post-rename name under
     * the assumption that {@code preRenameDotName} is NOT a wrapper type; that is, it is not to be
     * wrapped into an array or exception wrapper.
     *
     * In particular, the following cases are handled by this method:
     *
     * 1. If {@code preRenameDotName} is a JCL class it will be renamed to a shadow JCL class.
     * 2. If {@code preRenameDotName} is an API class it will be renamed to a shadow API class.
     * 3. If {@code preRenameDotName == null} this method will return null.
     *
     * Otherwise, if none of the conditions above are satisfied, {@code preRenameDotName} will be
     * renamed as a user-defined class.
     *
     * NOTE: java.lang.Object will be renamed to shadow Object and not IObject!
     *
     * This method will return back the original {@code preRenameDotName} only if it has already
     * been renamed.
     *
     * @param preRenameDotName The name to be renamed.
     * @return The renamed name.
     */
    public static String toPostRenameClassName(String preRenameDotName) {
        if (preRenameDotName == null) {
            return null;
        }

        RuntimeAssertionError.assertTrue(!preRenameDotName.contains("/"));

        // If we are actually given a post-rename name then we do not perform any re-naming.
        if (ClassNameExtractor.isPostRenameClassDotStyle(preRenameDotName)) {
            return preRenameDotName;
        }

        // Handle java.lang.Object
        if (preRenameDotName.equals(CommonType.JAVA_LANG_OBJECT.dotName)) {
            return CommonType.SHADOW_OBJECT.dotName;
        }

        // Handle java.lang.Throwable
        if (preRenameDotName.equals(CommonType.JAVA_LANG_THROWABLE.dotName)) {
            return CommonType.SHADOW_THROWABLE.dotName;
        }

        // Handle JCL classes
        if (isJclClass(preRenameDotName)) {
            return PackageConstants.kShadowDotPrefix + preRenameDotName;
        }

        // Handle api classes
        if (preRenameDotName.startsWith(PackageConstants.kPublicApiDotPrefix)) {
            return PackageConstants.kShadowApiDotPrefix + preRenameDotName;
        }

        // If we make it here then we have to assume it is a user-space class.
        return PackageConstants.kUserDotPrefix + preRenameDotName;
    }

    private static boolean isJclClass(String dotName) {
        for (ShadowJclPackage shadowJclPackage : ShadowJclPackage.values()) {
            if (dotName.startsWith(shadowJclPackage.packageDotName)) {
                return true;
            }
        }
        return false;
    }

}
