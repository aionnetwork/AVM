module org.aion.avm.embed {
    exports org.aion.avm.embed;

    requires org.aion.avm.rt;
    requires org.aion.avm.userlib;
    requires org.aion.avm.api;
    requires org.aion.avm.core;
    requires org.aion.avm.tooling;

    // external modules
    requires spongycastle;
    requires ed25519;
    requires org.objectweb.asm;

    //Dependency for Junit Rule
    requires junit;
    requires aion.types;
}
