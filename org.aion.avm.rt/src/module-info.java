module org.aion.avm.rt {
    exports org.aion.avm.arraywrapper;
    exports org.aion.avm.exceptionwrapper.java.lang;
    exports org.aion.avm.internal;
    exports org.aion.avm.shadow.java.lang;
    exports org.aion.avm.shadow.java.lang.invoke;
    exports org.aion.avm.shadow.java.math;
    exports org.aion.avm.shadow.java.util;
    exports org.aion.avm.shadow.java.util.concurrent;
    exports org.aion.avm.shadow.java.util.function;
    exports org.aion.avm.api;
    exports org.aion.avm;

    // These are required in order to satisfy test compilation in Eclipse.
    // (our build process may avoid this need in other cases - not sure if that is more/less correct)
    requires slf4j.api;
    requires slf4j.simple;
}
