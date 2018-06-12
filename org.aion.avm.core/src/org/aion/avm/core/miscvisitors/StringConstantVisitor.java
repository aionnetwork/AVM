package org.aion.avm.core.miscvisitors;

import java.util.HashMap;
import java.util.Map;

import org.aion.avm.core.ClassToolchain;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * A class visitor which replaces the "ConstantValue" static final String constants with a "ldc+putstatic" pair in the &lt;clinit&gt;
 * (much like Class constants).
 * This should be one of the earliest visitors in the chain since the transformation produces code with no special assumptions,
 * the re-write needs to happen before shadowing and metering, and it depends on nothing other than this being valid bytecode.
 * Note that ASM defines that visitField is called before visitMethod so we can collect the constants we need to re-write before
 * we will see the method we need to modify.
 * If the &lt;clinit&gt; exists, we will prepend this ldc+pustatic pairs.  If it doesn't, we will generate it last.
 */
public class StringConstantVisitor extends ClassToolchain.ToolChainClassVisitor {
    private static final String kClinitName = "<clinit>";
    private static final int kClinitAccess = Opcodes.ACC_STATIC;
    private static final String kClinitDescriptor = "()V";

    private final Map<String, String> staticFieldNamesToConstantValues;
    private String thisClassName;

    public StringConstantVisitor() {
        super(Opcodes.ASM6);
        this.staticFieldNamesToConstantValues = new HashMap<>();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // We just need this to capture the name for the "owner" in the PUTSTATIC calls.
        this.thisClassName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        // The special case we want to handle is a field with the following properties:
        // -static
        // -non-null value
        // -String
        Object filteredValue = value;
        if ((0 != (access & Opcodes.ACC_STATIC))
                && (null != value)
                && "Ljava/lang/String;".equals(descriptor)
        ) {
            // We need to do something special in this case.
            this.staticFieldNamesToConstantValues.put(name, (String)value);
            filteredValue = null;
        }
        return super.visitField(access, name, descriptor, signature, filteredValue);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        // The special-case we want to handle is the <clinit>: prepending the constant loads.
        if (kClinitName.equals(name)) {
            // Prepend the ldc+putstatic pairs.
            for (Map.Entry<String, String> elt : this.staticFieldNamesToConstantValues.entrySet()) {
                visitor.visitLdcInsn(elt.getValue());
                visitor.visitFieldInsn(Opcodes.PUTSTATIC, this.thisClassName, elt.getKey(), "Ljava/lang/String;");
            }
            this.staticFieldNamesToConstantValues.clear();
        }
        return visitor;
    }

    @Override
    public void visitEnd() {
        // Note that visitEnd happens immediately after visitMethod, so we can synthesize the <clinit> here, if it is needed.
        // If we didn't see the <clinit>, we will need to generate one (we determine this if we have unstored constants in our map).
        if (!this.staticFieldNamesToConstantValues.isEmpty()) {
            // We can use our own implementation, so long as we add the return and Maxs calls.
            MethodVisitor clinitVisitor = this.visitMethod(kClinitAccess, kClinitName, kClinitDescriptor, null, null);
            clinitVisitor.visitInsn(Opcodes.RETURN);
            clinitVisitor.visitMaxs(1, 0);
            clinitVisitor.visitEnd();
        }
        super.visitEnd();
    }
}
