package org.aion.avm.core.dappreading;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static java.util.Arrays.sort;

/**
 * @author Roman Katerinenko
 */
public class DAppReaderWriterTest {
    private static final String dAppBase = "../build/main/org-aion-avm-examples";

    @Test
    public void given_2classesDApp_then_2classesRead() throws IOException {
        final var expectedReadClasses = new String[]{
                "org.aion.avm.separatejars.twotestclasses.TestAnnotation",
                "org.aion.avm.separatejars.twotestclasses.C1",
                "org.aion.avm.separatejars.twotestclasses.C1$NestedClass",
                "org.aion.avm.separatejars.twotestclasses.C1$NestedInterface",
                "org.aion.avm.separatejars.twotestclasses.C1$InnerClass",
                "org.aion.avm.separatejars.twotestclasses.C2",
                "org.aion.avm.separatejars.twotestclasses.C2$1",
                "org.aion.avm.separatejars.twotestclasses.C2$NestedEnum"
        };
        sort(expectedReadClasses);
        Map<String, byte[]> actualClasses = DAppReaderWriter.readClassesFrom(makePathTo("twotestclasses"));
        Assert.assertEquals(expectedReadClasses.length, actualClasses.size());
        Object[] actualClassesArray = actualClasses.keySet().toArray();
        sort(actualClassesArray);
        Assert.assertArrayEquals(expectedReadClasses, actualClassesArray);
    }

    private static String makePathTo(String rootDirOrJarName) {
        return String.format("%s-%s.jar", dAppBase, rootDirOrJarName);
    }
}