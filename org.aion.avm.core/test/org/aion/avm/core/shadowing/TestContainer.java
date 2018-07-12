package org.aion.avm.core.shadowing;


/**
 * Verifies that shadowing can properly transform an enum even when it is an inner class.
 */
public class TestContainer {
    public enum InternalEnum {
        ONE("ONE"),
        TWO("TWO"),
        ;
        public final String input;
        private InternalEnum(String input) {
            this.input = input;
        }
    }
}
