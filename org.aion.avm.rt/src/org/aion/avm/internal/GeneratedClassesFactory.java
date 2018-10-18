package org.aion.avm.internal;

import org.aion.avm.arraywrapper.*;

public class GeneratedClassesFactory {
    static ClassLoader cl;

    public static void initializeClassVariable(ClassLoader classLoader) {
        cl = classLoader;
    }

    public static IObject construct2DBooleanArray(boolean[][] data) {
        try {
            Class<?> wrapperClass = cl.loadClass(PackageConstants.kArrayWrapperDotPrefix + "$$Z");
            IObject ret = (IObject) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, new BooleanArray(data[m]));
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public static IObject construct2DByteArray(byte[][] data) {
        try {
            Class<?> wrapperClass = cl.loadClass(PackageConstants.kArrayWrapperDotPrefix + "$$B");
            IObject ret = (IObject) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, new ByteArray(data[m]));
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public static IObject construct2DCharArray(char[][] data) {
        try {
            Class<?> wrapperClass = cl.loadClass(PackageConstants.kArrayWrapperDotPrefix + "$$C");
            IObject ret = (IObject) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, new CharArray(data[m]));
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public static IObject construct2DDoubleArray(double[][] data) {
        try {
            Class<?> wrapperClass = cl.loadClass(PackageConstants.kArrayWrapperDotPrefix + "$$D");
            IObject ret = (IObject) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, new DoubleArray(data[m]));
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public static IObject construct2DFloatArray(float[][] data) {
        try {
            Class<?> wrapperClass = cl.loadClass(PackageConstants.kArrayWrapperDotPrefix + "$$F");
            IObject ret = (IObject) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, new FloatArray(data[m]));
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public static IObject construct2DShortArray(short[][] data) {
        try {
            Class<?> wrapperClass = cl.loadClass(PackageConstants.kArrayWrapperDotPrefix + "$$S");
            IObject ret = (IObject) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, new ShortArray(data[m]));
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public static IObject construct2DIntArray(int[][] data) {
        try {
            Class<?> wrapperClass = cl.loadClass(PackageConstants.kArrayWrapperDotPrefix + "$$I");
            IObject ret = (IObject) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, new IntArray(data[m]));
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public static IObject construct2DLongArray(long[][] data) {
        try {
            Class<?> wrapperClass = cl.loadClass(PackageConstants.kArrayWrapperDotPrefix + "$$J");
            IObject ret = (IObject) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, new LongArray(data[m]));
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public static IObject construct1DStringArray(String[] data) {
        try {
            //Class<?> wrapperClass = cl.loadClass(PackageConstants.kArrayWrapperDotPrefix + "interface._L" + PackageConstants.kShadowDotPrefix + "java.lang.String");
            Class<?> wrapperClass = cl.loadClass(PackageConstants.kArrayWrapperDotPrefix + "$Ljava.lang.String");
            IObject ret = (IObject) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, data[m]);
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public static IObject convert1DStringArray(IObject data) {
        return null;
    }
}
