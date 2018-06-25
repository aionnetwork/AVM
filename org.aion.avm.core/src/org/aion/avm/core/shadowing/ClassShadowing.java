package org.aion.avm.core.shadowing;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.ClassWhiteList;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.PackageConstants;
import org.objectweb.asm.*;

import java.util.stream.Stream;

/**
 * Utility for replacing class refs
 *
 * 1) All whitelisted JDK classes are tranformed into their shadow implementations;
 * 2) All method ref to whitelisted JDK class and user defined class will be transformed into it's `avm_` version.
 */
public class ClassShadowing extends ClassToolchain.ToolChainClassVisitor {

    private static final String JAVA_LANG_OBJECT = "java/lang/Object";

    private final String runtimeClassName;
    private final ClassWhiteList classWhiteList;
    private final Replacer replacer;
    private final String postRenameJavaLangObject;

    public ClassShadowing(String runtimeClassName, String shadowPackage) {
        super(Opcodes.ASM6);
        this.replacer = new Replacer(shadowPackage);
        this.runtimeClassName = runtimeClassName;
        this.classWhiteList = new ClassWhiteList();
        this.postRenameJavaLangObject = shadowPackage + JAVA_LANG_OBJECT;
    }

    public ClassShadowing(String runtimeClassName) {
        this(runtimeClassName, PackageConstants.kShadowSlashPrefix);
    }

    @Override
    public void visit(
            final int version,
            final int access,
            final String name,
            final String signature,
            final String superName,
            final String[] interfaces) {

        Assert.assertTrue(!this.classWhiteList.isJdkClass(name));

        // Note that we can't change the superName if this is an interface (since those all must specify "java/lang/Object").
        boolean isInterface = (0 != (Opcodes.ACC_INTERFACE & access));
        String newSuperName = isInterface
                ? (postRenameJavaLangObject.equals(superName) ? JAVA_LANG_OBJECT : superName)
                : replacer.replaceType(superName, false);
        Stream<String> replacedInterfaces = Stream.of(interfaces).map((oldName) -> replacer.replaceType(oldName, true));
        // If this is an interface, we need to add our "root interface" so that we have a unification point between the interface and our shadow Object.
        if (isInterface) {
            String rootInterfaceName = Helpers.fulllyQualifiedNameToInternalName(IObject.class.getName());
            replacedInterfaces = Stream.concat(replacedInterfaces, Stream.of(rootInterfaceName));
        }

        String[] newInterfaces = replacedInterfaces.toArray(String[]::new);

        // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
        super.visit(version, access, name, null, newSuperName, newInterfaces);
    }

    @Override
    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions) {

        String newName = name;

        // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
        MethodVisitor mv = super.visitMethod(access, newName, replacer.replaceMethodDescriptor(descriptor), null, exceptions);

        return new MethodVisitor(Opcodes.ASM6, mv) {
            @Override
            public void visitMethodInsn(
                    final int opcode,
                    final String owner,
                    final String name,
                    final String descriptor,
                    final boolean isInterface) {

                // don't replace methods of internal classes, introduced by instrumentation
                String newName = name;

                // Note that it is possible we will see calls from other phases in the chain and we don't want to re-write them
                // (often, they _are_ the bridging code).
                if ((Opcodes.INVOKESTATIC == opcode) && runtimeClassName.equals(owner)) {
                    super.visitMethodInsn(opcode, owner, newName, descriptor, isInterface);
                } else {
                    // Due to our use of the IObject interface at the root of the shadow type hierarchy (issue-80), we may need to replace this invokevirtual
                    // opcode and/or the owner of the call we are making.
                    // If this is invokespecial, it is probably something like "super.<init>" so we can't replace the opcode or type.
                    boolean allowInterfaceReplacement = (Opcodes.INVOKESPECIAL != opcode);
                    // If this is java/lang/Object, and we aren't in one of those invokespecial cases, we probably need to treat this as an interface.
                    boolean newIsInterface = postRenameJavaLangObject.equals(owner)
                            ? allowInterfaceReplacement
                            : isInterface;
                    // If we are changing to the interface, change the opcode.
                    int newOpcode = (newIsInterface && (Opcodes.INVOKEVIRTUAL == opcode))
                            ? Opcodes.INVOKEINTERFACE
                            : opcode;
                    // We need to shadow the owner type, potentially replacing it with the IObject type.
                    String newOwner = replacer.replaceType(owner, allowInterfaceReplacement);
                    super.visitMethodInsn(newOpcode, newOwner, newName, replacer.replaceMethodDescriptor(descriptor), newIsInterface);
                }
            }

            @Override
            public void visitTypeInsn(final int opcode, final String type) {
                super.visitTypeInsn(opcode, replacer.replaceType(type, true));
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                String newOwner = replacer.replaceType(owner, true);
                String newDescriptor = replacer.replaceMethodDescriptor(descriptor);

                // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
                super.visitFieldInsn(opcode, newOwner, name, newDescriptor);
            }

            @Override
            public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                super.visitLocalVariable(name, replacer.replaceMethodDescriptor(descriptor), signature, start, end, index);
            }
        };
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        String newDescriptor = replacer.replaceMethodDescriptor(descriptor);

        // Just pass in a null signature, instead of updating it (JVM spec 4.3.4: "This kind of type information is needed to support reflection and debugging, and by a Java compiler").
        return super.visitField(access, name, newDescriptor, null, value);
    }
}
