package org.aion.avm;

import java.util.regex.Pattern;
import org.aion.avm.internal.PackageConstants;

import java.util.HashMap;
import java.util.Map;
import org.aion.avm.internal.RuntimeAssertionError;

public class ArrayClassNameMapper {
    private static final Pattern PRIMITIVE_PATTERN = Pattern.compile("^\\${2,}[IBZCFSJD]$");

    static private HashMap<String, String> CLASS_WRAPPER_MAP = new HashMap<>();
    static private HashMap<String, String> INTERFACE_WRAPPER_MAP = new HashMap<>();

    static {
        CLASS_WRAPPER_MAP.put("[I", PackageConstants.kArrayWrapperSlashPrefix + "IntArray");
        CLASS_WRAPPER_MAP.put("[B", PackageConstants.kArrayWrapperSlashPrefix + "ByteArray");
        CLASS_WRAPPER_MAP.put("[Z", PackageConstants.kArrayWrapperSlashPrefix + "BooleanArray");
        CLASS_WRAPPER_MAP.put("[C", PackageConstants.kArrayWrapperSlashPrefix + "CharArray");
        CLASS_WRAPPER_MAP.put("[F", PackageConstants.kArrayWrapperSlashPrefix + "FloatArray");
        CLASS_WRAPPER_MAP.put("[S", PackageConstants.kArrayWrapperSlashPrefix + "ShortArray");
        CLASS_WRAPPER_MAP.put("[J", PackageConstants.kArrayWrapperSlashPrefix + "LongArray");
        CLASS_WRAPPER_MAP.put("[D", PackageConstants.kArrayWrapperSlashPrefix + "DoubleArray");
        CLASS_WRAPPER_MAP.put("[Ljava/lang/Object", PackageConstants.kArrayWrapperSlashPrefix + "ObjectArray");
        CLASS_WRAPPER_MAP.put("[L" + PackageConstants.kShadowSlashPrefix + "java/lang/Object", PackageConstants.kArrayWrapperSlashPrefix + "ObjectArray");
        CLASS_WRAPPER_MAP.put("[L" + PackageConstants.kInternalSlashPrefix + "IObject", PackageConstants.kArrayWrapperSlashPrefix + "ObjectArray");

        // some special-case unifications to IObjectArray.
        INTERFACE_WRAPPER_MAP.put("[L" + PackageConstants.kInternalSlashPrefix + "IObject", PackageConstants.kInternalSlashPrefix + "IObjectArray");
        INTERFACE_WRAPPER_MAP.put("L" + PackageConstants.kArrayWrapperSlashPrefix + "ObjectArray", PackageConstants.kInternalSlashPrefix + "IObjectArray");
        INTERFACE_WRAPPER_MAP.put("[L" + PackageConstants.kShadowSlashPrefix + "java/lang/Object", PackageConstants.kInternalSlashPrefix + "IObjectArray");
    }

    public static boolean isObjectArrayWrapperDotName(String typeName) {
        RuntimeAssertionError.assertTrue(-1 == typeName.indexOf("/"));
        return isInterfaceObjectArrayDotName(typeName) || isNonInterfaceObjectArrayDotName(typeName);
    }

    public static String stripArrayWrapperDotName(String typeName) {
        RuntimeAssertionError.assertTrue(-1 == typeName.indexOf("/"));
        return typeName.substring(PackageConstants.kArrayWrapperDotPrefix.length());
    }

    public static String stripInterfaceObjectArrayWrapperDotName(String typeName) {
        RuntimeAssertionError.assertTrue(-1 == typeName.indexOf("/"));
        return typeName.substring((PackageConstants.kArrayWrapperDotPrefix + "interface.").length());
    }

    public static boolean isNonInterfaceObjectArrayDotName(String typeName) {
        RuntimeAssertionError.assertTrue(-1 == typeName.indexOf("/"));
        return typeName.startsWith(PackageConstants.kArrayWrapperDotPrefix + "$");
    }

    public static boolean isMultiDimensionalPrimitiveArrayDotName(String typeName) {
        RuntimeAssertionError.assertTrue(-1 == typeName.indexOf("/"));

        if (typeName.startsWith(PackageConstants.kArrayWrapperDotPrefix)) {
            return PRIMITIVE_PATTERN.matcher(stripArrayWrapperDotName(typeName)).matches();
        }

        return false;
    }

    public static boolean isInterfaceObjectArrayDotName(String typeName) {
        RuntimeAssertionError.assertTrue(-1 == typeName.indexOf("/"));
        return typeName.startsWith(PackageConstants.kArrayWrapperDotPrefix + "interface.");
    }

    public static String stripInterfaceObjectArrayWrapperDotNameToBaseType(String typeName) {
        RuntimeAssertionError.assertTrue(isInterfaceObjectArrayDotName(typeName));

        int dimension = getInterfaceObjectArrayDotNameDimension(typeName);

        String strippedName = stripInterfaceObjectArrayWrapperDotName(typeName);

        // The +1 is to account for the 'L' just before the base type name.
        return strippedName.substring(dimension + 1);
    }

    public static String stripNonInterfaceObjectArrayWrapperDotNameToBaseType(String typeName) {
        RuntimeAssertionError.assertTrue(!isInterfaceObjectArrayDotName(typeName));

        int dimension = getNonInterfaceObjectArrayDotNameDimension(typeName);

        String strippedName = stripArrayWrapperDotName(typeName);

        // The +1 is to account for the 'L' just before the base type name.
        return strippedName.substring(dimension + 1);
    }

    public static String stripObjectArrayWrapperDotNameToBaseType(String typeName) {
        return (isInterfaceObjectArrayDotName(typeName))
            ? stripInterfaceObjectArrayWrapperDotNameToBaseType(typeName)
            : stripNonInterfaceObjectArrayWrapperDotNameToBaseType(typeName);
    }

    public static String wrapTypeDotNameAsInterfaceObjectArray(int dimension, String typeName) {
        RuntimeAssertionError.assertTrue(-1 == typeName.indexOf("/"));

        String dimensionString = new String(new char[dimension]).replaceAll("\0", "_");
        return PackageConstants.kArrayWrapperDotPrefix + "interface." + dimensionString + "L" + typeName;
    }

    public static String wrapTypeDotNameAsNonInterfaceObjectArray(int dimension, String typeName) {
        RuntimeAssertionError.assertTrue(-1 == typeName.indexOf("/"));

        String dimensionString = new String(new char[dimension]).replaceAll("\0", "\\$");
        return PackageConstants.kArrayWrapperDotPrefix + dimensionString + "L" + typeName;
    }

    public static int getInterfaceObjectArrayDotNameDimension(String typeName) {
        RuntimeAssertionError.assertTrue(isInterfaceObjectArrayDotName(typeName));

        String strippedName = stripInterfaceObjectArrayWrapperDotName(typeName);
        int length = strippedName.length();

        int dimension = 0;
        for (int i = 0; i < length; i++) {
            if (strippedName.charAt(i) != '_') {
                return dimension;
            }

            dimension++;
        }

        return dimension;
    }

    public static int getNonInterfaceObjectArrayDotNameDimension(String typeName) {
        RuntimeAssertionError.assertTrue(!isInterfaceObjectArrayDotName(typeName));

        String strippedName = stripArrayWrapperDotName(typeName);
        int length = strippedName.length();

        int dimension = 0;
        for (int i = 0; i < length; i++) {
            if (strippedName.charAt(i) != '$') {
                return dimension;
            }

            dimension++;
        }

        return dimension;
    }

    public static int getObjectArrayDotNameDimension(String typeName) {
        return (isInterfaceObjectArrayDotName(typeName))
            ? getInterfaceObjectArrayDotNameDimension(typeName)
            : getNonInterfaceObjectArrayDotNameDimension(typeName);
    }

    public static String getClassWrapper(String desc) {
        return CLASS_WRAPPER_MAP.get(desc);
    }

    public static String getInterfaceWrapper(String desc) {
        return INTERFACE_WRAPPER_MAP.get(desc);
    }

    public static String addClassWrapperDescriptor(String desc, String wrapper) {
        CLASS_WRAPPER_MAP.putIfAbsent(desc, wrapper);
        return CLASS_WRAPPER_MAP.get(desc);
    }

    public static java.lang.String addInterfaceWrapperDescriptor(String desc, String wrapper) {
        INTERFACE_WRAPPER_MAP.putIfAbsent(desc, wrapper);
        return INTERFACE_WRAPPER_MAP.get(desc);
    }

    public static String getElementNameFromWrapper(String wrapperClassName) {
        for (Map.Entry entry : CLASS_WRAPPER_MAP.entrySet()) {
            if (entry.getValue().equals(wrapperClassName)) {
                return entry.getKey().toString();
            }
        }
        return null;
    }

}
