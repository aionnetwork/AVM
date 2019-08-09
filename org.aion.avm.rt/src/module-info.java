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

    // When running unit tests in Eclipse, these are required (our Ant build process avoids this but it is probably more correct with them).
    opens s.java.lang;
    opens p.avm;
    opens i;
    exports s.java.io;
}
