package org.aion.avm.arraywrapper;

public class $$$MyObj extends ObjectArray {

    public static $$$MyObj initArray(int d0, int d1, int d2){
        $$$MyObj ret = new $$$MyObj(d0);
        for (int i = 0; i < d0; i++) {
            ret.set(i, $$MyObj.initArray(d1, d2));
        }
        return ret;
    }

    public $$$MyObj(int c){
        super(c);
    }

}
