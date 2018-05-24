package org.aion.avm.java.lang;


/**
 * Our shadow implementation of java.lang.TypeNotPresentException.
 */
public class TypeNotPresentException extends RuntimeException {
    private String typeName;

    public TypeNotPresentException(String typeName, Throwable cause) {
        super(new String("Type " + typeName + " not present"), cause);
        this.typeName = typeName;
    }

    public String typeName() {
        return typeName;
    }
}
