package org.aion.avm.core.instrument;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.core.types.RawDappModule;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class HeapMemoryCostCalculatorTest {

    @Test
    public void getHeapSizeInheritance() {
        Map<String, Integer> userObjectSizes = getObjectSizes(HeapSizeTestTarget.class, HeapSizeChildTarget.class);
        assertSize(userObjectSizes, HeapSizeTestTarget.class.getName(), 54);
        assertSize(userObjectSizes, HeapSizeChildTarget.class.getName(), 54 + 4 + 8 + 8);
    }

    @Test
    public void getHeapSizeUserlib() {
        Map<String, Integer> objectSizes = getObjectSizes(ABIDecoder.class, AionMap.class);

        assertSize(objectSizes, ABIDecoder.class.getName(), 28);
        assertSize(objectSizes, AionMap.class.getName(), 16 + 4 + 4 + 4 + 4 + 8);
        assertSize(objectSizes, "org.aion.avm.userlib.AionMap$AionAbstractCollection", 16 + 8);
        assertSize(objectSizes, "org.aion.avm.userlib.AionMap$KeySet", 16 + 8 + 8);
        // object + Aion Map reference + fields
        assertSize(objectSizes, "org.aion.avm.userlib.AionMap$HashIterator", 16 + 8 + 8 + 8 + 4 + 4);
        // object + HashIterator size as parent + Aion Map reference
        assertSize(objectSizes, "org.aion.avm.userlib.AionMap$ValueIterator", 16 + 8 + 8 + 8 + 4 + 4 + 8);
        // object + fields
        assertSize(objectSizes, "org.aion.avm.userlib.AionMap$AionMapEntry", 16 + 8 + 4 + 8 + 8);
    }

    @Test
    public void getHeapSizeException() {
        Map<String, Integer> objectSizes = getObjectSizes(HeapSizeExceptionTarget.class);
        assertSize(objectSizes, HeapSizeExceptionTarget.class.getName(), 16);
        // Exception + String as field
        assertSize(objectSizes, HeapSizeExceptionTarget.UserDefinedException.class.getName(), 40);
    }

    private Map<String, Integer> getObjectSizes(Class mainClass, Class<?>... classes) {
        Assert.assertNotNull(NodeEnvironment.singleton);
        byte[] jar = UserlibJarBuilder.buildJarForMainAndClasses(mainClass, classes);
        RawDappModule rawDapp = RawDappModule.readFromJar(jar, false, false);
        HeapMemoryCostCalculator objectSizeCalculator = new HeapMemoryCostCalculator();
        objectSizeCalculator.calcClassesInstanceSize(rawDapp.classHierarchyForest);
        return objectSizeCalculator.getClassHeapSizeMap();
    }

    @Test
    public void getHeapSizeEnum() {
        Map<String, Integer> objectSizes = getObjectSizes(ClassWithEnum.class);
        assertSize(objectSizes, ClassWithEnum.class.getName(), 16);
        assertSize(objectSizes, ClassWithEnum.Days.class.getName(), 28);
    }

    private void assertSize(Map<String, Integer> userObjectSizes, String className, int size) {
        Assert.assertEquals(size, (int) userObjectSizes.get(Utilities.fulllyQualifiedNameToInternalName(className)));
    }

}
