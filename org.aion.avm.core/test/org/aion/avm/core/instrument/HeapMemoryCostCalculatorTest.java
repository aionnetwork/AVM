package org.aion.avm.core.instrument;

import org.aion.avm.core.types.Forest;
import org.aion.avm.core.dappreading.LoadedJar;
import org.aion.avm.core.ClassHierarchyForest;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.junit.Assert.*;

public class HeapMemoryCostCalculatorTest {

    @Test
    public void testCalcClassesInstanceSize() throws IOException {
        final var module = "com.example.heapsizecalctest";
        final Path path = Paths.get(format("%s/%s.jar", "../examples/build", module));
        LoadedJar jar = LoadedJar.fromBytes(Files.readAllBytes(path));
        final var forest = ClassHierarchyForest.createForestFrom(jar);

        Collection<Forest.Node<String, byte[]>> roots = forest.getRoots();
        Assert.assertEquals(1, roots.size());

        final var heapCalc = new HeapMemoryCostCalculator();
        Map<String, Integer> runtimeObjectSizes = new HashMap<>();

        runtimeObjectSizes.put("java/lang/Object", 0);
        heapCalc.calcClassesInstanceSize(forest, runtimeObjectSizes);

        Map<String, Integer> result = heapCalc.getClassHeapSizeMap();

        final var moduleName = "com/example/heapsizecalctest";
        assertEquals(4, (long)result.get(moduleName + "/A"));
        assertEquals(22, (long)result.get(moduleName + "/AB"));
        assertEquals(26, (long)result.get(moduleName + "/ABC"));
        assertEquals(42, (long)result.get(moduleName + "/ABD"));
        assertEquals(0, (long)result.get(moduleName + "/E"));
        assertEquals(8, (long)result.get(moduleName + "/EF"));
    }
}
