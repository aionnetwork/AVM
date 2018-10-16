package org.aion.avm.internal;

import org.aion.avm.arraywrapper.*;

public class GeneratedClassesFactory {
    static ClassLoader cl;

    public static void initializeClassVariable(ClassLoader classLoader) {
        cl = classLoader;
    }

    public static IObject construct2DBooleanArray(boolean[][] data) {
        try {
            Class<?> wrapperClass = cl.loadClass((PackageConstants.kArrayWrapperSlashPrefix + "$$Z").replace('/', '.'));
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
            Class<?> wrapperClass = cl.loadClass((PackageConstants.kArrayWrapperSlashPrefix + "$$B").replace('/', '.'));
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
            Class<?> wrapperClass = cl.loadClass((PackageConstants.kArrayWrapperSlashPrefix + "$$C").replace('/', '.'));
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
            Class<?> wrapperClass = cl.loadClass((PackageConstants.kArrayWrapperSlashPrefix + "$$D").replace('/', '.'));
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
            Class<?> wrapperClass = cl.loadClass((PackageConstants.kArrayWrapperSlashPrefix + "$$F").replace('/', '.'));
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
            Class<?> wrapperClass = cl.loadClass((PackageConstants.kArrayWrapperSlashPrefix + "$$S").replace('/', '.'));
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
            Class<?> wrapperClass = cl.loadClass((PackageConstants.kArrayWrapperSlashPrefix + "$$I").replace('/', '.'));
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
            Class<?> wrapperClass = cl.loadClass((PackageConstants.kArrayWrapperSlashPrefix + "$$J").replace('/', '.'));
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
            Class<?> wrapperClass = cl.loadClass((PackageConstants.kArrayWrapperSlashPrefix + "$Ljava.lang.String").replace('/', '.'));
            IObject ret = (IObject) wrapperClass.getMethod("initArray", int.class).invoke(null, data.length);
            for (int m = 0; m < data.length; m ++) {
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, m, data[m]);
            }
            return ret;
        } catch (Throwable e) {
            throw RuntimeAssertionError.unexpected(e);
        }
    }
}
