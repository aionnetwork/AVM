package org.aion.avm.java.lang;

/**
 * @author Roman Katerinenko
 */
public class Double extends org.aion.avm.java.lang.Object {
    private double value;

    public static org.aion.avm.java.lang.Double avm_valueOf(double origValue) {
        Double transformed = new Double();
        transformed.value = origValue;
        return transformed;
    }

    public double avm_doubleValue() {
        return value;
    }

}