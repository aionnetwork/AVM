package org.aion.avm.tooling.blockchainruntime;

import java.math.BigInteger;
import avm.Blockchain;


public class BlockchainRuntimeTestFailingResource {
    public static byte[] main() {
        // We just want to try to misuse the Blockchain in various statically incorrect ways and verify that we see the excepted exceptions.
        try {
            Blockchain.getBalance(null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            Blockchain.getCodeSize(null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            Blockchain.call(null, BigInteger.ONE, null, 1L);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            Blockchain.create(BigInteger.ONE, null, 1L);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            Blockchain.selfDestruct(null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            Blockchain.log(null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            Blockchain.log(null, null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            Blockchain.log(null, null, null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            Blockchain.log(null, null, null, null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            Blockchain.log(null, null, null, null, null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        try {
            Blockchain.blake2b(null);
            throw new AssertionError();
        } catch (IllegalArgumentException e) {
            // Expected.
        }
        
        // Once we handle everything, just return the input so the caller knows we at least executed.
        return Blockchain.getData();
    }
}
