package org.aion.avm.tooling.shadowing.testEnum;

import org.aion.avm.tooling.abi.Callable;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

public class TestResource {
    static TestEnum earth = TestEnum.EARTH;
    static TestEnum jupiter = TestEnum.JUPITER;
    static TestEnum mars = TestEnum.MARS;

    @Callable
    public static boolean testEnumAccess(){
        boolean ret = true;
        ret = ret && (earth == TestEnum.EARTH);
        ret = ret && (earth == TestEnum.valueOf("EARTH"));
        ret = ret && (TestEnum.EARTH.mass() == earth.mass());

        return ret;
    }

    @Callable
    public static boolean testEnumValues(){
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


    @Callable
    public static boolean testShadowJDKEnum(){
        boolean ret = true;
        ret = ret && (RoundingMode.HALF_UP == RoundingMode.valueOf("HALF_UP"));
        ret = ret && (RoundingMode.CEILING instanceof Object);

        RoundingMode[] es = (RoundingMode[]) RoundingMode.values();
        ret = ret && (es[0] == RoundingMode.UP);
        ret = ret && (es[1] == RoundingMode.DOWN);
        ret = ret && (es[2] == RoundingMode.CEILING);
        ret = ret && (es[3] == RoundingMode.FLOOR);
        ret = ret && (es[4] == RoundingMode.HALF_UP);
        ret = ret && (es[5] == RoundingMode.HALF_DOWN);
        ret = ret && (es[6] == RoundingMode.HALF_EVEN);
        ret = ret && (es[7] == RoundingMode.UNNECESSARY);

        return ret;
    }

    @Callable
    public static boolean testTimeUnitEnum(){
        boolean ret = true;
        ret = ret && (TimeUnit.DAYS == TimeUnit.valueOf("DAYS"));
        ret = ret && (TimeUnit.HOURS instanceof Object);

        TimeUnit[] es = (TimeUnit[]) TimeUnit.values();
        ret = ret && (es[0] == TimeUnit.DAYS);
        ret = ret && (es[1] == TimeUnit.HOURS);
        ret = ret && (es[2] == TimeUnit.MINUTES);
        ret = ret && (es[3] == TimeUnit.SECONDS);
        ret = ret && (es[4] == TimeUnit.MILLISECONDS);
        ret = ret && (es[5] == TimeUnit.MICROSECONDS);
        ret = ret && (es[6] == TimeUnit.NANOSECONDS);

        return ret;
    }

    @Callable
    public static boolean testInvalidRoundingModeEnum() {
        try {
            RoundingMode.valueOf("up");
            throw new AssertionError();
        } catch (IllegalArgumentException e){
            // Expected
        }
        return true;
    }

    @Callable
    public static boolean EnumHashcode() {
        boolean ret = true;
        //From NodeEnvironment
        ret = ret && (RoundingMode.UP.hashCode() == 7);

        MathContext mc = new MathContext(1, RoundingMode.UP);
        ret = ret && (mc.hashCode() == 414);

        ret = ret && (Type1.NORMAL.hashCode() == 28);
        ret = ret && (Type2.SPECIALIZED.hashCode() == 31);

        return ret;
    }

    interface GeneralType {}

    enum Type1 implements GeneralType {
        NORMAL;
    }

    enum Type2 implements GeneralType {
        SPECIALIZED;
    }
}
