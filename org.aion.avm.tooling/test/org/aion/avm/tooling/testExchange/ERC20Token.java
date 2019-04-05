package org.aion.avm.tooling.testExchange;

import avm.Address;
import avm.BlockchainRuntime;
import org.aion.avm.userlib.AionMap;

public class ERC20Token {

    private static String name;
    private static String symbol;
    private static int decimals;

    private static Address minter;

    private static AionMap<Address, Long> ledger;

    private static AionMap<Address, AionMap<Address, Long>> allowance;

    private static long totalSupply;

    public static void init(String name, String symbol, int decimals, Address minter) {
        ERC20Token.name = name;
        ERC20Token.symbol = symbol;
        ERC20Token.decimals = decimals;
        ERC20Token.minter = minter;
        ERC20Token.ledger = new AionMap<>();
        ERC20Token.allowance = new AionMap<>();
    }

    public String name() {
        return name;
    }

    public String symbol() {
        return symbol;
    }

    public int decimals() {
        return decimals;
    }

    public static long totalSupply() {
        return totalSupply;
    }

    public static long balanceOf(Address tokenOwner) {
        return ledger.getOrDefault(tokenOwner, 0L);
    }

    public static long allowance(Address tokenOwner, Address spender) {
        if (!allowance.containsKey(tokenOwner)) {
            return 0L;
        }

        return allowance.get(tokenOwner).getOrDefault(spender, 0L);
    }

    public static boolean transfer(Address receiver, long tokens) {
        Address sender = BlockchainRuntime.getCaller();

        long senderBalance = ledger.getOrDefault(sender, 0L);
        long receiverBalance = ledger.getOrDefault(receiver, 0L);

        if ((senderBalance >= tokens) && (tokens > 0) && (receiverBalance + tokens > 0)) {
            ledger.put(sender, senderBalance - tokens);
            ledger.put(receiver, receiverBalance + tokens);
            BlockchainRuntime.log("Transfer".getBytes(), sender.unwrap(), receiver.unwrap(), Long.toString(tokens).getBytes());
            return true;
        }

        return false;
    }

    public static boolean approve(Address spender, long tokens) {
        Address sender = BlockchainRuntime.getCaller();

        if (!allowance.containsKey(sender)) {
            AionMap<Address, Long> newEntry = new AionMap<>();
            allowance.put(sender, newEntry);
        }

        BlockchainRuntime.log("Approval".getBytes(), sender.unwrap(), spender.unwrap(), Long.toString(tokens).getBytes());
        allowance.get(sender).put(spender, tokens);

        return true;
    }

    public static boolean transferFrom(Address from, Address to, long tokens) {
        Address sender = BlockchainRuntime.getCaller();

        long fromBalance = ledger.getOrDefault(from, 0L);
        long toBalance = ledger.getOrDefault(to, 0L);

        long limit = allowance(from, sender);

        if ((fromBalance > tokens) && (limit > tokens) && (toBalance + tokens > 0)) {
            BlockchainRuntime.log("Transfer".getBytes(), from.unwrap(), to.unwrap(), Long.toString(tokens).getBytes());
            ledger.put(from, fromBalance - tokens);
            allowance.get(from).put(sender, limit - tokens);
            ledger.put(to, toBalance + tokens);
            return true;
        }

        return false;
    }

    public static boolean mint(Address receiver, long tokens) {
        if (BlockchainRuntime.getCaller().equals(minter)) {
            long receiverBalance = ledger.getOrDefault(receiver, 0L);
            if ((tokens > 0) && (receiverBalance + tokens > 0)) {
                BlockchainRuntime.log("Mint".getBytes(), receiver.unwrap(), Long.toString(tokens).getBytes());
                ledger.put(receiver, receiverBalance + tokens);
                totalSupply += tokens;
                return true;
            }
        }
        return false;
    }
}
