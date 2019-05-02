module org.aion.avm.rt {
    exports a;
    exports e.s.java.lang;
    exports i;
    exports s.java.lang;
    exports s.java.lang.invoke;
    exports s.java.math;
    exports s.java.util;
    exports s.java.util.concurrent;
    exports s.java.util.function;
    exports p.avm;
    exports org.aion.avm;

    requires org.aion.avm.api;

    // These are required in order to satisfy test compilation in Eclipse.
    // (our build process may avoid this need in other cases - not sure if that is more/less correct)
    requires slf4j.api;
    requires slf4j.simple;

    // When running unit tests in Eclipse, these are required (our Ant build process avoids this but it is probably more correct with them).
    opens s.java.lang;
    opens p.avm;
    opens i;
    exports s.java.io;
}
