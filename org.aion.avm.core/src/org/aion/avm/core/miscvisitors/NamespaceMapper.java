package org.aion.avm.core.miscvisitors;


/**
 * Note that this was originally carved from UserClassMappingVisitor so some of its history can be found there.
 */
public class NamespaceMapper {
    private static final String FIELD_PREFIX = "avm_";
    private static final String METHOD_PREFIX = "avm_";

    /**
     * @param name The pre-transform field name.
     * @return The post-transform field name.
     */
    public static String mapFieldName(String name) {
        return FIELD_PREFIX  + name;
    }

    /**
     * @param name The pre-transform method name.
     * @return The post-transform method name.
     */
    public static String mapMethodName(String name) {
        if ("<init>".equals(name) || "<clinit>".equals(name)) {
            return name;
        }

        return METHOD_PREFIX + name;
    }
}
