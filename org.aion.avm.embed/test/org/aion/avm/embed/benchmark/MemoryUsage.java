package org.aion.avm.embed.benchmark;

import avm.Address;
import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;


/**
 * Tests memory usage based on creating a linked-list of small elements.
 * This just creates a linked list in the shape of a stack so extra elements can be cheaply added but they account to total
 * memory size.  The total stack can be walked to measure the impact of this size.
 */
public class MemoryUsage {
    private static  Node root;

    static {
        root = new Node(0, Blockchain.getCaller());
    }

    @Callable
    public static void insert(int count) {
        Address caller = Blockchain.getCaller();
        for (int i = 0; i < count; ++i) {
            Node currentRoot = root;
            Node newRoot = new Node(currentRoot.data + 1, caller);
            newRoot.next = root;
            root = newRoot;
        }
    }

    @Callable
    public static int getSum(Address creator) {
        int sum = 0;
        Node next = root;
        while (null != next) {
            if (creator.equals(next.nodeCreator)) {
                sum += next.data;
            }
            next = next.next;
        }
        return sum;
    }


    private static class Node {
        int data;
        Address nodeCreator;
        Node next;

        public Node(int data, Address creator) {
            this.data = data;
            this.nodeCreator = creator;
            this.next = null;
        }
    }
}
