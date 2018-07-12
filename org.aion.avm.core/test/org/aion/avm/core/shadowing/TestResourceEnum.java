package org.aion.avm.core.shadowing;


/**
 * Verifies that shadowing can properly transform an enum.
 */
public enum TestResourceEnum {
    ONE("ONE"),
    TWO("TWO"),
    ;
    public final String input;
    private TestResourceEnum(String input) {
        this.input = input;
    }
}
