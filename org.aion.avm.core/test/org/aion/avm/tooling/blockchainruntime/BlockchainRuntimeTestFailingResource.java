package org.aion.avm.tooling.blockchainruntime;

import java.math.BigInteger;
import org.aion.avm.api.BlockchainRuntime;


public class BlockchainRuntimeTestFailingResource {
    public static byte[] main() {
        // We just want to try to misuse the BlockchainRuntime in various statically incorrect ways and verify that we see the excepted exceptions.
        try {
            BlockchainRuntime.getBalance(null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            BlockchainRuntime.getCodeSize(null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            BlockchainRuntime.call(null, BigInteger.ONE, null, 1L);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            BlockchainRuntime.create(BigInteger.ONE, null, 1L);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            BlockchainRuntime.selfDestruct(null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            BlockchainRuntime.log(null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            BlockchainRuntime.log(null, null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            BlockchainRuntime.log(null, null, null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            BlockchainRuntime.log(null, null, null, null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            BlockchainRuntime.log(null, null, null, null, null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            BlockchainRuntime.blake2b(null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        
        // Once we handle everything, just return the input so the caller knows we at least executed.
        return BlockchainRuntime.getData();
    }
}
