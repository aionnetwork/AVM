package org.aion.avm.core.dappreading;

import org.junit.Assert;
import org.junit.Test;

import legacy_examples.twoclasses.C1;
import legacy_examples.twoclasses.C2;
import legacy_examples.twoclasses.JavaAccessor;
import legacy_examples.twoclasses.Main;
import legacy_examples.twoclasses.TestAnnotation;
import legacy_examples.twoclasses.innerpackage.C3;

import java.io.IOException;
import java.util.Map;

import static java.util.Arrays.sort;


public class LoadedJarTest {

    private static final String[] expectedReadClasses = {
            "legacy_examples.twoclasses.C1",
            "legacy_examples.twoclasses.C1$NestedClass",
            "legacy_examples.twoclasses.C1$NestedInterface",
            "legacy_examples.twoclasses.C1$InnerClass",
            "legacy_examples.twoclasses.C2",
            "legacy_examples.twoclasses.C2$1",
            "legacy_examples.twoclasses.C2$NestedEnum",
            "legacy_examples.twoclasses.JavaAccessor",
            "legacy_examples.twoclasses.Main",
            "legacy_examples.twoclasses.TestAnnotation",
            "legacy_examples.twoclasses.innerpackage.C3"
    };

    static {
        sort(expectedReadClasses);
    }

    @Test
    public void checkExpectedClassesReadFromJar() throws IOException {
        byte[] jarBytes = UserlibJarBuilder.buildJarForMainAndClasses(Main.class, C1.class, C2.class, JavaAccessor.class, TestAnnotation.class, C3.class);
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