package org.aion.avm.java.lang;


/**
 * Our shadow implementation of java.lang.EnumConstantNotPresentException.
 */
@SuppressWarnings("rawtypes") /* rawtypes are part of the public api */
public class EnumConstantNotPresentException extends RuntimeException {
    private Class<? extends Enum> enumType;
    private String constantName;

    public EnumConstantNotPresentException(Class<? extends Enum> enumType, String constantName) {
        super(new String(enumType.avm_getName() + "." + constantName));
        this.enumType = enumType;
        this.constantName  = constantName;
    }

    public String avm_constantName() {
        return this.constantName;
    }

    public Class<? extends Enum> avm_enumType() {
        return this.enumType;
    }
}
