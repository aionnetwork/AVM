package org.aion.avm.core.shadowing.Enum;


public class TestEnumResource {
    TestEnum earth = TestEnum.EARTH;
    TestEnum jupiter = TestEnum.JUPITER;
    TestEnum mars = TestEnum.MARS;

    public boolean testEnumAccess(){
        boolean ret = true;
        ret = ret && (earth == TestEnum.EARTH);
        ret = ret && (earth == TestEnum.valueOf("EARTH"));
        ret = ret && (TestEnum.EARTH.mass() == earth.mass());

        //TestEnum[] es = TestEnum.values();
        return ret;
    }
}
