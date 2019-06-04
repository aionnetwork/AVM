module org.aion.avm.tooling {
    exports org.aion.avm.tooling;

    requires org.aion.avm.rt;
    requires org.aion.avm.userlib;
    requires org.aion.avm.api;
    requires org.aion.avm.core;

    // external modules
    requires slf4j.api;
    requires slf4j.simple;
    requires spongycastle;
    requires ed25519;
    requires aion.vm.api;
    requires org.objectweb.asm;

    //Dependency for Junit Rule
    requires junit;
    requires org.objectweb.asm.tree;
    requires aion.types;
}
