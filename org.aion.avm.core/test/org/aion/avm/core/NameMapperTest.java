package org.aion.avm.core;

import static org.junit.Assert.assertEquals;

import org.aion.avm.NameStyle;
import org.aion.avm.core.arraywrapping.ArrayNameMapper;
import org.aion.avm.internal.PackageConstants;
import org.junit.Test;

public class NameMapperTest {

    @Test
    public void testUnifyingArrayWrapperDescriptor() {
        String primitive1D = byte[].class.getName();
        String primitiveMD = short[][].class.getName();
        String object1DdotName = java.lang.Number[].class.getName();
        String objectMDdotName = java.lang.Number[][].class.getName();
        String interfaceObjectMDdotName = java.lang.CharSequence[].class.getName();

        String object1DslashName = object1DdotName.replaceAll("\\.", "/");
        String objectMDslashName = objectMDdotName.replaceAll("\\.", "/");
        String interfaceObjectMDslashName = interfaceObjectMDdotName.replaceAll("\\.", "/");

        String expectedPrimitive1DdotName = "org.aion.avm.arraywrapper.ByteArray";
        String expectedPrimitiveMDdotName = "org.aion.avm.arraywrapper.$$S";
        String expectedObject1DdotName = "org.aion.avm.arraywrapper.interface._Ljava.lang.Number";
        String expectedObjectMDdotName = "org.aion.avm.arraywrapper.interface.__Ljava.lang.Number";
        String expectedInterfaceObjectMDdotName = "org.aion.avm.arraywrapper.interface._Ljava.lang.CharSequence";

        String expectedPrimitive1DslashName = expectedPrimitive1DdotName.replaceAll("\\.", "/");
        String expectedPrimitiveMDslashName = expectedPrimitiveMDdotName.replaceAll("\\.", "/");
        String expectedObject1DslashName = expectedObject1DdotName.replaceAll("\\.", "/");
        String expectedObjectMDslashName = expectedObjectMDdotName.replaceAll("\\.", "/");
        String expectedInterfaceObjectMDslashName = expectedInterfaceObjectMDdotName.replaceAll("\\.", "/");

        // Convert to 'unifying type' array wrappers.
        assertEquals(expectedPrimitive1DslashName, ArrayNameMapper.getUnifyingArrayWrapperDescriptor(primitive1D));
        assertEquals(expectedPrimitiveMDslashName, ArrayNameMapper.getUnifyingArrayWrapperDescriptor(primitiveMD));
        assertEquals(expectedObject1DslashName, ArrayNameMapper.getUnifyingArrayWrapperDescriptor(object1DslashName));
        assertEquals(expectedObjectMDslashName, ArrayNameMapper.getUnifyingArrayWrapperDescriptor(objectMDslashName));
        assertEquals(expectedInterfaceObjectMDslashName, ArrayNameMapper.getUnifyingArrayWrapperDescriptor(interfaceObjectMDslashName));
    }

    @Test
    public void testPreciseArrayWrapperDescriptor() {
        String primitive1D = int[].class.getName();
        String primitiveMD = boolean[][][].class.getName();
        String object1DdotName = java.lang.String[].class.getName();
        String objectMDdotName = java.lang.String[][].class.getName();
        String interfaceObjectMDdotName = java.lang.Comparable[].class.getName();

        String object1DslashName = object1DdotName.replaceAll("\\.", "/");
        String objectMDslashName = objectMDdotName.replaceAll("\\.", "/");
        String interfaceObjectMDslashName = interfaceObjectMDdotName.replaceAll("\\.", "/");

        String expectedPrimitive1DdotName = "org.aion.avm.arraywrapper.IntArray";
        String expectedPrimitiveMDdotName = "org.aion.avm.arraywrapper.$$$Z";
        String expectedObject1DdotName = "org.aion.avm.arraywrapper.$Ljava.lang.String";
        String expectedObjectMDdotName = "org.aion.avm.arraywrapper.$$Ljava.lang.String";
        String expectedInterfaceObjectMDdotName = "org.aion.avm.arraywrapper.$Ljava.lang.Comparable";

        String expectedPrimitive1DslashName = expectedPrimitive1DdotName.replaceAll("\\.", "/");
        String expectedPrimitiveMDslashName = expectedPrimitiveMDdotName.replaceAll("\\.", "/");
        String expectedObject1DslashName = expectedObject1DdotName.replaceAll("\\.", "/");
        String expectedObjectMDslashName = expectedObjectMDdotName.replaceAll("\\.", "/");
        String expectedInterfaceObjectMDslashName = expectedInterfaceObjectMDdotName.replaceAll("\\.", "/");

        // Convert to 'precise type' array wrappers.
        assertEquals(expectedPrimitive1DslashName, ArrayNameMapper.getPreciseArrayWrapperDescriptor(primitive1D));
        assertEquals(expectedPrimitiveMDslashName, ArrayNameMapper.getPreciseArrayWrapperDescriptor(primitiveMD));
        assertEquals(expectedObject1DslashName, ArrayNameMapper.getPreciseArrayWrapperDescriptor(object1DslashName));
        assertEquals(expectedObjectMDslashName, ArrayNameMapper.getPreciseArrayWrapperDescriptor(objectMDslashName));
        assertEquals(expectedInterfaceObjectMDslashName, ArrayNameMapper.getPreciseArrayWrapperDescriptor(interfaceObjectMDslashName));
    }

    @Test
    public void testOriginalNameOfPrimitive1D() {
        String intArrayOriginal = "[I";
        String byteArrayOriginal = "[B";
        String booleanArrayOriginal = "[Z";
        String charArrayOriginal = "[C";
        String floatArrayOriginal = "[F";
        String shortArrayOriginal = "[S";
        String longArrayOriginal = "[J";
        String doubleArrayOriginal = "[D";

        String intArrayRenamed = ArrayNameMapper.getPreciseArrayWrapperDescriptor(intArrayOriginal);
        String byteArrayRenamed = ArrayNameMapper.getPreciseArrayWrapperDescriptor(byteArrayOriginal);
        String booleanArrayRenamed = ArrayNameMapper.getPreciseArrayWrapperDescriptor(booleanArrayOriginal);
        String charArrayRenamed = ArrayNameMapper.getPreciseArrayWrapperDescriptor(charArrayOriginal);
        String floatArrayRenamed = ArrayNameMapper.getPreciseArrayWrapperDescriptor(floatArrayOriginal);
        String shortArrayRenamed = ArrayNameMapper.getPreciseArrayWrapperDescriptor(shortArrayOriginal);
        String longArrayRenamed = ArrayNameMapper.getPreciseArrayWrapperDescriptor(longArrayOriginal);
        String doubleArrayRenamed = ArrayNameMapper.getPreciseArrayWrapperDescriptor(doubleArrayOriginal);

        assertEquals(intArrayOriginal, ArrayNameMapper.getOriginalNameOf(intArrayRenamed));
        assertEquals(byteArrayOriginal, ArrayNameMapper.getOriginalNameOf(byteArrayRenamed));
        assertEquals(booleanArrayOriginal, ArrayNameMapper.getOriginalNameOf(booleanArrayRenamed));
        assertEquals(charArrayOriginal, ArrayNameMapper.getOriginalNameOf(charArrayRenamed));
        assertEquals(floatArrayOriginal, ArrayNameMapper.getOriginalNameOf(floatArrayRenamed));
        assertEquals(shortArrayOriginal, ArrayNameMapper.getOriginalNameOf(shortArrayRenamed));
        assertEquals(longArrayOriginal, ArrayNameMapper.getOriginalNameOf(longArrayRenamed));
        assertEquals(doubleArrayOriginal, ArrayNameMapper.getOriginalNameOf(doubleArrayRenamed));
    }

    @Test
    public void testOriginalNameOfPrimitiveMD() {
        String intArrayOriginal = "[[I";
        String byteArrayOriginal = "[[[B";
        String booleanArrayOriginal = "[[Z";
        String charArrayOriginal = "[[C";
        String floatArrayOriginal = "[[[F";
        String shortArrayOriginal = "[[[S";
        String longArrayOriginal = "[[J";
        String doubleArrayOriginal = "[[D";

        String intArrayRenamed = ArrayNameMapper.getPreciseArrayWrapperDescriptor(intArrayOriginal);
        String byteArrayRenamed = ArrayNameMapper.getPreciseArrayWrapperDescriptor(byteArrayOriginal);
        String booleanArrayRenamed = ArrayNameMapper.getPreciseArrayWrapperDescriptor(booleanArrayOriginal);
        String charArrayRenamed = ArrayNameMapper.getPreciseArrayWrapperDescriptor(charArrayOriginal);
        String floatArrayRenamed = ArrayNameMapper.getPreciseArrayWrapperDescriptor(floatArrayOriginal);
        String shortArrayRenamed = ArrayNameMapper.getPreciseArrayWrapperDescriptor(shortArrayOriginal);
        String longArrayRenamed = ArrayNameMapper.getPreciseArrayWrapperDescriptor(longArrayOriginal);
        String doubleArrayRenamed = ArrayNameMapper.getPreciseArrayWrapperDescriptor(doubleArrayOriginal);

        assertEquals(intArrayOriginal, ArrayNameMapper.getOriginalNameOf(intArrayRenamed));
        assertEquals(byteArrayOriginal, ArrayNameMapper.getOriginalNameOf(byteArrayRenamed));
        assertEquals(booleanArrayOriginal, ArrayNameMapper.getOriginalNameOf(booleanArrayRenamed));
        assertEquals(charArrayOriginal, ArrayNameMapper.getOriginalNameOf(charArrayRenamed));
        assertEquals(floatArrayOriginal, ArrayNameMapper.getOriginalNameOf(floatArrayRenamed));
        assertEquals(shortArrayOriginal, ArrayNameMapper.getOriginalNameOf(shortArrayRenamed));
        assertEquals(longArrayOriginal, ArrayNameMapper.getOriginalNameOf(longArrayRenamed));
        assertEquals(doubleArrayOriginal, ArrayNameMapper.getOriginalNameOf(doubleArrayRenamed));
    }

    @Test
    public void testOriginalNameOfPreciseTypeObjectArray() {
        String original1 = "[Ljava/lang/Number";
        String original2 = "[[Ljava/lang/Number";
        String original3 = "[[[Ljava/lang/Number";

        String renamed1 = ArrayNameMapper.getPreciseArrayWrapperDescriptor(original1);
        String renamed2 = ArrayNameMapper.getPreciseArrayWrapperDescriptor(original2);
        String renamed3 = ArrayNameMapper.getPreciseArrayWrapperDescriptor(original3);

        assertEquals(original1, ArrayNameMapper.getOriginalNameOf(renamed1));
        assertEquals(original2, ArrayNameMapper.getOriginalNameOf(renamed2));
        assertEquals(original3, ArrayNameMapper.getOriginalNameOf(renamed3));
    }

    @Test
    public void testOriginalNameOfUnifyingTypeObjectArray() {
        String original1 = "[Ljava/lang/String";
        String original2 = "[[Ljava/lang/String";
        String original3 = "[[[Ljava/lang/String";

        String renamed1 = ArrayNameMapper.getUnifyingArrayWrapperDescriptor(original1);
        String renamed2 = ArrayNameMapper.getUnifyingArrayWrapperDescriptor(original2);
        String renamed3 = ArrayNameMapper.getUnifyingArrayWrapperDescriptor(original3);

        assertEquals(original1, ArrayNameMapper.getOriginalNameOf(renamed1));
        assertEquals(original2, ArrayNameMapper.getOriginalNameOf(renamed2));
        assertEquals(original3, ArrayNameMapper.getOriginalNameOf(renamed3));
    }
}
