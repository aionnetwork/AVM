package org.aion.avm.tooling.deploy.eliminator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;

public class TestUtil {

    public static Map<String, byte[]> makeClassMap(Class<?>... classes) {
        Map<String, byte[]> classMap = new HashMap<>();

        for (Class<?> clazz : classes) {
            String slashName = Helpers.fulllyQualifiedNameToInternalName(clazz.getName());
            byte[] classBytes = Helpers.loadRequiredResourceAsBytes(slashName + ".class");
            classMap.put(slashName, classBytes);
        }

        return classMap;
    }

    public static byte[] serializeClassesAsJar(Class<?> mainClass, Class<?>... others) {
        Map<String, byte[]> loadedClasses = Arrays.stream(others)
            .map(c -> c.getName())
            .collect(Collectors.toMap(c -> c, c -> Helpers.loadRequiredResourceAsBytes(Helpers.fulllyQualifiedNameToInternalName(c) + ".class")));
        String qualifiedClassName = mainClass.getName();
        String internalName = Helpers.fulllyQualifiedNameToInternalName(qualifiedClassName);
        byte[] mainClassBytes = Helpers.loadRequiredResourceAsBytes(internalName + ".class");
        return JarBuilder.buildJarForExplicitClassNamesAndBytecode(qualifiedClassName, mainClassBytes, loadedClasses);
    }
}
