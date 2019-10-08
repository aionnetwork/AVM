package org.aion.avm.core.instrument;

import i.PackageConstants;
import org.aion.avm.core.classgeneration.CommonGenerators;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.Map;

/**
 * This is to prove the assigned heap allocation size values to generated exceptions in JCLAndAPIHeapInstanceSize are accurate.
 * It checks that generated exceptions do not declare any fields or have a parent that declares fields.
 */

public class ExceptionHeapAllocationTest {

    private List<String> exceptionClassesWithNonStaticFields = new ArrayList<>(Arrays.asList(s.java.lang.TypeNotPresentException.class.getName(),
            s.java.lang.EnumConstantNotPresentException.class.getName()));

    @Test
    public void testSizes() {
        Map<String, byte[]> generatedShadowJDK = CommonGenerators.generateShadowJDK();
        generatedShadowJDK.forEach((k, v) -> {
            if (!k.startsWith(PackageConstants.kExceptionWrapperDotPrefix)) {
                ClassNode classNode = new ClassNode();
                ClassReader cr = new ClassReader(v);
                cr.accept(classNode, ClassReader.SKIP_DEBUG);
                Assert.assertEquals(0, classNode.fields.size());
                Assert.assertTrue(!exceptionClassesWithNonStaticFields.contains(classNode.superName));
            }
        });
    }

}
