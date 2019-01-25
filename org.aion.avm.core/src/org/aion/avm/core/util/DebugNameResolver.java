package org.aion.avm.core.util;

import org.aion.avm.internal.PackageConstants;

public class DebugNameResolver {

    public static String getUserPackageSlashPrefix (String name, boolean debugMode){
        return debugMode? name: PackageConstants.kUserSlashPrefix + name;
    }

    public static String getUserPackageDotPrefix (String name, boolean debugMode){
        return debugMode? name: PackageConstants.kUserDotPrefix + name;
    }
}
