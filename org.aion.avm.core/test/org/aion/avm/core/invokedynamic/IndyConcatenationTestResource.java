package org.aion.avm.core.invokedynamic;

/**
 * @author Roman Katerinenko
 */
public class IndyConcatenationTestResource {
    public String concatWithDynamicArgs(String s1, String s2, String s3) {
        // this should compile to lambda
        return s1 + s2 + s3;
    }

    public String concatWithCharacters(String s1, String s2, String s3) {
        // this should compile to lambda
        return 'y' + s1 + s2 + s3 + "x" + 12.8;
    }

    public String nullReferenceConcat() {
        String nullPointer = null;
        return "" + nullPointer;
    }

    public String emptyStringsConcat() {
        return "" + "";
    }
}