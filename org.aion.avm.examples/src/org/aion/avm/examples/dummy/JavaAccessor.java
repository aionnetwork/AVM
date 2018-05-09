package org.aion.avm.examples.dummy;

/**
 * @author Roman Katerinenko
 */
public class JavaAccessor {
    public String callJavaClass() {
        return new StringBuilder("1").append("2").toString();
    }
}