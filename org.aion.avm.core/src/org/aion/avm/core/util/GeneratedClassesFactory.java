package org.aion.avm.core.util;

import org.aion.avm.arraywrapper.BooleanArray;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.arraywrapper.DoubleArray;
import org.aion.avm.arraywrapper.FloatArray;
import org.aion.avm.arraywrapper.IntArray;
import org.aion.avm.arraywrapper.LongArray;
import org.aion.avm.arraywrapper.ObjectArray;
import org.aion.avm.arraywrapper.ShortArray;
import org.aion.avm.core.classloading.AvmSharedClassLoader;
import org.aion.avm.internal.IArrayWrapperFactory;
import org.aion.avm.internal.PackageConstants;
import org.aion.avm.internal.RuntimeAssertionError;


/**
 * Provides generated array instantiation for the ABIEncoder, in the runtime package.
 * Note that this knows about the name/shape of generated array classes so we might want to fold this
 * support into a class which already has those assumptions.
 */
public class GeneratedClassesFactory implements IArrayWrapperFactory {
    private final AvmSharedClassLoader classLoader;

    public GeneratedClassesFactory(AvmSharedClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public ObjectArray construct2DBooleanArray(boolean[][] data) {
        try {
            Class<?> wrapperClass = this.classLoader.loadClass(PackageConstants.kArrayWrapperDotPrefix + "$$Z");
            ObjectArray ret = (ObjectArray) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, new BooleanArray(data[m]));
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public ObjectArray construct2DByteArray(byte[][] data) {
        try {
            Class<?> wrapperClass = this.classLoader.loadClass(PackageConstants.kArrayWrapperDotPrefix + "$$B");
            ObjectArray ret = (ObjectArray) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, new ByteArray(data[m]));
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public ObjectArray construct2DCharArray(char[][] data) {
        try {
            Class<?> wrapperClass = this.classLoader.loadClass(PackageConstants.kArrayWrapperDotPrefix + "$$C");
            ObjectArray ret = (ObjectArray) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, new CharArray(data[m]));
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public ObjectArray construct2DDoubleArray(double[][] data) {
        try {
            Class<?> wrapperClass = this.classLoader.loadClass(PackageConstants.kArrayWrapperDotPrefix + "$$D");
            ObjectArray ret = (ObjectArray) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, new DoubleArray(data[m]));
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public ObjectArray construct2DFloatArray(float[][] data) {
        try {
            Class<?> wrapperClass = this.classLoader.loadClass(PackageConstants.kArrayWrapperDotPrefix + "$$F");
            ObjectArray ret = (ObjectArray) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, new FloatArray(data[m]));
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public ObjectArray construct2DShortArray(short[][] data) {
        try {
            Class<?> wrapperClass = this.classLoader.loadClass(PackageConstants.kArrayWrapperDotPrefix + "$$S");
            ObjectArray ret = (ObjectArray) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, new ShortArray(data[m]));
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public ObjectArray construct2DIntArray(int[][] data) {
        try {
            Class<?> wrapperClass = this.classLoader.loadClass(PackageConstants.kArrayWrapperDotPrefix + "$$I");
            ObjectArray ret = (ObjectArray) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, new IntArray(data[m]));
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public ObjectArray construct2DLongArray(long[][] data) {
        try {
            Class<?> wrapperClass = this.classLoader.loadClass(PackageConstants.kArrayWrapperDotPrefix + "$$J");
            ObjectArray ret = (ObjectArray) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, new LongArray(data[m]));
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public ObjectArray construct1DStringArray(org.aion.avm.shadow.java.lang.String[] data) {
        try {
            Class<?> wrapperClass = this.classLoader.loadClass(PackageConstants.kArrayWrapperDotPrefix + "$L" + PackageConstants.kShadowDotPrefix + "java.lang.String");
            ObjectArray ret = (ObjectArray) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, data[m]);
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }
}
