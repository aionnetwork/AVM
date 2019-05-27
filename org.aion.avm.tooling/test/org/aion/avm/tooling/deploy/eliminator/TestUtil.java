package org.aion.avm.tooling.deploy.eliminator;

import java.util.HashMap;
import java.util.Map;
import org.aion.avm.core.util.Helpers;

public class TestUtil {

    public static Map<String, byte[]> makeClassMap(Class... classes) {
        Map<String, byte[]> classMap = new HashMap<>();

        for (Class clazz : classes) {
            String slashName = Helpers.fulllyQualifiedNameToInternalName(clazz.getName());
            byte[] classBytes = Helpers.loadRequiredResourceAsBytes(slashName + ".class");
            classMap.put(slashName, classBytes);
        }

        return classMap;
    }
}
