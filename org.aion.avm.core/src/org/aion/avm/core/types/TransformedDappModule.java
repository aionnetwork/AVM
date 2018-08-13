package org.aion.avm.core.types;

import java.util.Map;


/**
 * Represents the DApp code once it has been validated and transformed but before it has been deployed and stored.
 * All fields are public since this object is effectively an immutable struct.
 * See issue-134 for more details on this design.
 */
public class TransformedDappModule {
    public static TransformedDappModule fromTransformedClasses(Map<String, byte[]> classes, String mainClass)  {
        return new TransformedDappModule(classes, mainClass);
    }


    public final Map<String, byte[]> classes;
    public final String mainClass;

    private TransformedDappModule(Map<String, byte[]> classes, String mainClass) {
        this.classes = classes;
        this.mainClass = mainClass;
    }
}
