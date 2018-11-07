package org.aion.avm.core.miscvisitors;


public class ClassRenameVisitorTestTarget {
    public static ClassRenameVisitorTestTarget myStatic;
    public static ClassRenameVisitorTestTarget[] myStaticArray;
    public ClassRenameVisitorTestTarget myInstance;

    public static void staticOne(ClassRenameVisitorTestTarget[] arg) {
        myStatic = arg[0];
        myStaticArray = arg;
        arg[0].instanceOne(arg[0]);
    }

    public void instanceOne(ClassRenameVisitorTestTarget arg) {
        myInstance = arg;
    }

    public static ClassRenameVisitorTestTarget staticBuilder() {
        return new ClassRenameVisitorTestTarget();
    }

    public static ClassRenameVisitorTestTarget[] staticWrap(ClassRenameVisitorTestTarget arg) {
        return new ClassRenameVisitorTestTarget[] { arg };
    }
}
