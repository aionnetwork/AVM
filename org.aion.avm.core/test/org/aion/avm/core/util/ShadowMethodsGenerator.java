package org.aion.avm.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;

public class ShadowMethodsGenerator {

    public static void main(String args[]) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java ShadowMethodsGenerator [className]");
            System.exit(1);
        }

        // TODO: incorporate shadowing and array wrapping


        Class<?> clazz = Class.forName(args[0]);
        StringBuilder sb = new StringBuilder();


        Field[] fields = clazz.getDeclaredFields();
        Arrays.sort(fields, Comparator.comparing(Field::getName));
        for (Field f : fields) {
            int modifiers = f.getModifiers();

            if (Modifier.isPublic(modifiers)) {
                sb.append(Modifier.toString(modifiers));
                sb.append(" " + f.getType().getSimpleName());
                sb.append(" avm_" + f.getName());
                sb.append(";\n\n");
            }
        }

        Method[] methods = clazz.getDeclaredMethods();
        Arrays.sort(methods, Comparator.comparing(Method::getName));
        for (Method m : methods) {
            int modifiers = m.getModifiers();

            if (Modifier.isPublic(modifiers)) {
                sb.append(Modifier.toString(modifiers));
                sb.append(" " + m.getReturnType().getSimpleName());
                sb.append(" avm_" + m.getName());
                sb.append("(");
                Class<?>[] parameters = m.getParameterTypes();
                for (int i = 0; i < parameters.length; i++) {
                    sb.append((i == 0 ? "" : ", ") + parameters[i].getSimpleName());
                    sb.append(" " + (char) ('a' + i));
                }
                sb.append(")\n{\n}\n\n");
            }
        }

        System.out.println(sb);
    }
}
