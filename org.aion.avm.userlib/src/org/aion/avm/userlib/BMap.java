package org.aion.avm.userlib;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BMap<K, V> implements Map<K, V> {

    // Current size of the map
    private int size;

    // The root of the BMap
    // It will be a BLeafNode when size of the map is less than MAX_LEAF_SIZE
    // It will be a BInternalNode when size of the map is greater than MAX_INTERNAL_SIZE
    private BNode root;

    // The maximum size (number of routers) of the internal node
    private int order = 2;

    public BMap(){
        this.size = 0;
        this.root = new BLeafNode();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object key) {
        keyNullCheck((K) key);

        BEntry entry = this.searchForLeaf((K)key).searchForEntry(key);

        return (null == entry) ? null : (V) entry.value;
    }

    @Override
    public V put(K key, V value) {
        keyNullCheck(key);

        V ret = null;

        BLeafNode leaf = this.searchForLeaf(key);
        BEntry entry = leaf.searchForEntry(key);

        if (null == entry){
            // If entry is not present, add it into the leaf node
            bInsert(key, value);
            size = size + 1;
        }else{
            // If entry is present, replace it with new value
            ret = (V) entry.getValue();
            entry.setValue(value);
        }

        return ret;
    }

    @Override
    //TODO
    public V remove(Object key) {
        V ret = (V) root.delete(key);

        if (null != ret){size--;}

        return ret;
    }

    @Override
    //TODO
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {
        this.size = 0;
        this.root = new BLeafNode();
    }

    @Override
    public Set<K> keySet() {
        BLeafNode cur = getLeftMostLeaf();
        Set<K> ret = new AionSet<>();

        while (null != cur){
            for (int i = 0; i < cur.nodeSize; i ++){
                ret.add((K) cur.entries[i].key);
            }
            cur = (BLeafNode) cur.next;
        }

        return ret;
    }

    @Override
    public Collection<V> values() {
        BLeafNode cur = getLeftMostLeaf();
        List<V> ret = new AionList<>();

        while (null != cur){
            for (int i = 0; i < cur.nodeSize; i ++){
                ret.add((V) cur.entries[i].value);
            }
            cur = (BLeafNode) cur.next;
        }

        return ret;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        BLeafNode cur = getLeftMostLeaf();
        Set<Entry<K, V>> ret = new AionSet<>();

        while (null != cur){
            for (int i = 0; i < cur.nodeSize; i ++){
                ret.add(cur.entries[i]);
            }
            cur = (BLeafNode) cur.next;
        }

        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }


    abstract class BNode<K, V>{
        int nodeSize;

        BNode parent;

        BNode next;

        BNode pre;

        abstract void insertNonFull(K key, V value);

        abstract V delete(K key);

        abstract void splitChild(int i);

        void rebalance(){
            if (this.isUnderflow() && root != this){
                // Check if we can borrow from left sibling
                if (null != this.pre){
                    // Check if we can borrow from left sibling
                    if (!this.pre.isMinimal()){
                        //Borrow
                        borrowFromLeft();
                    }else{
                        mergeToLeft();
                    }
                }else{
                    // Check if we can borrow from right sibling
                    if (!this.next.isMinimal()){
                        //Borrow
                        borrowFromRight();
                    }else{
                        mergeToRight();
                    }
                }
            }else if (root == this && needCollapse()){
                collapseRoot();
            }
        }

        boolean isUnderflow(){
            return nodeSize < order;
        }

        boolean isMinimal(){
            return nodeSize == order;
        }

        boolean needCollapse(){
            return nodeSize == 1;
        }

        BNode getAnchor(BNode target){
            // Find the common ancestor of this and target
            BNode thisAnchor = this.parent;
            BNode targetAnchor = target.parent;

            while (thisAnchor != targetAnchor){
                thisAnchor = thisAnchor.parent;
                targetAnchor = targetAnchor.parent;
            }

            return thisAnchor;
        }

        abstract void borrowFromLeft();

        abstract void mergeToLeft();

        abstract void borrowFromRight();

        abstract void mergeToRight();

        abstract void collapseRoot();

    }

    /**
     * The Internal node of a BTree
     */
    class BTreeNode<K, V> extends BNode <K, V>{
        // Routers array for navigation
        private int[] routers;

        // The children of an internal node.
        // Children are either all internal nodes or all leaf nodes.
        BNode<K, V>[] children;

        BTreeNode(){
            this.routers = new int[2 * order - 1];
            this.children = new BNode[2 * order];
        }

        @Override
        void insertNonFull(K key, V value) {
            int i = this.nodeSize - 1;
            while (i > 0 && key.hashCode() < this.routers[i - 1]){
                i--;
            }

            if (this.children[i].nodeSize == (2 * order)){
                bSplitChild(this, i);
                if (key.hashCode() >= this.routers[i]){
                    i++;
                }
            }
            this.children[i].insertNonFull(key, value);
        }

        @Override
        V delete(K key) {
            V ret = null;
            int i = this.nodeSize - 1;
            while (i > 0 && key.hashCode() < this.routers[i - 1]){
                i--;
            }

            ret = this.children[i].delete(key);

            // Delete succeed, check router and rebalance
            if (ret != null){
                if (this.recalibrate()){
                    this.rebalance();
                }
            }
            return ret;
        }

        //TODO
        private boolean recalibrate(){
            boolean ret = false;

            // If the first child is removed, shift both children and routers left by 1
            if (0 == this.children[0].nodeSize){
                System.arraycopy(this.children, 1, this.children, 0, this.nodeSize);
                if (this.nodeSize > 2) {
                    System.arraycopy(this.routers, 1, this.routers, 0, this.nodeSize - 1);
                }

                ret = true;
            }else {
                // Search for the removed children
                for (int i = 1; i < this.nodeSize; i++) {
                    if (0 == this.children[i].nodeSize) {
                        // Empty node
                        System.arraycopy(this.children, i + 1, this.children, i    , this.nodeSize - i);
                        if (this.nodeSize > 2) {
                            System.arraycopy(this.routers, i, this.routers, i - 1, this.nodeSize - i);
                        }
                        ret = true;
                        break;
                    }
                }
            }

            //update the first router

            if (ret) this.nodeSize--;
            return ret;
        }

        @Override
        void borrowFromLeft(){
            BTreeNode leftNode = (BTreeNode)this.pre;
            BNode fChild = this.children[0];

            BTreeNode anchor = (BTreeNode) getAnchor(leftNode);
            int slot = findSlot(anchor, leftNode, this);

            // Shift current tree node to right by 1, insert the right most node from left sibling
            // Shift is always safe
            System.arraycopy(this.routers,  0, this.routers,  1, this.nodeSize - 1);
            System.arraycopy(this.children, 0, this.children, 1, this.nodeSize);

            // Set new head router
            this.routers[0] = anchor.routers[slot];
            anchor.routers[slot] = leftNode.routers[leftNode.nodeSize - 2];
            // Move last children from leftNode
            this.children[0] = leftNode.children[leftNode.nodeSize - 1];
            this.children[0].parent = this;
            leftNode.children[leftNode.nodeSize - 1] = null;
            this.nodeSize++;
            leftNode.nodeSize--;
        }

        @Override
        void mergeToLeft(){
            BTreeNode leftNode = (BTreeNode)this.pre;

            BTreeNode anchor = (BTreeNode) getAnchor(leftNode);
            int slot = findSlot(anchor, leftNode, this);

            for (int i = 0; i < this.nodeSize; i++){
                this.children[i].parent = leftNode;
            }

            // Move this node to the tail of the leftNode
            System.arraycopy(this.routers,  0, leftNode.routers,  leftNode.nodeSize, this.nodeSize - 1);
            System.arraycopy(this.children, 0, leftNode.children, leftNode.nodeSize, this.nodeSize);

            leftNode.routers[leftNode.nodeSize - 1] = anchor.routers[slot];
            leftNode.nodeSize += this.nodeSize;
            this.nodeSize = 0;

            if (null != this.next){this.next.pre = leftNode;}
            leftNode.next = this.next;
        }

        @Override
        void borrowFromRight(){
            BTreeNode rightNode = (BTreeNode)this.next;
            BNode childToMove = rightNode.children[0];

            BTreeNode anchor = (BTreeNode) getAnchor(rightNode);
            int slot = findSlot(anchor, this, rightNode);

            // Move the head of the right node to the tail of the current node
            this.children[this.nodeSize] = childToMove;
            childToMove.parent = this;
            this.routers[this.nodeSize - 1] = anchor.routers[slot];
            anchor.routers[slot] = rightNode.routers[0];

            System.arraycopy(rightNode.routers,  1, rightNode.routers,  0, rightNode.nodeSize - 2);
            System.arraycopy(rightNode.children, 1, rightNode.children, 0, rightNode.nodeSize - 1);
            this.nodeSize++;
            rightNode.nodeSize--;
        }

        @Override
        void mergeToRight(){
            BTreeNode rightNode = (BTreeNode)this.next;

            BTreeNode anchor = (BTreeNode) getAnchor(rightNode);
            int slot = findSlot(anchor, this, rightNode);

            for (int i = 0; i < this.nodeSize; i++){
                this.children[i].parent = rightNode;
            }

            // Move this node to the head of the rightNode
            System.arraycopy(rightNode.routers, 0, rightNode.routers, this.nodeSize, rightNode.nodeSize - 1);
            System.arraycopy(this.routers,      0, rightNode.routers, 0,             this.nodeSize - 1);

            System.arraycopy(rightNode.children, 0, rightNode.children, this.nodeSize, rightNode.nodeSize);
            System.arraycopy(this.children,      0, rightNode.children, 0,             this.nodeSize);

            rightNode.routers[this.nodeSize - 1] = anchor.routers[slot];
            rightNode.nodeSize += this.nodeSize;
            this.nodeSize = 0;

            if (null != this.pre){this.pre.next = rightNode;}
            rightNode.pre = this.pre;
        }

        @Override
        void collapseRoot() {
            // Collapse
            root = this.children[0];
            root.parent = null;
        }

        @Override
        void splitChild(int i) {

        }

    }

    class BLeafNode<K, V> extends BNode <K, V>{
        // Entry array for data storage
        private BEntry<K, V>[] entries;

        BLeafNode(){
            this.entries = new BEntry[2 * order];
        }

        // Search for entry within this leaf node
        // TODO: Adopt log(N) search for entry within leaf node
        public BEntry searchForEntry(K key){
            int i = 0;
            while (i < nodeSize && !key.equals(entries[i].getKey())){
                i = i + 1;
            }

            if (i < nodeSize && key.equals(entries[i].getKey())){
                return entries[i];
            }

            return null;
        }

        @Override
        void insertNonFull(K key, V value) {
            int i = this.nodeSize;
            while (i > 0 && key.hashCode() < this.entries[i - 1].hashCode()){
                //this.entries[i] = this.entries[i - 1];
                i--;
            }

            System.arraycopy(this.entries, i, this.entries, i + 1, this.nodeSize - i);
            this.entries[i] = new BEntry(key, value);
            this.nodeSize++;
        }

        @Override
        V delete(K key) {
            V ret = null;

            int i = 0;
            while (i < nodeSize && !key.equals(entries[i].getKey())){
                i = i + 1;
            }

            if (i < nodeSize && key.equals(entries[i].getKey())){
                ret = entries[i].getValue();

                // Shift and remove entry
                System.arraycopy(this.entries, i + 1, this.entries, i, this.nodeSize - i - 1);
                entries[nodeSize - 1] = null;
                this.nodeSize--;

                this.rebalance();
            }

            return ret;
        }

        @Override
        void borrowFromLeft(){
            BLeafNode leftNode = (BLeafNode)this.pre;

            // Need to update router within anchor node
            BTreeNode anchor = (BTreeNode) getAnchor(leftNode);
            int slot = findSlot(anchor, leftNode, this);

            // Shift current leaf to right by 1, insert the right most node from left sibling
            // Shift is always safe
            System.arraycopy(this.entries, 0, this.entries, 1, this.nodeSize);
            this.entries[0] = leftNode.entries[leftNode.nodeSize - 1];
            leftNode.entries[leftNode.nodeSize - 1] = null;
            this.nodeSize++;
            leftNode.nodeSize--;

            anchor.routers[slot] = this.entries[0].hashCode();
        }

        @Override
        void mergeToLeft(){
            BLeafNode leftNode = (BLeafNode)this.pre;
            // Move this node to the tail of the leftNode
            System.arraycopy(this.entries, 0, leftNode.entries, leftNode.nodeSize, this.nodeSize);
            leftNode.nodeSize += this.nodeSize;
            this.nodeSize = 0;

            if (null != this.next){this.next.pre = leftNode;}
            leftNode.next = this.next;
        }

        @Override
        void borrowFromRight(){
            BLeafNode rightNode = (BLeafNode)this.next;

            // Need to update router within anchor node
            BTreeNode anchor = (BTreeNode) getAnchor(rightNode);
            int slot = findSlot(anchor, this, rightNode);

            // Move the head of the right node to the tail of the current node
            this.entries[this.nodeSize] = rightNode.entries[0];
            System.arraycopy(rightNode.entries, 1, rightNode.entries, 0, rightNode.nodeSize - 1);
            this.nodeSize++;
            rightNode.nodeSize--;

            anchor.routers[slot] = rightNode.entries[0].hashCode();
        }

        @Override
        void mergeToRight(){
            BLeafNode rightNode = (BLeafNode)this.next;
            // Move this node to the head of the rightNode
            System.arraycopy(rightNode.entries, 0, rightNode.entries, this.nodeSize, rightNode.nodeSize);
            System.arraycopy(this.entries, 0, rightNode.entries, 0, this.nodeSize);
            rightNode.nodeSize += this.nodeSize;
            this.nodeSize = 0;

            if (null != this.pre){this.pre.next = rightNode;}
            rightNode.pre = this.pre;
        }

        @Override
        void collapseRoot() {
            // Do nothing is root is leaf node
        }

        @Override
        void splitChild(int i) {

        }

    }

    // Entry (key value pair) of the BMap which implements Map.Entry
    class BEntry<K, V> implements Map.Entry<K, V>{

        private K key;

        private V value;

        public BEntry(K key, V value){
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V ret = this.value;
            this.value = value;
            return ret;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        // Since our map is hashcode based, the hashcode of an entry is defined as hashcode of the key.
        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }


    private void keyNullCheck(Object key){
        if (null == key){
            throw new NullPointerException("BMap does not allow empty key.");
        }
    }

    // Search for the leaf node of the given key.
    private BLeafNode searchForLeaf(K key){
        BNode cur = this.root;

        while (!(cur instanceof BLeafNode)){
            BTreeNode tmp = (BTreeNode) cur;

            int i = tmp.nodeSize - 1;
            while (i > 0 && key.hashCode() < tmp.routers[i - 1]){
                i--;
            }
            cur = tmp.children[i];
        }

        return (BLeafNode)cur;
    }

    private BLeafNode getLeftMostLeaf(){
        BNode cur = this.root;

        while (!(cur instanceof BLeafNode)){
            cur = ((BTreeNode) cur).children[0];
        }

        return (BLeafNode)cur;
    }

    private void bInsert(K key, V value){
        BNode r = this.root;
        if (r.nodeSize == ((2 * order))){
            BTreeNode s = new BTreeNode();
            this.root = s;
            s.nodeSize = 1;
            s.children[0] = r;
            r.parent = s;
            bSplitChild(s, 0);
            s.insertNonFull(key, value);
        }else{
            r.insertNonFull(key, value);
        }
    }

    private void bSplitChild(BTreeNode x, int i){
        if (x.children[i] instanceof BTreeNode){
            bSplitTreeChild(x, i);
        }else{
            bSplitLeafChild(x, i);
        }
    }

    private void bSplitTreeChild(BTreeNode x, int i){
        // Left node
        BTreeNode y = (BTreeNode) x.children[i];
        // Right node
        BTreeNode z = new BTreeNode();

        // Right node has t children
        z.nodeSize = order;

        System.arraycopy(y.routers , order, z.routers , 0, order - 1);
        System.arraycopy(y.children, order, z.children, 0, order);
        for (int j = 0; j < order; j++){
            z.children[j].parent = z;
        }

        // Left node has t children
        y.nodeSize = order;
        // Remove reference from left node
        // TODO: This may not be necessary
        int newXRouter = y.routers[order - 1];
        for (int j = order; j < (2 * order) - 1; j++){
            y.routers [j] = 0;
            y.children[j] = null;
        }
        y.routers [order - 1] = 0;
        y.children[2 * order - 1] = null;

        // Link tree node
        z.next = y.next;
        if (null != y.next) {
            y.next.pre = z;
        }
        z.pre = y;
        y.next = z;
        y.parent = x;
        z.parent = x;

        // Shift parent node
        if (x.nodeSize > 1) {
            System.arraycopy(x.routers, i, x.routers, i + 1, x.nodeSize - i - 1);
            System.arraycopy(x.children, i, x.children, i + 1, x.nodeSize - i);
        }

        x.children[i + 1] = z;
        x.routers [i] = newXRouter;
        x.nodeSize++;
    }

    private void bSplitLeafChild(BTreeNode x, int i){
        BLeafNode y = (BLeafNode) x.children[i];
        BLeafNode z = new BLeafNode();

        // Right node has t children
        z.nodeSize = order;
        // Move t children to right node
        System.arraycopy(y.entries, order, z.entries, 0, order);

        y.nodeSize = order;
        // Remove reference from left node to prevent future memory leak
        // TODO: This may not be necessary
        for (int j = order; j < (2 * order); j++){
            y.entries[j] = null;
        }

        // Link leaf nodes
        z.next = y.next;
        if (null != y.next) {
            y.next.pre = z;
        }
        z.pre = y;
        y.next = z;
        y.parent = x;
        z.parent = x;

        // Shift parent node
        if (x.nodeSize > 0) {
            System.arraycopy(x.routers, i, x.routers, i + 1, x.nodeSize - i - 1);
            System.arraycopy(x.children, i, x.children, i + 1, x.nodeSize - i);
        }

        x.children[i + 1] = z;
        x.routers [i] = z.entries[0].hashCode();
        x.nodeSize++;
    }

    int findSlot(BTreeNode anchor, BLeafNode left, BLeafNode right){
        int lvalue = left.entries[left.nodeSize - 1].hashCode();
        int rvalue = right.entries[0].hashCode();

        int i = 0;
        while (!(lvalue < anchor.routers[i] && rvalue >= anchor.routers[i])){
            i++;
        }
        return i;
    }

    int findSlot(BTreeNode anchor, BTreeNode left, BTreeNode right) {

        int lvalue = left.routers[0];
        int rvalue = right.routers[0];

        assert (lvalue < rvalue);

        int i = 0;
        while (!(lvalue < anchor.routers[i] && rvalue >= anchor.routers[i])) {
            i++;
        }

        return i;
    }
}
