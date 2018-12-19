package testutils;

import java.util.function.Function;

import org.aion.avm.arraywrapper.BooleanArray;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.arraywrapper.DoubleArray;
import org.aion.avm.arraywrapper.FloatArray;
import org.aion.avm.arraywrapper.IntArray;
import org.aion.avm.arraywrapper.LongArray;
import org.aion.avm.arraywrapper.ObjectArray;
import org.aion.avm.arraywrapper.ShortArray;
import org.aion.avm.internal.IArrayWrapperFactory;
import org.aion.avm.shadow.java.lang.Object;


/**
 * Used when testing the ABIEncoder.  This implementation just returns a normal ObjectArray, not the more specific
 * type.  This makes this factory sufficient when just interacting with the array but not when using it in
 * instrumented code (since the types won't map properly:  both byte[][] and int[][] map to ObjectArray).
 */
public class TestArrayWrapperFactory implements IArrayWrapperFactory {
    @Override
    public ObjectArray construct2DByteArray(byte[][] nativeArray) {
        return createArray(nativeArray.length, (i) -> new ByteArray(nativeArray[i]));
    }
    @Override
    public ObjectArray construct2DBooleanArray(boolean[][] nativeArray) {
        return createArray(nativeArray.length, (i) -> new BooleanArray(nativeArray[i]));
    }
    @Override
    public ObjectArray construct2DCharArray(char[][] nativeArray) {
        return createArray(nativeArray.length, (i) -> new CharArray(nativeArray[i]));
    }
    @Override
    public ObjectArray construct2DShortArray(short[][] nativeArray) {
        return createArray(nativeArray.length, (i) -> new ShortArray(nativeArray[i]));
    }
    @Override
    public ObjectArray construct2DIntArray(int[][] nativeArray) {
        return createArray(nativeArray.length, (i) -> new IntArray(nativeArray[i]));
    }
    @Override
    public ObjectArray construct2DLongArray(long[][] nativeArray) {
        return createArray(nativeArray.length, (i) -> new LongArray(nativeArray[i]));
    }
    @Override
    public ObjectArray construct2DFloatArray(float[][] nativeArray) {
        return createArray(nativeArray.length, (i) -> new FloatArray(nativeArray[i]));
    }
    @Override
    public ObjectArray construct2DDoubleArray(double[][] nativeArray) {
        return createArray(nativeArray.length, (i) -> new DoubleArray(nativeArray[i]));
    }
    @Override
    public ObjectArray construct1DStringArray(org.aion.avm.shadow.java.lang.String[] shadowArray) {
        return createArray(shadowArray.length, (i) -> shadowArray[i]);
    }
    private ObjectArray createArray(int length, Function<Integer, Object> mapper) {
        ObjectArray array = ObjectArray.initArray(length);
        for (int i = 0; i < length; ++i) {
            Object elt = mapper.apply(i);
            array.set(i, elt);
        }
        return array;
    }
}
