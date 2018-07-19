package org.aion.avm.core.miscvisitors;


/**
 * We will take advantage of how the &lt;clinit&gt; normally saves the string constants into the statics to see whether or not that happens
 * after our transformation.
 */
public class ClinitStrippingVisitorTestTarget {
    // NOTE:  Making this final will turn the getConstant into "ldc" so it cannot be final.
    public static String ONE_CONSTANT = "ONE CONSTATNT";

    public static String getConstant() {
        return ONE_CONSTANT;
    }
}
