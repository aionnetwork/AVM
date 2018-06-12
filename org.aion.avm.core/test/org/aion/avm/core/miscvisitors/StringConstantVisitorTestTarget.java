package org.aion.avm.core.miscvisitors;


public class StringConstantVisitorTestTarget {
    public static final String kStringConstant = "a constant";
    public static String publicString;
    public static final Class<?> kClass = Object.class;

    public String returnStaticStringConstant() {
        return kStringConstant;
    }
}
