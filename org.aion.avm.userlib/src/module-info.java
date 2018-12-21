module org.aion.avm.userlib {
    exports org.aion.avm.userlib;

    // These are required in order to satisfy test compilation in Eclipse.
    // (our build process may avoid this need in other cases - not sure if that is more/less correct)
    requires slf4j.api;
    requires slf4j.simple;
}
