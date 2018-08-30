package org.aion.avm.core.miscvisitors;

public class StrictFPVisitorTestResource {

    public static double v = Double.MAX_VALUE;

    strictfp public static double f() {
        return (v * 1.1) / 1.1;
    }

    public static double g() {
        return (v * 1.1) / 1.1;
    }

    public static double h() {
        float f = Float.MAX_VALUE;
        float g = Float.MAX_VALUE;
        return f * g;
    }

    public static byte[] main() {
        for (int i = 0; i < 10_000; i++) {
            if (f() != g()) {
                throw new RuntimeException();
            }

            if (h() != Double.POSITIVE_INFINITY) {
                throw new RuntimeException();
            }
        }

        return null;
    }
}
