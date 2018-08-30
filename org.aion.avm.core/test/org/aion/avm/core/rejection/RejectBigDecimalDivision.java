package org.aion.avm.core.rejection;

import java.math.BigDecimal;
import java.math.MathContext;


public class RejectBigDecimalDivision {
    public BigDecimal divide() {
        BigDecimal dividend = new BigDecimal("3.14159");
        BigDecimal divisor = new BigDecimal("1.01");
        MathContext context = MathContext.DECIMAL32;
        BigDecimal pair[] = dividend.divideAndRemainder(divisor, context);
        return pair[0];
    }
}
