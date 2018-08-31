package org.aion.avm.core;

import org.aion.avm.core.classgeneration.CommonGenerators;
import org.aion.avm.internal.PackageConstants;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class NodeEnvironmentTest {

    @Test
    public void printShadowJCL() {
        Map<String, List<String>> classMethods = new TreeMap<>();

        List<Class<?>> shadowClasses = NodeEnvironment.singleton.getShadowClasses();
        for (Class<?> clazz : shadowClasses) {
            String name = clazz.getName().replaceAll(PackageConstants.kShadowDotPrefix, "");
            List<String> m = new ArrayList<>();
            // TODO: parse method by reflection
            classMethods.put(name, m);
        }

        String[] exceptionClassNames = CommonGenerators.kExceptionClassNames;
        for (String name : exceptionClassNames) {
            List<String> m = new ArrayList<>();
            // TODO: add methods based on rules
            classMethods.put(name, m);
        }

        for (String name : classMethods.keySet()) {
            System.out.println(name);
        }
    }
}
