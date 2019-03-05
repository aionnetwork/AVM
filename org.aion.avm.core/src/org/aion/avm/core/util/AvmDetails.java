package org.aion.avm.core.util;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.arraywrapping.ArrayNameMapper;
import org.aion.avm.internal.PackageConstants;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AvmDetails {
    private static List<Class<?>> omittedClasses = new ArrayList<>(
            Arrays.asList(org.aion.avm.shadow.java.lang.invoke.LambdaMetafactory.class, org.aion.avm.shadow.java.lang.invoke.StringConcatFactory.class));

    /**
     * @return Map of supported Class objects to a list of supported methods.
     * @throws ClassNotFoundException
     */
    public static Map<Class<?>, List<MethodDescriptor>> getClassLibraryWhiteList() throws ClassNotFoundException {
        Map<Class<?>, List<MethodDescriptor>> classDeclaredMethodMap = new HashMap<>();
        List<Class<?>> shadowClasses = getCallableShadowClasses();

        for (Class c : shadowClasses) {
            String associatedJclName = mapClassName(c.getName());
            Class jclClass = Class.forName(associatedJclName);

            List<MethodDescriptor> declaredMethodList = Stream.of(c.getDeclaredMethods(), c.getDeclaredConstructors())
                    .flatMap(Stream::of)
                    .filter(method -> isSupportedExecutable(method) && hasValidParamTypes(method))
                    .map(AvmDetails::generateMethodDescriptor)
                    .collect(Collectors.toList());
            classDeclaredMethodMap.put(jclClass, declaredMethodList);
        }
        return classDeclaredMethodMap;
    }

    private static List<Class<?>> getCallableShadowClasses() {
        List<Class<?>> shadowClasses = new ArrayList<>(NodeEnvironment.singleton.getShadowClasses());
        shadowClasses.removeAll(omittedClasses);
        return shadowClasses;
    }

    private static String mapClassName(String className) {
        if (isShadowClass(className)) {
            return className.substring(PackageConstants.kShadowDotPrefix.length());
        } else if (isArrayWrapperClass(className)) {
            return ArrayNameMapper.getElementNameFromWrapper(Helpers.fulllyQualifiedNameToInternalName(className));
        } else if (isSupportedInternalType(className)) {
            return "java.lang.Object";
        } else {
            return className;
        }
    }

    private static MethodDescriptor generateMethodDescriptor(Executable method) {
        String methodName = mapMethodName(method);
        try {
            Class<?>[] parameterTypes = mapParameterTypes(method.getParameterTypes());
            return new MethodDescriptor(methodName, parameterTypes, Modifier.isStatic(method.getModifiers()));

        } catch (ClassNotFoundException e) {
            // Parameter class could not be located.
            throw new AssertionError(e);
        }
    }

    private static Class<?>[] mapParameterTypes(Class<?>[] methodParameterTypes) throws ClassNotFoundException {
        Class<?>[] mappedTypes = new Class<?>[methodParameterTypes.length];
        for (int i = 0; i < methodParameterTypes.length; i++) {
            if (isPrimitive(methodParameterTypes[i])) {
                mappedTypes[i] = methodParameterTypes[i];
            } else {
                mappedTypes[i] = Class.forName(mapClassName(methodParameterTypes[i].getName()));
            }
        }
        return mappedTypes;
    }

    private static String mapMethodName(Executable method) {
        if (method instanceof Constructor) {
            return Modifier.isStatic(method.getModifiers()) ? "<clinit>" : "<init>";
        } else {
            return method.getName().substring("avm_".length());
        }
    }

    private static boolean hasValidParamTypes(Executable method) {
        for (Class<?> c : method.getParameterTypes()) {
            if (!isShadowClass(c.getName()) &&
                    !isArrayWrapperClass(c.getName()) &&
                    !isPrimitive(c) &&
                    !isSupportedInternalType(c.getName()))
                return false;
        }
        return true;
    }

    private static boolean isShadowClass(String className) {
        return className.startsWith(PackageConstants.kShadowDotPrefix);
    }

    private static boolean isArrayWrapperClass(String className) {
        return className.startsWith(PackageConstants.kArrayWrapperDotPrefix);
    }

    private static boolean isSupportedInternalType(String className) {
        return className.equals(PackageConstants.kInternalDotPrefix + "IObject");
    }

    private static boolean isSupportedExecutable(Executable method) {
        if (method instanceof Constructor) {
            return Modifier.isPublic(method.getModifiers());
        } else
            return method.getName().startsWith("avm_");
    }

    private static boolean isPrimitive(Class<?> type) {
        if (type == null) {
            return false;
        }
        return type.isPrimitive();
    }

    public static class MethodDescriptor {
        public final String name;
        public final Class<?>[] parameters;
        public final boolean isStatic;

        public MethodDescriptor(String name, Class<?>[] parameters, boolean isStatic) {
            this.name = name;
            this.parameters = parameters;
            this.isStatic = isStatic;
        }

        @Override
        public String toString() {
            return "MethodDescriptor{" +
                    "name='" + name + '\'' +
                    ", parameters=" + Arrays.toString(parameters) +
                    ", isStatic=" + isStatic +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodDescriptor that = (MethodDescriptor) o;
            return isStatic == that.isStatic &&
                    name.equals(that.name) &&
                    Arrays.equals(parameters, that.parameters);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(name, isStatic);
            result = 31 * result + Arrays.hashCode(parameters);
            return result;
        }
    }

}
