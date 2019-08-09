module org.aion.avm.tooling {
    exports org.aion.avm.tooling;
    exports org.aion.avm.tooling.abi;
    exports org.aion.avm.tooling.deploy;
    exports org.aion.avm.tooling.deploy.eliminator;
    exports org.aion.avm.tooling.deploy.renamer;

    requires org.aion.avm.userlib;
    requires org.aion.avm.api;

    // external modules
    requires org.objectweb.asm;
    requires org.objectweb.asm.tree;
    requires org.objectweb.asm.commons;
    requires aion.types;
}
