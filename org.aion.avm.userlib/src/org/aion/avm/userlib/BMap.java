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
    private int order = 5;

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
        return null;
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
            cur = cur.nextLeaf;
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
            cur = cur.nextLeaf;
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
            cur = cur.nextLeaf;
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

        private BNode parent;

        abstract void insertNonFull(K key, V value);

        abstract void splitChild(int i);

        abstract BNode combine();
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
            this.routers = new int[2 * order];
            this.children = new BNode[2 * order + 1];
        }

        @Override
        void insertNonFull(K key, V value) {

            int i = this.nodeSize;
            while (i > 0 && key.hashCode() < this.routers[i - 1]){
                i--;
            }
            if (this.children[i].nodeSize == (2 * order)){
                bSplitChild(this, i);
                if (key.hashCode() > this.routers[i]){
                    i++;
                }
            }
            this.children[i].insertNonFull(key, value);
        }

        @Override
        void splitChild(int i) {

        }

        @Override
        BNode combine() {
            return null;
        }
    }

    class BLeafNode<K, V> extends BNode <K, V>{
        // Entry array for data storage
        private BEntry<K, V>[] entries;

        // Pointer to next leaf node for fast navigation and fast remove.
        private BLeafNode nextLeaf;

        // Pointer to previousLeaf leaf node for fast navigation and fast remove.
        private BLeafNode previousLeaf;

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
                this.entries[i] = this.entries[i - 1];
                i--;
            }
            this.entries[i] = new BEntry(key, value);
            this.nodeSize++;
        }

        @Override
        void splitChild(int i) {

        }

        @Override
        BNode combine() {
            return null;
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

    private void keyNullCheck(K key){
        if (null == key){
            throw new NullPointerException("BMap does not allow empty key.");
        }
    }

    // Search for the leaf node of the given key.
    private BLeafNode searchForLeaf(K key){
        BNode cur = this.root;

        while (!(cur instanceof BLeafNode)){
            BTreeNode tmp = (BTreeNode) cur;
            int i = 0;
            while (i < cur.nodeSize && key.hashCode() < tmp.routers[i]){
                i = i + 1;
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
            s.nodeSize = 0;
            s.children[0] = r;
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
        // Move t children to right node
        for (int j = 0; j < order; j++){
            z.routers [j] = y.routers [j + order];
            z.children[j] = y.children[j + order];
        }
        z.children[order] = y.children[2 * order];

        // Left node has t - 1 children
        y.nodeSize = order;
        // Remove reference from left node TODO: This may not be necessary
        for (int j = order; j < (2 * order); j++){
            y.routers [j] = 0;
            y.children[j] = null;
        }
        y.children[2 * order] = null;

        // Shift parent node
        for (int j = x.nodeSize; j > i; j--){
            x.children[j]     = x.children[j - 1];
            x.routers [j + 1] = x.routers [j];
        }

        x.children[i + 1] = z;
        x.routers [i]     = z.routers[0];
        x.nodeSize++;
    }

    private void bSplitLeafChild(BTreeNode x, int i){
        BLeafNode y = (BLeafNode) x.children[i];
        BLeafNode z = new BLeafNode();

        // Right node has t children
        z.nodeSize = order;
        // Move t children to right node
        for (int j = 0; j < order; j++){
            z.entries[j] = y.entries[j + order];
        }

        y.nodeSize = order;
        // Remove reference from left node to prevent future memory leak
        // TODO: This may not be necessary
        for (int j = order; j < (2 * order); j++){
            y.entries[j] = null;
        }

        // Link leaf nodes
        z.nextLeaf = y.nextLeaf;
        z.previousLeaf = y;
        y.nextLeaf = z;

        // Shift parent node
        for (int j = x.nodeSize; j > i; j--){
            x.children[j]     = x.children[j - 1];
            x.routers [j + 1] = x.routers [j];
        }

        x.children[i + 1] = z;
        x.routers [i]     = z.entries[0].hashCode();
        x.nodeSize++;
    }
}
