package org.aion.avm.core.invokedynamic;

public class IndyConcatenationTestResource {
    int i = 0;
    long j = 1;
    short s = 2;
    double d = 1.1;
    float f = 0;
    char c = 'a';
    boolean z = true;
    byte b = 3;



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

    public String concatWithPrimtives(){
        return "" + i + j + s + d + f+ c + z + b;
    }
}