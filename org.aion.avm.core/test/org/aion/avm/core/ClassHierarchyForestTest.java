package org.aion.avm.core;

import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.core.dappreading.LoadedJar;
import org.aion.avm.core.types.ClassInfo;
import org.aion.avm.core.types.Forest;
import org.junit.Assert;
import org.junit.Test;

import legacy_examples.foresttest.A;
import legacy_examples.foresttest.AB;
import legacy_examples.foresttest.ABC;
import legacy_examples.foresttest.ABD;
import legacy_examples.foresttest.E;
import legacy_examples.foresttest.F;
import legacy_examples.foresttest.InterFace;

import java.io.IOException;
import java.util.Collection;


public class ClassHierarchyForestTest {

    @Test
    public void test() throws IOException {
        LoadedJar jar = LoadedJar.fromBytes(UserlibJarBuilder.buildJarForMainAndClasses(A.class, AB.class, ABC.class, ABD.class, E.class, F.class, InterFace.class));
        final var forest = ClassHierarchyForest.createForestFrom(jar);
        Collection<Forest.Node<String, ClassInfo>> roots = forest.getRoots();
        Assert.assertEquals(2, roots.size());
        final var objectNode = forest.getNodeById("java.lang.Object");
        final var bigDecimalNode = forest.getNodeById("java.math.BigDecimal");
        Assert.assertTrue(roots.contains(objectNode));
        Assert.assertTrue(roots.contains(bigDecimalNode));
    }
}