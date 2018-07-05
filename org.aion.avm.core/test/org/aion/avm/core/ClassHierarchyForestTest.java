package org.aion.avm.core;

import org.aion.avm.core.dappreading.LoadedJar;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static java.lang.String.format;

/**
 * @author Roman Katerinenko
 */
public class ClassHierarchyForestTest {

    @Test
    public void test() throws IOException {
        final var module = "com.example.foresttest";
        final Path path = Paths.get(format("%s/%s.jar", "../examples/build", module));
        LoadedJar jar = LoadedJar.fromBytes(Files.readAllBytes(path));
        final var forest = ClassHierarchyForest.createForestFrom(jar);
        Collection<Forest.Node<String, byte[]>> roots = forest.getRoots();
        Assert.assertEquals(2, roots.size());
        final var objectNode = forest.getNodeById("java.lang.Object");
        final var bigDecimalNode = forest.getNodeById("java.math.BigDecimal");
        Assert.assertTrue(roots.contains(objectNode));
        Assert.assertTrue(roots.contains(bigDecimalNode));
    }
}