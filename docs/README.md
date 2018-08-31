# Aion Virtual Mahince

## Introduction

Aion Virtual Machine (AVM) is decentralized application engine which executes Java
bytecode in a deterministic, secure, and instrumented way.

It comes with three components:
- The `core` module, in charge of transforming user bytecode and interacting with kernel;
- The `api` module, a library exposed to developer for accessing blockchain environment;
- The `rt` module, including s restricted Java Class Library(JCL) and runtime data structures.


## Blockchain API and user library

Check the `javadoc` folder


## Supported JCL classes

```
java.lang.AbstractMethodError
java.lang.ArithmeticException
java.lang.ArrayIndexOutOfBoundsException
java.lang.ArrayStoreException
java.lang.AssertionError
java.lang.Boolean
java.lang.BootstrapMethodError
java.lang.Byte
java.lang.CharSequence
java.lang.Character
java.lang.Class
java.lang.ClassCastException
java.lang.ClassCircularityError
java.lang.ClassFormatError
java.lang.ClassNotFoundException
java.lang.CloneNotSupportedException
java.lang.Double
java.lang.Enum
java.lang.EnumConstantNotPresentException
java.lang.Error
java.lang.Exception
java.lang.ExceptionInInitializerError
java.lang.Float
java.lang.IllegalAccessError
java.lang.IllegalAccessException
java.lang.IllegalArgumentException
java.lang.IllegalCallerException
java.lang.IllegalMonitorStateException
java.lang.IllegalStateException
java.lang.IllegalThreadStateException
java.lang.IncompatibleClassChangeError
java.lang.IndexOutOfBoundsException
java.lang.InstantiationError
java.lang.InstantiationException
java.lang.Integer
java.lang.InternalError
java.lang.InterruptedException
java.lang.Iterable
java.lang.LayerInstantiationException
java.lang.LinkageError
java.lang.Long
java.lang.Math
java.lang.NegativeArraySizeException
java.lang.NoClassDefFoundError
java.lang.NoSuchFieldError
java.lang.NoSuchFieldException
java.lang.NoSuchMethodError
java.lang.NoSuchMethodException
java.lang.NullPointerException
java.lang.Number
java.lang.NumberFormatException
java.lang.Object
java.lang.OutOfMemoryError
java.lang.ReflectiveOperationException
java.lang.Runnable
java.lang.RuntimeException
java.lang.SecurityException
java.lang.Short
java.lang.StackOverflowError
java.lang.StrictMath
java.lang.String
java.lang.StringBuffer
java.lang.StringBuilder
java.lang.StringIndexOutOfBoundsException
java.lang.System
java.lang.ThreadDeath
java.lang.Throwable
java.lang.TypeNotPresentException
java.lang.UnknownError
java.lang.UnsatisfiedLinkError
java.lang.UnsupportedClassVersionError
java.lang.UnsupportedOperationException
java.lang.VerifyError
java.lang.VirtualMachineError
java.lang.invoke.LambdaMetafactory
java.lang.invoke.StringConcatFactory
java.math.BigDecimal
java.math.BigInteger
java.math.MathContext
java.math.RoundingMode
java.nio.Buffer
java.nio.ByteBuffer
java.nio.ByteOrder
java.nio.CharBuffer
java.nio.DoubleBuffer
java.nio.FloatBuffer
java.nio.IntBuffer
java.nio.LongBuffer
java.nio.ShortBuffer
java.util.Arrays
java.util.Collection
java.util.Iterator
java.util.List
java.util.ListIterator
java.util.Map
java.util.Map$Entry
java.util.Set
java.util.function.Function
```


## DApp example

```java
package org.aion.avm.core.testExchange;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.AionMap;

public class ERC20Token implements ERC20 {

    private final String name;
    private final String symbol;
    private final int decimals;

    private final Address minter;

    private AionMap<Address, Long> ledger;

    private AionMap<Address, AionMap<Address, Long>> allowance;

    private long totalSupply;

    public ERC20Token(String name, String symbol, int decimals, Address minter) {
        this.name = name;
        this.symbol = symbol;
        this.decimals = decimals;
        this.minter = minter;
        this.ledger = new AionMap<>();
        this.allowance = new AionMap<>();
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

    public long totalSupply() {
        return totalSupply;
    }

    public long balanceOf(Address tokenOwner) {
        return this.ledger.getOrDefault(tokenOwner, 0L);
    }

    public long allowance(Address tokenOwner, Address spender) {
        if (!this.allowance.containsKey(tokenOwner)) {
            return 0L;
        }

        return this.allowance.get(tokenOwner).getOrDefault(spender, 0L);
    }

    public boolean transfer(Address receiver, long tokens) {
        Address sender = BlockchainRuntime.getCaller();

        long senderBalance = this.ledger.getOrDefault(sender, 0L);
        long receiverBalance = this.ledger.getOrDefault(receiver, 0L);

        if ((senderBalance >= tokens) && (tokens > 0) && (receiverBalance + tokens > 0)) {
            this.ledger.put(sender, senderBalance - tokens);
            this.ledger.put(receiver, receiverBalance + tokens);
            BlockchainRuntime.log("Transfer".getBytes(), sender.unwrap(), receiver.unwrap(), Long.toString(tokens).getBytes());
            return true;
        }

        return false;
    }

    public boolean approve(Address spender, long tokens) {
        Address sender = BlockchainRuntime.getCaller();

        if (!this.allowance.containsKey(sender)) {
            AionMap<Address, Long> newEntry = new AionMap<>();
            this.allowance.put(sender, newEntry);
        }

        BlockchainRuntime.log("Approval".getBytes(), sender.unwrap(), spender.unwrap(), Long.toString(tokens).getBytes());
        this.allowance.get(sender).put(spender, tokens);

        return true;
    }

    public boolean transferFrom(Address from, Address to, long tokens) {
        Address sender = BlockchainRuntime.getCaller();

        long fromBalance = this.ledger.getOrDefault(from, 0L);
        long toBalance = this.ledger.getOrDefault(to, 0L);

        long limit = allowance(from, sender);

        if ((fromBalance > tokens) && (limit > tokens) && (toBalance + tokens > 0)) {
            BlockchainRuntime.log("Transfer".getBytes(), from.unwrap(), to.unwrap(), Long.toString(tokens).getBytes());
            this.ledger.put(from, fromBalance - tokens);
            this.allowance.get(from).put(sender, limit - tokens);
            this.ledger.put(to, toBalance + tokens);
            return true;
        }

        return false;
    }

    public boolean mint(Address receiver, long tokens) {
        if (BlockchainRuntime.getCaller().equals(this.minter)) {
            long receiverBalance = this.ledger.getOrDefault(receiver, 0L);
            if ((tokens > 0) && (receiverBalance + tokens > 0)) {
                BlockchainRuntime.log("Mint".getBytes(), receiver.unwrap(), Long.toString(tokens).getBytes());
                this.ledger.put(receiver, receiverBalance + tokens);
                this.totalSupply += tokens;
                return true;
            }
        }
        return false;
    }

    private static ERC20 token;

    /**
     * Initialization code executed once at the Dapp deployment.
     * Read the transaction data, decode it and construct the token instance with the decoded arguments.
     * This token instance is transparently put into storage.
     */
    static {
        Object[] arguments = ABIDecoder.decodeArguments(BlockchainRuntime.getData());
        String name = new String((char[]) arguments[0]);
        String symbol = new String((char[]) arguments[1]);
        int decimals = (int) arguments[2];
        Address minter = BlockchainRuntime.getCaller();

        token = new ERC20Token(name, symbol, decimals, minter);
    }

    /**
     * Entry point at a transaction call.
     * Read the transaction data, decode it and run the specified method of the token class with the decoded arguments.
     * The token instance is loaded transparently from the storage in prior.
     * @return the encoded return data of the method being called.
     */
    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithObject(token, BlockchainRuntime.getData());
    }
}
```


## User guide

See [USER_GUIDE.md](./USER_GUIDE.md)
