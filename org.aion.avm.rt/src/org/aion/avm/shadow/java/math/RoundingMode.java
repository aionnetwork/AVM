package org.aion.avm.shadow.java.math;

public enum RoundingMode{
    UP(BigDecimal.ROUND_UP),
    DOWN(BigDecimal.ROUND_DOWN),
    CEILING(BigDecimal.ROUND_CEILING),
    FLOOR(BigDecimal.ROUND_FLOOR),
    HALF_UP(BigDecimal.ROUND_HALF_UP),
    HALF_DOWN(BigDecimal.ROUND_HALF_DOWN),
    HALF_EVEN(BigDecimal.ROUND_HALF_EVEN),
    UNNECESSARY(BigDecimal.ROUND_UNNECESSARY);

    final int oldMode;

    private RoundingMode(int oldMode) {
        this.oldMode = oldMode;
    }

    public static RoundingMode valueOf(int rm) {
        switch(rm) {

            case BigDecimal.ROUND_UP:
                return UP;

            case BigDecimal.ROUND_DOWN:
                return DOWN;

            case BigDecimal.ROUND_CEILING:
                return CEILING;

            case BigDecimal.ROUND_FLOOR:
                return FLOOR;

            case BigDecimal.ROUND_HALF_UP:
                return HALF_UP;

            case BigDecimal.ROUND_HALF_DOWN:
                return HALF_DOWN;

            case BigDecimal.ROUND_HALF_EVEN:
                return HALF_EVEN;

            case BigDecimal.ROUND_UNNECESSARY:
                return UNNECESSARY;

            default:
                throw new IllegalArgumentException("argument out of range");
        }
    }
}