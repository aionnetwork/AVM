package org.aion.avm.core.miscvisitors;

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
        ClassWriter writer = new ClassWriter(0);
        ConstantVisitor visitor = new ConstantVisitor(TestHelpers.CLASS_NAME);
        visitor.setDelegate(writer);
        visitor.visit(Opcodes.V10, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "TestClass", null, "java/lang/Object", null);
        
        // Write the <clinit>.
        MethodVisitor methodVisitor = visitor.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitMaxs(1, 0);
        methodVisitor.visitEnd();
        
        // Now, the field.
        visitor.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "publicString", "L" + PackageConstants.kShadowSlashPrefix + "java/lang/String;", null, "value").visitEnd();
        
        // Finally, finish.
        visitor.visitEnd();
        
        byte[] bytecode = writer.toByteArray();
        // Get the class and make sure there are no issues.  Note that this would fail in our old implementation (duplicate <clinit>).
        Class<?> clazz = SingleLoader.loadClass("TestClass", bytecode);
        Assert.assertNotNull(clazz);
    }


    /**
     * Note that this is just because the ConstantVisitor injects helper calls which need to go somewhere.
     */
    public static class TestHelpers {
        public static final String CLASS_NAME = Helpers.fulllyQualifiedNameToInternalName(TestHelpers.class.getName());
        
        public static org.aion.avm.shadow.java.lang.String wrapAsString(String input) {
            // We don't do anything with this so even null works.
            return null;
        }
    }
}
