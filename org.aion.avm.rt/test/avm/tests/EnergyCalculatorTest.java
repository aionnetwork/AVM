package avm.tests;

import org.aion.avm.EnergyCalculator;
import org.aion.avm.RuntimeMethodFeeSchedule;
import org.junit.Assert;
import org.junit.Test;

public class EnergyCalculatorTest {
    @Test
    public void testCalculatedFee() {
        int linearFee = EnergyCalculator.multiplyLinearValueByMethodFeeLevel2AndAddBase(100, 1_000_000_000);
        Assert.assertEquals(Integer.MAX_VALUE, linearFee);

        linearFee = EnergyCalculator.multiplyLinearValueByMethodFeeLevel2AndAddBase(1500, Integer.MAX_VALUE);
        Assert.assertEquals(Integer.MAX_VALUE, linearFee);

        linearFee = EnergyCalculator.multiplyLinearValueByMethodFeeLevel2AndAddBase(100, 0);
        Assert.assertEquals(100, linearFee);

        linearFee = EnergyCalculator.multiplyLinearValueByMethodFeeLevel2AndAddBase(100, 1505);
        Assert.assertEquals(100 + 1505 * RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR_LEVEL_2, linearFee);

        linearFee = EnergyCalculator.multiplyLinearValueByMethodFeeLevel1AndAddBase(100, 1_000_000_000);
        Assert.assertEquals(100 + 1_000_000_000, linearFee);

        linearFee = EnergyCalculator.multiplyLinearValueByMethodFeeLevel1AndAddBase(300, Integer.MAX_VALUE);
        Assert.assertEquals(Integer.MAX_VALUE, linearFee);
    }
}
