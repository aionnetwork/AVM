package org.aion.avm.core;

import org.aion.avm.ArrayClassNameMapper;
import org.aion.avm.ArrayRenamer;
import org.aion.avm.ArrayUtil;
import org.aion.avm.ClassNameExtractor;
import org.aion.avm.NameStyle;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.core.exceptionwrapping.ExceptionWrapperNameMapper;
import org.aion.avm.core.types.ClassHierarchy;
import org.aion.avm.core.types.NonWrapperClassRenamer;
import org.aion.avm.core.types.CommonType;
import org.aion.avm.core.util.Helpers;
import org.objectweb.asm.ClassWriter;


/**
 * We extend the ClassWriter to override their implementation of getCommonSuperClass() with an implementation which knows how
 * to compute this relationship between our generated classes, before they can be loaded.
 */
public class TypeAwareClassWriter extends ClassWriter {
    private final ClassHierarchy pocHierarchy;
    private final boolean preserveDebuggability;

    public TypeAwareClassWriter(int flags, ClassHierarchy hierarchy, boolean preserveDebuggability) {
        super(flags);
        this.pocHierarchy = hierarchy;
        this.preserveDebuggability = preserveDebuggability;
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        // If the two types are the same, return either one.
        if (type1.equals(type2)) {
            return type1;
        }

        String type1dotName = Helpers.internalNameToFulllyQualifiedName(type1);
        String type2dotName = Helpers.internalNameToFulllyQualifiedName(type2);

        // If we can immediately determine the super class becuase one of these is a root type, do it.
        String rootTypeSuper = findCommonSuperIfOneTypeIsRoot(type1dotName, type2dotName);
        if (rootTypeSuper != null) {
            return rootTypeSuper;
        }

        // If one type is an exception wrapper we determine the class now. We do this before the pre-post name
        // mixup check because exception wrappers have two possible unifications that require extra work
        // if the other type is a pre-rename type.
        String exceptionWrapperSuper = findCommonSuperIfOneTypeIsExceptionWrapper(type1dotName,
            type2dotName);
        if (exceptionWrapperSuper != null) {
            return exceptionWrapperSuper;
        }

        // If one type is pre-rename and the other post-rename then the super class is java/lang/Object.
        String mixedNameSuper = findCommonSuperIfOneTypeIsPreRenameAndOtherPostRename(type1dotName,
            type2dotName);
        if (mixedNameSuper != null) {
            return mixedNameSuper;
        }

        // If one type is an array wrapper then we do the special-case wrapper handling here and return.
        String arrayWrapperSuper = findCommonSuperIfOneTypeIsArrayWrapper(type1dotName,
            type2dotName);
        if (arrayWrapperSuper != null) {
            return arrayWrapperSuper;
        }

        // Finally, if we made it this far then we have no special-case handling remaining. In particular,
        // exception & array wrappers have been handled (and a few other easy-to-answer cases).
        // The only remaining special-casing left is renaming if we are dealing with pre-rename classes.
        // But otherwise, we expect all remaining types (once renamed if needed) are in the hierarchy and we
        // can resolve their super class by querying the hierarchy.
        return (this.preserveDebuggability)
            ? getCommonSuperFromBasicTypesWhenInDebugMode(type1dotName, type2dotName)
            : getCommonSuperFromBasicTypes(type1dotName, type2dotName);
    }

    /**
     * This should be the FINAL attempt to get the common super class -- this logic only works because
     * we assume we have eliminated the following types:
     *
     * 1. java/lang/Object
     * 2. type1 is pre-rename and type2 is post-rename, or vice versa
     * 3. type1 or type2 is an exception wrapper
     * 4. type1 or type2 is an array wrapper
     *
     * In other words, we are only left with 'basic' types.
     *
     * This method does the same thing as getCommonSuperFromBasicTypesWhenInDebugMode() but is for
     * when we are NOT in debug mode!
     */
    private String getCommonSuperFromBasicTypes(String type1dotName, String type2dotName) {
        // Since we know both are either pre- or post-rename we only need to query one.
        boolean bothTypesArePreRename = !ClassNameExtractor.isPostRenameClassDotStyle(type1dotName);

        // Finally, if we have any pre-rename types, we rename them so that we can query the hierarchy.
        type1dotName = (bothTypesArePreRename) ? NonWrapperClassRenamer.toPostRenameClassName(type1dotName) : type1dotName;
        type2dotName = (bothTypesArePreRename) ? NonWrapperClassRenamer.toPostRenameClassName(type2dotName) : type2dotName;

        // Grab the super class.
        String commonSuper = this.pocHierarchy.getTightestCommonSuperClass(type1dotName, type2dotName);

        // If we had any pre-rename types involved then we always un-name them when returning.
        if (bothTypesArePreRename) {

            // If the common super was IObject, this becomes Object because we are 'un-naming' now.
            if (commonSuper.equals(CommonType.I_OBJECT.dotName)) {
                return Helpers.fulllyQualifiedNameToInternalName(CommonType.JAVA_LANG_OBJECT.dotName);
            }

            // Otherwise, we simply undo the renaming we did and return the super class.
            String unnamedCommonSuper = ClassNameExtractor.getOriginalClassName(commonSuper);
            return Helpers.fulllyQualifiedNameToInternalName(unnamedCommonSuper);
        }

        // If we make it here, no pre-rename types were involved and we are done.
        return Helpers.fulllyQualifiedNameToInternalName(commonSuper);
    }

    /**
     * This should be the FINAL attempt to get the common super class -- this logic only works because
     * we assume we have eliminated the following types:
     *
     * 1. java/lang/Object
     * 2. type1 is pre-rename and type2 is post-rename, or vice versa
     * 3. type1 or type2 is an exception wrapper
     * 4. type1 or type2 is an array wrapper
     *
     * In other words, we are only left with 'basic' types.
     *
     * This method does the same thing as getCommonSuperFromBasicTypes() but is for when we ARE in
     * debug mode!
     */
    private String getCommonSuperFromBasicTypesWhenInDebugMode(String type1dotName, String type2dotName) {
        // Determine which classes to rename
        boolean type1isUserDefinedType = this.pocHierarchy.isPreRenameUserDefinedClass(type1dotName);
        boolean type2isUserDefinedType = this.pocHierarchy.isPreRenameUserDefinedClass(type2dotName);

        boolean type1isPreRename = !type1isUserDefinedType && !ClassNameExtractor.isPostRenameClassDotStyle(type1dotName);
        boolean type2isPreRename = !type2isUserDefinedType && !ClassNameExtractor.isPostRenameClassDotStyle(type2dotName);

        // Finally, if we have any pre-rename types, we rename them so that we can query the hierarchy.
        type1dotName = (type1isPreRename) ? NonWrapperClassRenamer.toPostRenameClassName(type1dotName) : type1dotName;
        type2dotName = (type2isPreRename) ? NonWrapperClassRenamer.toPostRenameClassName(type2dotName) : type2dotName;

        // Grab the super class.
        String commonSuper = this.pocHierarchy.getTightestCommonSuperClass(type1dotName, type2dotName);

        // If both types are user-defined then we can return whatever the hierarchy found as the super class.
        if (type1isUserDefinedType && type2isUserDefinedType) {
            return Helpers.fulllyQualifiedNameToInternalName(commonSuper);
        }

        // If one is a user-defined type and the other is pre-rename, then if our common super is post-rename
        // we have to default to java/lang/Object as the only common super, otherwise, if it is pre-rename
        // we are safe to return it.
        if (type1isUserDefinedType && type2isPreRename) {

            if (ClassNameExtractor.isPostRenameClassDotStyle(commonSuper)) {
                return Helpers.fulllyQualifiedNameToInternalName(CommonType.JAVA_LANG_OBJECT.dotName);
            }

            return Helpers.fulllyQualifiedNameToInternalName(commonSuper);
        }

        // Same case as above, but reversed.
        if (type2isUserDefinedType && type1isPreRename) {

            if (ClassNameExtractor.isPostRenameClassDotStyle(commonSuper)) {
                return Helpers.fulllyQualifiedNameToInternalName(CommonType.JAVA_LANG_OBJECT.dotName);
            }

            return Helpers.fulllyQualifiedNameToInternalName(commonSuper);
        }

        // If we had any pre-rename types involved then we always un-name them when returning.
        if (type1isPreRename && type2isPreRename) {

            // If the common super was IObject, this becomes Object because we are 'un-naming' now.
            if (commonSuper.equals(CommonType.I_OBJECT.dotName)) {
                return Helpers.fulllyQualifiedNameToInternalName(CommonType.JAVA_LANG_OBJECT.dotName);
            }

            // Otherwise, we simply undo the renaming we did and return the super class.
            String unnamedCommonSuper = ClassNameExtractor.getOriginalClassName(commonSuper);
            return Helpers.fulllyQualifiedNameToInternalName(unnamedCommonSuper);
        }

        // If we make it here, no pre-rename types were involved and we are done.
        return Helpers.fulllyQualifiedNameToInternalName(commonSuper);
    }

    /**
     * Returns the common super class of the two types only if one of them is the root type:
     * java/lang/Object.
     *
     * Returns NULL if neither type is a root type and so we can not quickly determine the super.
     */
    private String findCommonSuperIfOneTypeIsRoot(String type1dotName, String type2dotName) {
        if (type1dotName.equals(CommonType.JAVA_LANG_OBJECT.dotName) || type2dotName.equals(CommonType.JAVA_LANG_OBJECT.dotName)) {
            return Helpers.fulllyQualifiedNameToInternalName(CommonType.JAVA_LANG_OBJECT.dotName);
        }

        return null;
    }

    /**
     * Returns the only possible super class (java/lang/Object) if one of the two types is pre-rename
     * and the other is post-rename.
     *
     * Returns NULL if the two types are both pre-rename or else they are both post-rename, since
     * further investigation is required.
     */
    private String findCommonSuperIfOneTypeIsPreRenameAndOtherPostRename(String type1dotName, String type2dotName) {
        // In debug mode a user-defined class's post-rename and pre-rename forms are the same, so we exclude them.

        boolean type1isPreRename = (this.preserveDebuggability)
            ? !this.pocHierarchy.isPreRenameUserDefinedClass(type1dotName) && !ClassNameExtractor.isPostRenameClassDotStyle(type1dotName)
            : !ClassNameExtractor.isPostRenameClassDotStyle(type1dotName);

        boolean type2isPreRename = (this.preserveDebuggability)
            ? !this.pocHierarchy.isPreRenameUserDefinedClass(type2dotName) && !ClassNameExtractor.isPostRenameClassDotStyle(type2dotName)
            : !ClassNameExtractor.isPostRenameClassDotStyle(type2dotName);

        return (type1isPreRename == type2isPreRename)
            ? null
            : Helpers.fulllyQualifiedNameToInternalName(CommonType.JAVA_LANG_OBJECT.dotName);
    }

    /**
     * Returns the super class if one of the two types is an exception wrapper. This is only ever
     * java/lang/Throwable or java/lang/Object, but some inspection is required to determine which
     * one.
     *
     * Returns NULL if the neither type is an exception wrapper.
     */
    private String findCommonSuperIfOneTypeIsExceptionWrapper(String type1dotName, String type2dotName) {
        boolean type1isExceptionWrapper = ExceptionWrapperNameMapper.isExceptionWrapperDotName(type1dotName);
        boolean type2isExceptionWrapper = ExceptionWrapperNameMapper.isExceptionWrapperDotName(type2dotName);

        // If both types are exception wrappers we can safely return java/lang/Throwable because this
        // wrapper is always unwrapped and the real type discovered then.
        if (type1isExceptionWrapper && type2isExceptionWrapper) {
            return Helpers.fulllyQualifiedNameToInternalName(CommonType.JAVA_LANG_THROWABLE.dotName);
        }

        // If one type is an exception wrapper then we essentially want java/lang/Object or java/lang/Throwable
        // But we have to query the hierarchy to actually determine which of these we want.
        if (type1isExceptionWrapper || type2isExceptionWrapper) {

            // If the other type is java/lang/Throwable then we unify to Throwable.
            if (type1dotName.equals(CommonType.JAVA_LANG_THROWABLE.dotName) || (type2dotName.equals(CommonType.JAVA_LANG_THROWABLE.dotName))) {
                return Helpers.fulllyQualifiedNameToInternalName(CommonType.JAVA_LANG_THROWABLE.dotName);
            }

            // If the other type is pre-rename then we have to unify to java/lang/Object or java/lang/Throwable.
            // We actually have to query the hierarchy to determine this result.
            // In debug mode a user-defined class's post-rename form is the same as its pre-rename, so we don't count these.
            boolean type1isPreRename = (this.preserveDebuggability)
                ? !this.pocHierarchy.isPreRenameUserDefinedClass(type1dotName) && !ClassNameExtractor.isPostRenameClassDotStyle(type1dotName)
                : !ClassNameExtractor.isPostRenameClassDotStyle(type1dotName);

            boolean type2isPreRename = (this.preserveDebuggability)
                ? !this.pocHierarchy.isPreRenameUserDefinedClass(type2dotName) && !ClassNameExtractor.isPostRenameClassDotStyle(type2dotName)
                : !ClassNameExtractor.isPostRenameClassDotStyle(type2dotName);

            if (type1isPreRename || type2isPreRename) {

                // First, unwrap the exception wrapper.
                String type1queryName = (type1isExceptionWrapper) ? ExceptionWrapperNameMapper.dotClassNameForWrapperName(type1dotName) : type1dotName;
                String type2queryName = (type2isExceptionWrapper) ? ExceptionWrapperNameMapper.dotClassNameForWrapperName(type2dotName) : type2dotName;

                // Second, rename the pre-renamed class.
                type1queryName = (type1isPreRename) ? NonWrapperClassRenamer.toPostRenameClassName(type1queryName) : type1queryName;
                type2queryName = (type2isPreRename) ? NonWrapperClassRenamer.toPostRenameClassName(type2queryName) : type2queryName;

                // Third, query the hierarchy.
                String commonSuper = this.pocHierarchy.getTightestCommonSuperClass(type1queryName, type2queryName);

                // If we hit an Object type then we have to unify to java/lang/Object since the other type is not an exception.
                if (commonSuper.equals(CommonType.JAVA_LANG_OBJECT.dotName) || commonSuper.equals(CommonType.I_OBJECT.dotName) || commonSuper.equals(CommonType.SHADOW_OBJECT.dotName)) {
                    return Helpers.fulllyQualifiedNameToInternalName(CommonType.JAVA_LANG_OBJECT.dotName);
                }

                // Otherwise, the other type is an exception type so we can unify to java/lang/Throwable.
                return Helpers.fulllyQualifiedNameToInternalName(CommonType.JAVA_LANG_THROWABLE.dotName);
            }

            // Then the other type is post-rename and we can only unify to java/lang/Object since the
            // wrapper descends from java/lang/Throwable, which is unreachable from the post-rename classes.
            return Helpers.fulllyQualifiedNameToInternalName(CommonType.JAVA_LANG_OBJECT.dotName);
        }

        // Then neither type must be an exception wrapper, so we don't handle this here.
        return null;
    }

    /**
     * Returns the super class if one of the two types is an array wrapper. Note that an array wrapper
     * accounts for all array types EXCEPT one-dimensional primitive arrays.
     *
     * These are handled specially as regular objects, and since they are in the hierarchy, we can
     * unify other types against them simply by calling into the hierarchy - they require no special
     * handling, like array wrappers do. That is why they are not included here.
     *
     * Returns NULL if neither type is an array wrapper.
     */
    private String findCommonSuperIfOneTypeIsArrayWrapper(String type1dotName, String type2dotName) {
        boolean type1isExceptionWrapper = ExceptionWrapperNameMapper.isExceptionWrapperDotName(type1dotName);
        boolean type2isExceptionWrapper = ExceptionWrapperNameMapper.isExceptionWrapperDotName(type2dotName);

        boolean type1isPreRenameJclException = !type1isExceptionWrapper && CommonGenerators.isJclExceptionType(type1dotName);

        // In debug mode we don't want to count a user-defined class as pre-rename since its post-rename form is the same as its pre-rename form.
        boolean isPreRenameClass = (this.preserveDebuggability)
            ? !this.pocHierarchy.isPreRenameUserDefinedClass(type1dotName) && !ClassNameExtractor.isPostRenameClassDotStyle(type1dotName)
            : !ClassNameExtractor.isPostRenameClassDotStyle(type1dotName);

        boolean type1isPreRenameNonJclExceptionOrWrapper = !type1isExceptionWrapper && !type1isPreRenameJclException && isPreRenameClass;

        // Since we know both are either pre- or post-rename.
        boolean bothTypesArePreRename = type1isPreRenameJclException || type1isPreRenameNonJclExceptionOrWrapper;

        boolean type1isMultiDimPrimitiveArray = !type1isExceptionWrapper && !bothTypesArePreRename && ArrayUtil.isMultiDimensionalPrimitiveArray(NameStyle.DOT_NAME, type1dotName);
        boolean type2isMultiDimPrimitiveArray = !type2isExceptionWrapper && !bothTypesArePreRename && ArrayUtil.isMultiDimensionalPrimitiveArray(NameStyle.DOT_NAME, type2dotName);

        boolean type1isObjectArray = !type1isExceptionWrapper && !bothTypesArePreRename && !type1isMultiDimPrimitiveArray && ArrayUtil.isPostRenameObjectArray(NameStyle.DOT_NAME, type1dotName);
        boolean type2isObjectArray = !type2isExceptionWrapper && !bothTypesArePreRename && !type2isMultiDimPrimitiveArray && ArrayUtil.isPostRenameObjectArray(NameStyle.DOT_NAME, type2dotName);

        boolean type1isArray = type1isMultiDimPrimitiveArray || type1isObjectArray;
        boolean type2isArray = type2isMultiDimPrimitiveArray || type2isObjectArray;

        // Handle the case where both types are object arrays.
        if (type1isObjectArray && type2isObjectArray) {

            boolean type1isInterfaceObjectArray = ArrayUtil.isPostRenameUnifyingTypeObjectArray(NameStyle.DOT_NAME, type1dotName);
            boolean type2isInterfaceObjectArray = ArrayUtil.isPostRenameUnifyingTypeObjectArray(NameStyle.DOT_NAME, type2dotName);

            // If one type is an interface object array and the other is not, then IObjectArray is their unifying type.
            if (type1isInterfaceObjectArray != type2isInterfaceObjectArray) {
                return Helpers.fulllyQualifiedNameToInternalName(CommonType.I_OBJECT_ARRAY.dotName);
            }

            // If the two arrays differ in dimension, then we unify to IObjectArray.
            int array1dimension = ArrayUtil.dimensionOfPostRenameObjectArray(NameStyle.DOT_NAME, type1dotName);

            if (array1dimension != ArrayUtil.dimensionOfPostRenameObjectArray(NameStyle.DOT_NAME, type2dotName)) {
                return Helpers.fulllyQualifiedNameToInternalName(CommonType.I_OBJECT_ARRAY.dotName);
            }

            // Otherwise, we strip the arrays down to their base types and get the common super class of the base types
            // and then wrap them back up in the array wrappers and return.
            String type1stripped = ArrayRenamer.getObjectArrayWrapperUnderlyingTypeName(NameStyle.DOT_NAME, type1dotName);
            String type2stripped = ArrayRenamer.getObjectArrayWrapperUnderlyingTypeName(NameStyle.DOT_NAME, type2dotName);

            // We make a recursive call back into getCommonSuperClassViaNewClassHierarchy() -- this call can never
            // recurse any deeper than this, and it solves all our problems for us.
            String type1strippedSlashName = Helpers.fulllyQualifiedNameToInternalName(type1stripped);
            String type2strippedSlashName = Helpers.fulllyQualifiedNameToInternalName(type2stripped);

            String strippedCommonSuper = Helpers.internalNameToFulllyQualifiedName(getCommonSuperClass(type1strippedSlashName, type2strippedSlashName));

            // If we hit the 'top' of the hierarchy (The Object types) then we just go to IObjectArray
            if (strippedCommonSuper.equals(CommonType.JAVA_LANG_OBJECT.dotName) || strippedCommonSuper.equals(CommonType.I_OBJECT.dotName) || strippedCommonSuper.equals(CommonType.SHADOW_OBJECT.dotName)) {
                return Helpers.fulllyQualifiedNameToInternalName(CommonType.I_OBJECT_ARRAY.dotName);
            }

            // Finally, we reconstruct our answer as an array wrapper and return. We only ask if type1 is
            // an interface object array because we know type2 has the same answer.
            return type1isInterfaceObjectArray
                ? Helpers.fulllyQualifiedNameToInternalName(ArrayRenamer.wrapAsUnifyingObjectArray(NameStyle.DOT_NAME, strippedCommonSuper, array1dimension))
                : Helpers.fulllyQualifiedNameToInternalName(ArrayRenamer.wrapAsConcreteObjectArray(NameStyle.DOT_NAME, strippedCommonSuper, array1dimension));
        }

        boolean atLeastOneMultiDimPrimitiveArray = type1isMultiDimPrimitiveArray || type2isMultiDimPrimitiveArray;
        boolean atLeastOneObjectArray = type1isObjectArray || type2isObjectArray;

        // Handle the case where one of our types is a multi-dimensional primitive array
        if (atLeastOneMultiDimPrimitiveArray || atLeastOneObjectArray) {

            // Then we have a primitive and object array unifying, this must be IObjectArray.
            if (type1isArray && type2isArray) {
                return Helpers.fulllyQualifiedNameToInternalName(CommonType.I_OBJECT_ARRAY.dotName);
            }

            // Otherwise, since we have a non-array unifying with an array wrapper we return java/lang/Object.
            //TODO: we could be more precise and distinguish pre-post rename to determine if shadow Object/IObject are more appropriate. Is it worth it??
            return Helpers.fulllyQualifiedNameToInternalName(CommonType.JAVA_LANG_OBJECT.dotName);
        }

        // Neither type is an array wrapper then.
        return null;
    }
}
