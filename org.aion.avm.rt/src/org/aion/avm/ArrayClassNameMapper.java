package org.aion.avm;

import org.aion.avm.internal.PackageConstants;

import java.util.HashMap;
import java.util.Map;

public class ArrayClassNameMapper {
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
