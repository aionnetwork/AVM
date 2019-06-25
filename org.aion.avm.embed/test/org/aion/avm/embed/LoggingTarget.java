package org.aion.avm.embed;

import java.math.BigInteger;

import org.aion.avm.userlib.abi.ABIDecoder;
import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIEncoder;

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

    public static int depth = 0;

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("spawnInternalTransactionsAndHitLogsAtEachLevel")) {
                spawnInternalTransactionsAndHitLogsAtEachLevel(decoder.decodeOneInteger());
                return new byte[0];
            } else if (methodName.equals("spawnInternalTransactionsAndHitLogsAtBottomLevel")) {
                spawnInternalTransactionsAndHitLogsAtBottomLevel(decoder.decodeOneInteger());
                return new byte[0];
            } else if (methodName.equals("spawnInternalTransactionsAndFailAtDepth5")) {
                spawnInternalTransactionsAndFailAtDepth5(decoder.decodeOneInteger());
                return new byte[0];
            } else if (methodName.equals("hitLogs")) {
                hitLogs();
                return new byte[0];
            } else {
                return new byte[0];
            }
        }
    }

    public static void spawnInternalTransactionsAndHitLogsAtEachLevel(int numInternals) {
        hitLogs();
        if (numInternals > 0) {
            byte[] arg1 = ABIEncoder.encodeOneString("spawnInternalTransactionsAndHitLogsAtEachLevel");
            byte[] arg2 = ABIEncoder.encodeOneInteger(numInternals  -1);
            byte[] data = new byte[arg1.length + arg2.length];
            System.arraycopy(arg1, 0, data, 0, arg1.length);
            System.arraycopy(arg2, 0, data, arg1.length, arg2.length);
            Blockchain.call(Blockchain.getAddress(), BigInteger.ZERO, data, Blockchain.getRemainingEnergy());
        }
    }

    public static void spawnInternalTransactionsAndHitLogsAtBottomLevel(int numInternals) {
        if (numInternals > 0) {
            byte[] arg1 = ABIEncoder.encodeOneString("spawnInternalTransactionsAndHitLogsAtBottomLevel");
            byte[] arg2 = ABIEncoder.encodeOneInteger(numInternals  -1);
            byte[] data = new byte[arg1.length + arg2.length];
            System.arraycopy(arg1, 0, data, 0, arg1.length);
            System.arraycopy(arg2, 0, data, arg1.length, arg2.length);
            Blockchain.call(Blockchain.getAddress(), BigInteger.ZERO, data, Blockchain.getRemainingEnergy());
        } else {
            hitLogs();
        }
    }

    public static void spawnInternalTransactionsAndFailAtDepth5(int numInternals) {
        hitLogs();
        if (depth > 4) {
            depth = 0;
            Blockchain.require(false);
        }
        depth++;
        if (numInternals > 0) {
            byte[] arg1 = ABIEncoder.encodeOneString("spawnInternalTransactionsAndFailAtDepth5");
            byte[] arg2 = ABIEncoder.encodeOneInteger(numInternals  -1);
            byte[] data = new byte[arg1.length + arg2.length];
            System.arraycopy(arg1, 0, data, 0, arg1.length);
            System.arraycopy(arg2, 0, data, arg1.length, arg2.length);
            Blockchain.call(Blockchain.getAddress(), BigInteger.ZERO, data, Blockchain.getRemainingEnergy());
        }
    }

    public static void hitLogs() {
        hitLog1();
        hitLog2();
        hitLog3();
        hitLog4();
        hitLog5();
    }

    private static void hitLog1() {
        Blockchain.log(DATA1);
    }

    private static void hitLog2() {
        Blockchain.log(TOPIC1, DATA2);
    }

    private static void hitLog3() {
        Blockchain.log(TOPIC1, TOPIC2, DATA3);
    }

    private static void hitLog4() {
        Blockchain.log(TOPIC1, TOPIC2, TOPIC3, DATA4);
    }

    private static void hitLog5() {
        Blockchain.log(TOPIC1, TOPIC2, TOPIC3, TOPIC4, DATA5);
    }

}
