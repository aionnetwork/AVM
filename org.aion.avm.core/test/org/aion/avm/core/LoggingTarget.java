package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.BlockchainRuntime;

public class LoggingTarget {
    public static final byte[] TOPIC1 = new byte[]{ 0xf, 0xe, 0xd, 0xc, 0xb, 0xa };
    public static final byte[] TOPIC2 = new byte[]{ 0xa, 0xb, 0xc, 0xd, 0xe, 0xf };
    public static final byte[] TOPIC3 = new byte[]{ 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9 };
    public static final byte[] TOPIC4 = new byte[]{ 0x9, 0x8, 0x7, 0x6, 0x5, 0x4, 0x3 };

    public static final byte[] DATA1 = new byte[]{ 0x1 };
    public static final byte[] DATA2 = new byte[]{ 0x2 };
    public static final byte[] DATA3 = new byte[]{ 0x3 };
    public static final byte[] DATA4 = new byte[]{ 0x4 };
    public static final byte[] DATA5 = new byte[]{ 0x5 };

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithObject(new LoggingTarget(), BlockchainRuntime.getData());
    }

    public void spawnInternalTransactionsAndHitLogsAtEachLevel(int numInternals) {
        hitLogs();
        if (numInternals > 0) {
            byte[] data = ABIEncoder.encodeMethodArguments("spawnInternalTransactionsAndHitLogsAtEachLevel", numInternals - 1);
            BlockchainRuntime.call(BlockchainRuntime.getAddress(), BigInteger.ZERO, data, BlockchainRuntime.getRemainingEnergy());
        }
    }

    public void spawnInternalTransactionsAndHitLogsAtBottomLevel(int numInternals) {
        if (numInternals > 0) {
            byte[] data = ABIEncoder.encodeMethodArguments("spawnInternalTransactionsAndHitLogsAtBottomLevel", numInternals - 1);
            BlockchainRuntime.call(BlockchainRuntime.getAddress(), BigInteger.ZERO, data, BlockchainRuntime.getRemainingEnergy());
        } else {
            hitLogs();
        }
    }

    public void hitLogs() {
        hitLog1();
        hitLog2();
        hitLog3();
        hitLog4();
        hitLog5();
    }

    private void hitLog1() {
        BlockchainRuntime.log(DATA1);
    }

    private void hitLog2() {
        BlockchainRuntime.log(TOPIC1, DATA2);
    }

    private void hitLog3() {
        BlockchainRuntime.log(TOPIC1, TOPIC2, DATA3);
    }

    private void hitLog4() {
        BlockchainRuntime.log(TOPIC1, TOPIC2, TOPIC3, DATA4);
    }

    private void hitLog5() {
        BlockchainRuntime.log(TOPIC1, TOPIC2, TOPIC3, TOPIC4, DATA5);
    }

}
