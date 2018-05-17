module org.aion.avm.core {
    requires org.aion.avm.rt;

    exports org.aion.avm.core;
    exports org.aion.avm.core.dappreading;

    // external modules
    requires slf4j.api;
    requires org.objectweb.asm;
    requires org.objectweb.asm.commons;
    requires org.objectweb.asm.tree;
    requires org.objectweb.asm.util;
    requires org.objectweb.asm.tree.analysis;
}