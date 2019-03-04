package org.aion.avm.tooling.abi;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.Map;

public class TestHelpers {
    public static void saveMainClassInABICompiler(ABICompiler compiler) {
        DataOutputStream dout = null;
        try {
            dout = new DataOutputStream(
                    new FileOutputStream(compiler.getMainClassName() + ".class"));
            dout.write(compiler.getMainClassBytes());
            dout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveNonMainClassInABICompiler(ABICompiler compiler) {
        try {
            DataOutputStream dout = null;
            for (
                    Map.Entry<String, byte[]> entry : compiler.getClassMap().entrySet()) {
                dout = new DataOutputStream(new FileOutputStream(entry.getKey() + ".class"));
                dout.write(entry.getValue());
                dout.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveAllClassesInABICompiler(ABICompiler compiler) {
        saveMainClassInABICompiler(compiler);
        saveNonMainClassInABICompiler(compiler);
    }
}
