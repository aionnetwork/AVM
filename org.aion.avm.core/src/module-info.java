module org.aion.avm.core {
    exports org.aion.avm.core;
    exports org.aion.avm.core.impl;
    // external modules
    requires slf4j.api;
    requires junit;
    requires hamcrest.all;
    requires org.objectweb.asm;
    requires org.objectweb.asm.commons;
    requires org.objectweb.asm.tree;
    requires org.objectweb.asm.util;
}