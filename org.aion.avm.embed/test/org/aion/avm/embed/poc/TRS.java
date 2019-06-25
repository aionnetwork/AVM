package org.aion.avm.embed.poc;

import java.math.BigDecimal;
import java.math.BigInteger;
import avm.Address;
import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.AionMap;

public class TRS {
    // public for testing
    public static int intervalSecs = 2_592_000;

    private static int periods, t0special;
    private static long startBlockTimestamp = 0;
    private static boolean inited = false, locked = false, nullified = false;
    private static AionMap<Address, BigInteger> deposited = new AionMap<>(), withdrawn = new AionMap<>();
    private static Address owner, newOwner;
    private static BigInteger precision = BigDecimal.valueOf(StrictMath.pow(10, 18)).toBigIntegerExact(), totalfv, remainder, total;

    static {
        owner = Blockchain.getCaller();
    }

    @Callable
    public static void init(int numPeriods, int specialValue) {
        if (!inited && callerIsOwner()) {
            assert numPeriods > 0;
            periods = numPeriods;
            t0special = specialValue;
            inited = true;
        }
    }

    @Callable
    public static void start(long blockTimestamp) {
        if (inited && isPreStart() && callerIsOwner()) {
            startBlockTimestamp = blockTimestamp;
            total = Blockchain.getBalance(Blockchain.getAddress());
            totalfv = total;
            remainder = total;
        }
    }

    public static void refund(Address address, BigInteger amount) {
        if (callerIsOwner() && isPreLock()) {
            Blockchain.call(address, amount, new byte[0], 21_000);
        }
    }

    public static int period() {
        return periodAt(Blockchain.getBlockTimestamp());
    }

    public static int periodAt(long timestamp) {
        if (startBlockTimestamp > timestamp) {
            return 0;
        }
        int p = (int) ((timestamp - startBlockTimestamp) / intervalSecs) + 1;
        return (p > periods) ? periods : p;
    }

    @Callable
    public static void mint(Address address, long amount) {
        if (callerIsOwner() && isPreLock() && !nullified) {
            BigInteger value = BigInteger.valueOf(amount);
            deposited.put(address, value);
            withdrawn.put(address, BigInteger.ZERO);
            Blockchain.log(address.toByteArray(), value.toByteArray());
        }
    }

    public static BigInteger checkBalance(Address address) {
        return deposited.get(address);
    }

    public static void depositTo(Address beneficiary, BigInteger amount) {
        if (callerIsOwner() && isPreLock() && !nullified) {
            if (deposited.containsKey(beneficiary)) {
                deposited.put(beneficiary, deposited.get(beneficiary).add(amount));
            } else {
                deposited.put(beneficiary, amount);
            }

            totalfv = totalfv.add(amount);
            Blockchain.log(beneficiary.toByteArray(), amount.toByteArray());
        }
    }

    public static void deposit(BigInteger amount) {
        if (callerIsOwner() && !nullified) {
            depositTo(Blockchain.getCaller(), amount);
        }
    }

    public static BigInteger availableForWithdrawalAt(long timestamp) {
        BigInteger numerator = BigInteger.valueOf(t0special + periodAt(timestamp)).multiply(precision);
        return numerator.divide(BigInteger.valueOf(t0special + periods));
    }

    public static BigInteger _withdrawTo(BigInteger deposit, BigInteger withdraw, long timestamp) {
        BigInteger fraction = availableForWithdrawalAt(timestamp);
        BigInteger withdrawable = (deposit.multiply(fraction)).divide(precision);
        if (withdrawable.compareTo(withdraw) > 0) {
            return withdrawable.subtract(withdraw);
        }
        return BigInteger.ZERO;
    }

    public static boolean withdrawTo(Address address) {
        if (isPostStart() && !nullified) {
            BigInteger deposit = deposited.get(address);
            BigInteger withdraw = withdrawn.get(address);
            BigInteger diff = _withdrawTo(deposit, withdraw, Blockchain.getBlockTimestamp());
            if (diff.equals(BigInteger.ZERO)) {
                return false;
            }

            assert ((diff.add(withdraw)).compareTo(deposit) <= 0);
            Blockchain.call(address, diff, new byte[0], 21_000);

            if (withdrawn.containsKey(address)) {
                withdrawn.put(address, withdrawn.get(address).add(diff));
            } else {
                withdrawn.put(address, diff);
            }

            remainder = remainder.subtract(diff);
            Blockchain.log(address.toByteArray(), diff.toByteArray());
            return true;
        }
        return false;
    }

    @Callable
    public static boolean withdraw() {
        return !nullified && withdrawTo(Blockchain.getCaller());
    }

    public static void bulkWithdraw(Address[] addresses) {
        if (!nullified) {
            for (Address address : addresses) {
                withdrawTo(address);
            }
        }
    }

    private static void finalizeInit() {
        if (!inited && callerIsOwner()) {
            inited = true;
        }
    }

    @Callable
    public static void lock() {
        if (callerIsOwner()) {
            locked = true;
        }
    }

    private static boolean callerIsOwner() {
        return owner.equals(Blockchain.getCaller());
    }

    private static boolean isPreStart() {
        return locked && (startBlockTimestamp == 0);
    }

    private static boolean isPostStart() {
        return locked && (startBlockTimestamp != 0);
    }

    private static boolean isPreLock() {
        return !locked && (startBlockTimestamp == 0);
    }

    private static void changeOwner(Address nextOwner) {
        if (callerIsOwner()) {
            newOwner = nextOwner;
        }
    }

    private static void acceptOwnership() {
        if (Blockchain.getCaller().equals(newOwner)) {
            owner = newOwner;
            newOwner = null;
            Blockchain.log(owner.toByteArray());
        }
    }

}
