package legacy_examples.deployAndRunTest;

import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.abi.ABIEncoder;

public class DeployAndRunTarget {

    public int foo;

    public static int bar;

    public int add(int a, int b) {
        return a + b;
    }

    @Callable
    public static int addArray(int[] a, int b) {
        return a[0] + a[1] + b;
    }

    @Callable
    public static int addArray2(int[][] a) {
        return a[0][0] + a[1][0];
    }

    @Callable
    public static char[] concatenate(char[][] s) {
        char[] res = new char[6];
        System.arraycopy(s[0], 0, res, 0, s[0].length);
        System.arraycopy(s[1], 0, res, s[0].length, s[1].length);
        return res;
    }

    @Callable
    public static String concatString(String s1, String s2) {
        return s1 + s2;
    }

    @Callable
    public static String[] concatStringArray(String[] s) {
        return new String[]{s[0] + s[1], "perfect"};
    }

    @Callable
    public static char[][] swap(char[][] s) {
        char[][] res = new char[2][2];
        res[0] = s[1];
        res[1] = s[0];
        return res;
    }

    @Callable
    public static void setBar(int bar) {
        DeployAndRunTarget.bar = bar;
    }

    public static byte[] run() {
        return "Hello, world!".getBytes();
    }

    @Callable
    public static byte[] encodeArgs(){
        String methodName = "addArray";
        int[] a = new int[]{123, 1};
        int b = 5;
        return ABIEncoder.encodeMethodArguments(methodName, a, b);
    }
}
