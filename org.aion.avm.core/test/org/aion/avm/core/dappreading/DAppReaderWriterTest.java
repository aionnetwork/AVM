package org.aion.avm.core.dappreading;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static java.util.Arrays.sort;

/**
 * @author Roman Katerinenko
 */
public class DAppReaderWriterTest {
    private static final String[] expectedReadClasses = {
            "com.example.twoclasses.Main",
            "com.example.twoclasses.TestAnnotation",
            "com.example.twoclasses.C1",
            "com.example.twoclasses.C1$NestedClass",
            "com.example.twoclasses.C1$NestedInterface",
            "com.example.twoclasses.C1$InnerClass",
            "com.example.twoclasses.C2",
            "com.example.twoclasses.C2$1",
            "com.example.twoclasses.C2$NestedEnum",
            "com.example.twoclasses.innerpackage.C3"
    };

    static {
        sort(expectedReadClasses);
    }

    @Ignore
    @Test
    public void checkExpectedClassesReadFromJar() throws IOException {
        final var module = "com.example.twoclasses";
        Map<String, byte[]> actualClasses = new DAppReaderWriter().readClassesFromJar(makePathToJarWithName(module));
        checkIfMatchExpected(actualClasses);
    }

    @Ignore
    @Test
    public void checkExpectedClassesReadFromDir() throws IOException {
        final var module = "com.example.twoclasses";
        String dirPath = makePathToFolderWithName(module);
        Map<String, byte[]> actualClasses = new DAppReaderWriter().readClassesFromDir(dirPath, module);
        checkIfMatchExpected(actualClasses);
    }

    private static void checkIfMatchExpected(Map<String, byte[]> actual) {
        Assert.assertEquals(expectedReadClasses.length, actual.size());
        final var actualClassesArray = actual.keySet().toArray();
        sort(actualClassesArray);
        Assert.assertArrayEquals(expectedReadClasses, actualClassesArray);
    }

    private static String makePathToFolderWithName(String rootDirName) {
        return String.format("%s/%s", "../examples/build", rootDirName);
    }

    private static String makePathToJarWithName(String jarNameNoSuffix) {
        return String.format("%s/%s.jar", "../examples/build", jarNameNoSuffix);
    }
}