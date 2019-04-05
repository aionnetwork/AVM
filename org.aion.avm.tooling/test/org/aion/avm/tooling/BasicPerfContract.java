package org.aion.avm.tooling;

import avm.Address;
import avm.BlockchainRuntime;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;


/**
 * BasicPerfContract is designed as a contrived system around graph building:
 * -the world consists of a set of nodes (data elements) and a set of users (corresponding to external addresses)
 * -each node consists of a value, block number of last update, a list of edges, and the index of the next edge a user must take
 * -each user consists of a balance, current node, previous node, and a marked node
 * -each iteration, each user submits a "take action" transaction (in the future, each iteration will probably be a block)
 * -this "action" is mechanically defined:
 *  -"move" is the preferred action but is not possible if the next edge index points to the user's previous location
 *   -on successful move, the next edge index of the source is incremented, the value of the destination is updated, added to the user's balance,
 *    and set to zero
 *  -"create and move" is next attempted, assuming the user has the balance to afford it
 *   -on success, this creates a new node with a zero value, connected to the location of the user when they took the action and then moves them
 *    to this new node
 *  -"connect and mark" is the final attempt, creating an edge between the current location and the marked location, assuming they aren't already
 *   connected and that the marked doesn't have more edges than the current
 *   -on success, the user is moved to the marked location, its marked location is updated to the previous location, and otherwise proceeds as a move
 *  -if nothing else is possible automatically moved to the initial block of the system (just like a move)
 * -the "value" of a node is updated when something changes about the block:
 *  -take current block versus last updated block multiplied by the number of edges
 *  -add this to the existing value and update the last updated block to current block
 * 
 * The graph can be of any size, all edges in the graph are bidirectional, and the users represent roots pointing into this graph.  The main point of
 * this is to demonstrate a graph-based data structure which can be manipulated for correctness/performance testing.
 */
public class BasicPerfContract {
    private static final long CREATE_NODE_COST = 1000l;
    private static final Node GENESIS = new Node(BlockchainRuntime.getBlockNumber());
    static {
        // We actually need a cluster of 3 "genesis" nodes, so that "movement" can be defined.
        Node two = new Node(BlockchainRuntime.getBlockNumber());
        Node three = new Node(BlockchainRuntime.getBlockNumber());
        two.attachNewNode(three);
        GENESIS.attachNewNode(two);
        GENESIS.attachNewNode(three);
    }
    
    private static final AionMap<Address, User> userLocations = new AionMap<>();
    private static long nodeCount = 3l;

    public static byte[] main() {
        // Look up the user (potentially creating one if they are new).
        Address sender = BlockchainRuntime.getCaller();
        User thisUser = userLocations.get(sender);
        if (null == thisUser) {
            thisUser = new User();
            userLocations.put(sender, thisUser);
        }
        
        // Decide what to do:
        boolean didTakeAction = false;
        // 1) Move.
        if (!didTakeAction) {
            BlockchainRuntime.log("move".getBytes(), new byte[0]);
            didTakeAction = thisUser.tryToMove();
        }
        // 2) Create.
        if (!didTakeAction) {
            BlockchainRuntime.log("create".getBytes(), new byte[0]);
            didTakeAction = thisUser.tryToCreate();
        }
        // 3) Connect.
        if (!didTakeAction) {
            BlockchainRuntime.log("connect".getBytes(), new byte[0]);
            didTakeAction = thisUser.tryToConnect();
        }
        // 4) Default to move to GENESIS.
        if (!didTakeAction) {
            BlockchainRuntime.log("default".getBytes(), new byte[0]);
            thisUser.defaultToGenesis();
        }
        
        // We don't really care about the response, yet.
        return new byte[] {(byte) nodeCount};
    }

    private static class Node {
        private long value;
        private long lastUpdateBlockNumber;
        private AionList<Node> edges;
        private int nextEdgeToTake;
        
        public Node(long blockNumber) {
            this.value = 0;
            this.lastUpdateBlockNumber = blockNumber;
            this.edges = new AionList<>();
            this.nextEdgeToTake = -1;
        }
        
        public Node getNextPossible() {
            return this.edges.get(this.nextEdgeToTake);
        }
        
        public void attachNewNode(Node node) {
            // Act on us.
            updateValue();
            int initialSize = this.edges.size();
            this.edges.add(node);
            this.nextEdgeToTake = initialSize;
            
            // Act on them.
            node.updateValue();
            int theirSize = node.edges.size();
            node.edges.add(this);
            node.nextEdgeToTake = theirSize;
        }
        
        public void updateValue() {
            long time = BlockchainRuntime.getBlockNumber() - this.lastUpdateBlockNumber;
            long edgeCount = (long)(this.edges.size());
            this.value += (time * edgeCount);
            this.lastUpdateBlockNumber = BlockchainRuntime.getBlockNumber();
        }
        
        public void advanceIndex() {
            this.nextEdgeToTake = (this.nextEdgeToTake + 1) % this.edges.size();
        }
        
        public long takeValue() {
            long temp = this.value;
            this.value = 0;
            return temp;
        }

        public boolean isConnectedTo(Node markedNode) {
            // Note that Node instances can only be equal if instance-equal.
            return this.edges.contains(markedNode);
        }

        public int getEdgeCount() {
            return this.edges.size();
        }
    }

    private static class User {
        private long balance;
        private Node currentNode;
        private Node previousNode;
        private Node markedNode;
        
        public User() {
            this.currentNode = GENESIS;
            // Just say the previous is genesis so we know this is never null.
            this.previousNode = GENESIS;
            this.markedNode = GENESIS;
        }
        
        public boolean tryToMove() {
            boolean didMove = false;
            Node nextNodePossible = this.currentNode.getNextPossible();
            if (nextNodePossible != this.previousNode) {
                // This is good so take this.
                doMove(nextNodePossible);
                didMove = true;
            }
            return didMove;
        }
        
        public boolean tryToCreate() {
            boolean didCreate = false;
            if (CREATE_NODE_COST <= this.balance) {
                // We can afford it so make it happen.
                this.balance -= CREATE_NODE_COST;
                // Create the node.
                Node newNode = new Node(BlockchainRuntime.getBlockNumber());
                nodeCount += 1;
                // Connect the node.
                this.currentNode.attachNewNode(newNode);
                // Move to the node.
                doMove(newNode);
                didCreate = true;
                BlockchainRuntime.log("CREATE".getBytes(), new byte[0]);
            }
            return didCreate;
        }
        
        public boolean tryToConnect() {
            boolean didConnect = false;
            // Make sure that they aren't the same.
            if (this.currentNode != this.markedNode) {
                // Make sure that they aren't already connected and that the marked doesn't have more edges than current.
                if (!this.currentNode.isConnectedTo(this.markedNode) && (this.currentNode.getEdgeCount() >= this.markedNode.getEdgeCount())) {
                    // This follows the rules so connect them.
                    this.currentNode.attachNewNode(this.markedNode);
                    // Update our marked node.
                    this.markedNode = this.currentNode;
                    // Move us.
                    doMove(this.markedNode);
                    didConnect = true;
                }
            }
            return didConnect;
        }
        
        public void defaultToGenesis() {
            doMove(GENESIS);
        }
        
        private void doMove(Node newNode) {
            // Change the previous node.
            this.previousNode.advanceIndex();
            
            // Change the new node.
            newNode.updateValue();
            long value = newNode.takeValue();
            
            // Change us.
            this.balance += value;
            this.previousNode = this.currentNode;
            this.currentNode = newNode;
        }
    }
}
