package org.aion.cli;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;


/**
 * A very simple DApp meant to be used as a demo for the Alpha2 release.
 * The main idea with this is to demonstrate an incredibly simple DApp, which we only observe/verify via debug output, but which also
 * demonstrates the vanilla Java nature of AVM DApps and the transparent storage model.
 * 
 * NOTE:  For now, we will test this via that AvmCLIIntegrationTest, but that might need to be tweaked once we have the alpha2 launcher.
 */
public class SimpleStackDemo {
    private static SimpleStackDemo topOfStack;

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(BlockchainRuntime.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("addNewTuple")) {
                addNewTuple(decoder.decodeOneString());
                return new byte[0];
            } else {
                return new byte[0];
            }
        }
    }

    public static void addNewTuple(String name) {
        // Create the new element.
        Address sender = BlockchainRuntime.getCaller();
        SimpleStackDemo newElt = new SimpleStackDemo(name, sender, topOfStack);
        topOfStack = newElt;
        
        // Walk the new stack, writing it to debug.
        SimpleStackDemo toPrint = topOfStack;
        while (null != toPrint) {
            BlockchainRuntime.println(toPrint.toString());
            toPrint = toPrint.next;
        }
    }


    private final String name;
    private final Address sender;
    private final SimpleStackDemo next;

    public SimpleStackDemo(String name, Address sender, SimpleStackDemo next) {
        this.name = name;
        this.sender = sender;
        this.next = next;
    }

    @Override
    public String toString() {
        return this.name + " (" + this.sender + ")";
    }
}
