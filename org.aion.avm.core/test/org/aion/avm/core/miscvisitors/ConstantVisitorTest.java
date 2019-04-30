package org.aion.avm.core.miscvisitors;

import java.util.Collections;
import java.util.Map;

import org.aion.avm.core.ConstantClassBuilder;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.classloading.AvmClassLoader;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.PackageConstants;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * Does a VERY basic invocation of ConstantVisitor to verify that it can handle fields coming in after the &lt;clinit&gt;.
 */
public class ConstantVisitorTest {
    @Test
    public void testLoadStringConstant() throws Exception {
        // We will need the constant class.
        String constantTestValue = "value";
        String constantClassName = "ConstantClass";
        ConstantClassBuilder.ConstantClassInfo constantClass = ConstantClassBuilder.generateConstantClassForTest(constantClassName, Collections.singletonList(constantTestValue));
        Assert.assertEquals(1, constantClass.constantToFieldMap.size());
        Assert.assertEquals("const_0", constantClass.constantToFieldMap.get(constantTestValue));
        
        String testClassName = "TestClass";
        ClassWriter writer = new ClassWriter(0);
        ConstantVisitor visitor = new ConstantVisitor(constantClassName, constantClass.constantToFieldMap);
        visitor.setDelegate(writer);
        visitor.visit(Opcodes.V10, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, testClassName, null, "java/lang/Object", null);
        
        // Write the <clinit>.
        MethodVisitor methodVisitor = visitor.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitMaxs(1, 0);
        methodVisitor.visitEnd();
        
        // Now, the field.
        visitor.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "publicString", "L" + PackageConstants.kShadowSlashPrefix + "java/lang/String;", null, constantTestValue).visitEnd();
        
        // Finally, finish.
        visitor.visitEnd();
        
        byte[] bytecode = writer.toByteArray();
        // Get the class and make sure there are no issues.  Note that this would fail in our old implementation (duplicate <clinit>).
        byte[] stubBytecode = Helpers.loadRequiredResourceAsBytes(HelperStub.CLASS_NAME + ".class");
        Map<String, byte[]> classes = Map.of(testClassName, bytecode
                , constantClassName, constantClass.bytecode
        );
        Map<String, byte[]> classesAndHelper = Helpers.mapIncludingHelperBytecode(classes, stubBytecode);
        AvmClassLoader loader = NodeEnvironment.singleton.createInvocationClassLoader(classesAndHelper);
        Assert.assertEquals(0, TestHelpers.wrapAsStringCounter);
        Class<?> clazz = Class.forName(testClassName, true, loader);
        Assert.assertNotNull(clazz);
        // Prove that the <clinit> _did_ actually run.
        Assert.assertEquals(1, TestHelpers.wrapAsStringCounter);
    }


    public static class TestHelpers {
        public static int wrapAsStringCounter;
    }

    /**
     * Note that this is just because the ConstantVisitor injects helper calls which need to go somewhere.
     */
    public static class HelperStub {
        public static final String CLASS_NAME = Helpers.fulllyQualifiedNameToInternalName(HelperStub.class.getName());
        public static org.aion.avm.shadow.java.lang.String wrapAsString(String input) {
            TestHelpers.wrapAsStringCounter += 1;
            // We don't do anything with this so even null works.
            return null;
        }
    }
}
