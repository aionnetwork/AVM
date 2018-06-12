package org.aion.avm.core.miscvisitors;


/**
 * The same as StringConstantVisitorTestTarget but structured such that no &lt;clinit&gt; is generated.
 */
public class StringConstantVisitorTestTargetNoStatic {
    public static final String kStringConstant = "a constant";
    public static String publicString;

    public String returnStaticStringConstant() {
        return kStringConstant;
    }
}
