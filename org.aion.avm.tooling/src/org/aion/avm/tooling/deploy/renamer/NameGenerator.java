package org.aion.avm.tooling.deploy.renamer;

import java.util.Set;

public class NameGenerator {
    private static char[] characters;
    private int currentClassIndex;
    private int currentInstructionIndex;

    public NameGenerator() {
        characters = new char[26];
        currentClassIndex = 1;
        for (int i = 97; i <= 122; i++) {
            characters[i - 97] = ((char) i);
        }
    }

    public String getNextClassName(Set<String> internalClassNames) {
        String className = nextString(currentClassIndex).toUpperCase();
        while (internalClassNames.contains(className)) {
            currentClassIndex++;
            className = nextString(currentClassIndex).toUpperCase();
        }
        currentClassIndex++;
        return className.toUpperCase();
    }

    // main class will always be mapped to A
    public static String getNewMainClassName() {
        return String.valueOf(characters[0]).toUpperCase();
    }

    public String getNextMethodOrFieldName(Set<String> restrictions) {
        String name = nextString(currentInstructionIndex);
        if (restrictions != null) {
            while (restrictions.contains(name)) {
                currentInstructionIndex++;
                name = nextString(currentInstructionIndex);
            }
        }
        currentInstructionIndex++;
        return name;
    }

    private String nextString(int i) {
        return i < 0 ? "" : nextString((i / 26) - 1) + characters[i % 26];
    }
}
