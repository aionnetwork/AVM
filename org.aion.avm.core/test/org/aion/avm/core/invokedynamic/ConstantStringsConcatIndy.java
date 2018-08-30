package org.aion.avm.core.invokedynamic;

/**
 * @author Roman Katerinenko
 */
public class ConstantStringsConcatIndy {

    public String test() {
        return concat("a", "b", "c");
    }

    public String concat(String s1, String s2, String s3) {
        // this should compile to lambda
        return s1 + s2 + s3;
    }
}