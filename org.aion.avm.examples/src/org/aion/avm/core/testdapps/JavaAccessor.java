package org.aion.avm.core.testdapps;

/**
 * @author Roman Katerinenko
 */
public class JavaAccessor {
    public String callJavaClass() {
        return new StringBuilder("1").append("2").toString();
    }
}