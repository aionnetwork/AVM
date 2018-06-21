package org.aion.avm.core.shadowing.testEnum;


public class TestResource {
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

    public boolean testEnumValues(){
        boolean ret = true;

        TestEnum[] es = TestEnum.values();
        ret = ret && (es[0] == TestEnum.MERCURY);
        ret = ret && (es[1] == TestEnum.VENUS);
        ret = ret && (es[2] == TestEnum.EARTH);
        ret = ret && (es[3] == TestEnum.MARS);
        ret = ret && (es[4] == TestEnum.JUPITER);
        ret = ret && (es[5] == TestEnum.SATURN);
        ret = ret && (es[6] == TestEnum.URANUS);
        ret = ret && (es[7] == TestEnum.NEPTUNE);

        return ret;
    }
}
