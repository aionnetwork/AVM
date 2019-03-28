package org.aion.avm.tooling.deploy;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

public class SignatureDependencyVisitor extends SignatureVisitor {
    private final DependencyCollector dependencyCollector;

    private String mainClassName;
    public SignatureDependencyVisitor(DependencyCollector dependencyCollector) {
        super(Opcodes.ASM6);
        this.dependencyCollector = dependencyCollector;
    }

    //visit signature of a class
    @Override
    public void visitClassType(String name) {
        dependencyCollector.addType(name);
        mainClassName = name;
        super.visitClassType(name);
    }

    @Override
    public void visitInnerClassType(String name) {
        dependencyCollector.addType(mainClassName + "$" + name);
        super.visitInnerClassType(name);
    }

}
