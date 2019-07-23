package org.aion.avm.tooling.deploy.eliminator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.aion.avm.tooling.util.JarBuilder;
import org.aion.avm.tooling.util.Utilities;


public class TestUtil {
    public static Map<String, byte[]> makeClassMap(Class<?>... classes) {
        Map<String, byte[]> classMap = new HashMap<>();

        for (Class<?> clazz : classes) {
            String slashName = Utilities.fulllyQualifiedNameToInternalName(clazz.getName());
            byte[] classBytes = Utilities.loadRequiredResourceAsBytes(slashName + ".class");
            classMap.put(slashName, classBytes);
        }

        return classMap;
    }

    public static byte[] serializeClassesAsJar(Class<?> mainClass, Class<?>... others) {
        Map<String, byte[]> loadedClasses = Arrays.stream(others)
            .map(c -> c.getName())
            .collect(Collectors.toMap(c -> c, c -> Utilities.loadRequiredResourceAsBytes(Utilities.fulllyQualifiedNameToInternalName(c) + ".class")));
        return JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(mainClass, loadedClasses);
    }
}
