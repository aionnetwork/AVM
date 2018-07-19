module org.aion.avm.core {
    exports org.aion.avm.core;
    exports org.aion.avm.core.dappreading;

    requires org.aion.avm.rt;
    requires org.aion.avm.userlib;

    // external modules
    requires slf4j.api;
    requires slf4j.simple;
    requires org.objectweb.asm;
    requires org.objectweb.asm.commons;
    requires org.objectweb.asm.tree;
    requires org.objectweb.asm.util;
    requires org.objectweb.asm.tree.analysis;
}