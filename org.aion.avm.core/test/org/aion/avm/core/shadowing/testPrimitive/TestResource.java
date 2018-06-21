package org.aion.avm.core.shadowing.testPrimitive;

public class TestResource {
    public boolean testAutoboxing(){
        boolean ret = true;

        Boolean     a = true;
        Character   c = 'a';
        Double      d = 0.1d;
        Float       e = 0.1f;
        Integer     f = 1;
        Long        g = 100000000L;
        Short       h = 1;

        boolean     aa = a;
        char        cc = c;
        double      dd = d;
        float       ee = e;
        int         ff = f;
        long        gg = g;
        short       hh = h;

        return ret;
    }
}
