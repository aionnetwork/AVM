package org.aion.avm.core.dappreading;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static java.util.Arrays.sort;

/**
 * @author Roman Katerinenko
 */
public class DAppReaderWriterTest {
    private static final String[] expectedReadClasses = {
            "org.aion.avm.separatejars.twotestclasses.TestAnnotation",
            "org.aion.avm.separatejars.twotestclasses.C1",
            "org.aion.avm.separatejars.twotestclasses.C1$NestedClass",
            "org.aion.avm.separatejars.twotestclasses.C1$NestedInterface",
            "org.aion.avm.separatejars.twotestclasses.C1$InnerClass",
            "org.aion.avm.separatejars.twotestclasses.C2",
            "org.aion.avm.separatejars.twotestclasses.C2$1",
            "org.aion.avm.separatejars.twotestclasses.C2$NestedEnum",
            "org.aion.avm.separatejars.twotestclasses.innerpackage.C3"
    };

    static {
        sort(expectedReadClasses);
    }

    @Test
    public void checkExpectedClassesReadFromJar() throws IOException {
        Map<String, byte[]> actualClasses = new DAppReaderWriter().readClassesFromJar(makePathToJarWithName("twotestclasses"));
        checkIfMatchExpected(actualClasses);
    }

    @Test
    public void checkExpectedClassesReadFromDir() throws IOException {
        String dirPath = makePathToFolderWithName("twotestclasses");
        final var packagePrefix = "org.aion.avm.separatejars.twotestclasses";
        Map<String, byte[]> actualClasses = new DAppReaderWriter().readClassesFromDir(dirPath, packagePrefix);
        checkIfMatchExpected(actualClasses);
    }

    private static void checkIfMatchExpected(Map<String, byte[]> actual) {
        Assert.assertEquals(expectedReadClasses.length, actual.size());
        final var actualClassesArray = actual.keySet().toArray();
        sort(actualClassesArray);
        Assert.assertArrayEquals(expectedReadClasses, actualClassesArray);
    }

    private static String makePathToFolderWithName(String rootDirName) {
        return "../org.aion.avm.examples/build/main/org/aion/avm/separatejars/" + rootDirName;
    }

    private static String makePathToJarWithName(String jarNameNoSuffix) {
        return String.format("%s-%s.jar", "../build/main/org-aion-avm-examples", jarNameNoSuffix);
    }
}