package org.aion.avm.internal;

import org.aion.avm.shadow.java.lang.Object;

import java.util.HashMap;
import java.util.Map;

public class ConstantsHolder {

    public static Map<Integer, Object> getConstants() {
        return constants;
    }

    private static Map<Integer, Object> constants = new HashMap<>();

    public static void addConstant(int constantId, org.aion.avm.shadow.java.lang.Object constant) {
        RuntimeAssertionError.assertTrue(!constants.containsKey(constantId));
        constants.put(constantId, constant);
    }


}
