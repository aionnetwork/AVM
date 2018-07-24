package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.RuntimeAssertionError;

import java.util.Arrays;
import java.util.function.Consumer;


public class CharArray extends Array {

    private char[] underlying;

    public static CharArray initArray(int c){
        //IHelper.currentContractHelper.get().externalChargeEnergy(c * 16);
        return new CharArray(c);
    }

    public CharArray(int c) {
        this.underlying = new char[c];
    }

    // Deserializer support.
    public CharArray(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(CharArray.class, deserializer);
        
        // TODO:  We probably want faster array copies.
        int length = deserializer.readInt();
        this.underlying = new char[length];
        for (int i = 0; i < length; ++i) {
            this.underlying[i] = deserializer.readChar();
        }
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        super.serializeSelf(CharArray.class, serializer, nextObjectQueue);
        
        // TODO:  We probably want faster array copies.
        serializer.writeInt(this.underlying.length);
        for (int i = 0; i < this.underlying.length; ++i) {
            serializer.writeChar(this.underlying[i]);
        }
    }

    public int length() {
        return this.underlying.length;
    }

    public char get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, char val) {
        this.underlying[idx] = val;
    }

    public IObject avm_clone() {
        return new CharArray(Arrays.copyOf(underlying, underlying.length));
    }

    public IObject clone() {
        return new CharArray(Arrays.copyOf(underlying, underlying.length));
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public CharArray(char[] underlying) {
        RuntimeAssertionError.assertTrue(null != underlying);
        this.underlying = underlying;
    }

    public char[] getUnderlying() {
        return underlying;
    }

    public void setUnderlyingAsObject(java.lang.Object u){
        RuntimeAssertionError.assertTrue(null != u);
        this.underlying = (char[]) u;
    }

    public java.lang.Object getUnderlyingAsObject(){
        return underlying;
    }

    public java.lang.Object getAsObject(int idx){
        lazyLoad();
        return this.underlying[idx];
    }
}
