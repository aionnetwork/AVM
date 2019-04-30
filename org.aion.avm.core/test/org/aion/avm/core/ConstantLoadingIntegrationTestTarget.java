package org.aion.avm.core;

import avm.Blockchain;


/**
 * The test class loaded by ConstantLoadingIntegrationTest.
 */
public class ConstantLoadingIntegrationTestTarget {
    private interface II {
        default String getString() {
            return "STRING";
        }
    }

    private static class Bare implements II {
    }

    private static class Populated implements II {
        @Override
        public String getString() {
            return "POP";
        }
    }

    private static Bare bare;
    private static Populated populated;

    public static byte[] main() {
        byte arg = Blockchain.getData()[0];
        byte[] result = null;
        switch (arg) {
        case 0:
            bare = new Bare();
            result = new byte[] { (byte)bare.hashCode(), (byte)bare.getString().length() };
            break;
        case 1:
            populated = new Populated();
            result = new byte[] { (byte)populated.hashCode(), (byte)populated.getString().length() };
            break;
        case 2:
            result = new byte[] { (byte)bare.hashCode(), (byte)bare.getString().length() };
            break;
        case 3:
            result = new byte[] { (byte)populated.hashCode(), (byte)populated.getString().length() };
            break;
            default:
                result = null;
        }
        return result;
    }
}
