package org.aion.avm.core.dappreading;

import org.aion.avm.core.util.Helpers;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static java.util.Arrays.sort;

/**
 * @author Roman Katerinenko
 */
public class LoadedJarTest {

    private static final String[] expectedReadClasses = {
            "com.example.twoclasses.C1",
            "com.example.twoclasses.C1$NestedClass",
            "com.example.twoclasses.C1$NestedInterface",
            "com.example.twoclasses.C1$InnerClass",
            "com.example.twoclasses.C2",
            "com.example.twoclasses.C2$1",
            "com.example.twoclasses.C2$NestedEnum",
            "com.example.twoclasses.JavaAccessor",
            "com.example.twoclasses.Main",
            "com.example.twoclasses.TestAnnotation",
            "com.example.twoclasses.innerpackage.C3"
    };

    static {
        sort(expectedReadClasses);
    }

    @Test
    public void checkExpectedClassesReadFromJar() throws IOException {
        final var module = "com.example.twoclasses";
        final var pathToJar = String.format("%s/%s.jar", "../examples/build", module);
        byte[] jarBytes = Helpers.readFileToBytes(pathToJar);
        Map<String, byte[]> actualClasses = LoadedJar.fromBytes(jarBytes).classBytesByQualifiedNames;
        checkIfMatchExpected(actualClasses);
    }

    private static void checkIfMatchExpected(Map<String, byte[]> actual) {
        Assert.assertEquals(expectedReadClasses.length, actual.size());
        final var actualClassesArray = actual.keySet().toArray();
        sort(actualClassesArray);
        Assert.assertArrayEquals(expectedReadClasses, actualClassesArray);
    }
}