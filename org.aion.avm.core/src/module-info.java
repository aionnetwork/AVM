module org.aion.avm.core {
    exports org.aion.avm.core;
    exports org.aion.avm.core.dappreading;
    exports org.aion.avm.core.types;
    exports org.aion.avm.core.util;
    exports org.aion.avm.core.classloading;
    exports org.aion.kernel;
    exports org.aion.parallel;

    requires org.aion.avm.rt;
    requires org.aion.avm.userlib;
    requires org.aion.avm.api;

    // external modules
    requires slf4j.api;
    requires slf4j.simple;
    requires org.objectweb.asm;
    requires org.objectweb.asm.commons;
    requires org.objectweb.asm.tree;
    requires org.objectweb.asm.util;
    requires org.objectweb.asm.tree.analysis;
    requires aion.vm.api;
}
