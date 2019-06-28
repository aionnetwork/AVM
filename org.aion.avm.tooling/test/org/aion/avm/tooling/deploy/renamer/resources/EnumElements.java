package org.aion.avm.tooling.deploy.renamer.resources;

public enum EnumElements {
    ClassA("A"),
    ClassB("B"),
    C("C");

    public final String name;

    public int getCount() { return 3; }

    EnumElements(String name) {
        this.name = name;
    }
}
