package org.aion.avm.core.util;

import java.util.HashMap;
import java.util.Map;

import org.aion.avm.api.Address;
import org.aion.avm.arraywrapper.BooleanArray;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.arraywrapper.DoubleArray;
import org.aion.avm.arraywrapper.FloatArray;
import org.aion.avm.arraywrapper.IntArray;
import org.aion.avm.arraywrapper.LongArray;
import org.aion.avm.arraywrapper.ObjectArray;
import org.aion.avm.arraywrapper.ShortArray;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.PackageConstants;


/**
 * This enum provides the basic mapping capabilities required to implement the ABI (called from high-level logic in GeneratedClassesFactory).
 * 
 * Cases which need to be converted between these environments:
 * 1) Box-types (primitive wrappers).
 * 2) String + Address.
 * 3) Primitive arrays.
 * 4) String + Address arrays.
 * 5) 2D primitive arrays.
 */
public enum ShadowTypeBridge {
    // 1) Box-types (primitive wrappers).
    BYTE(Byte.class, org.aion.avm.shadow.java.lang.Byte.class.getName(), org.aion.avm.shadow.java.lang.Byte.class.getName()) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            return Byte.valueOf(((org.aion.avm.shadow.java.lang.Byte)shadowValue).getUnderlying());
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new org.aion.avm.shadow.java.lang.Byte(((Byte)standardValue).byteValue());
        }
    },
    BOOLEAN(Boolean.class, org.aion.avm.shadow.java.lang.Boolean.class.getName(), org.aion.avm.shadow.java.lang.Boolean.class.getName()) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            return Boolean.valueOf(((org.aion.avm.shadow.java.lang.Boolean)shadowValue).getUnderlying());
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new org.aion.avm.shadow.java.lang.Boolean(((Boolean)standardValue).booleanValue());
        }
    },
    SHORT(Short.class, org.aion.avm.shadow.java.lang.Short.class.getName(), org.aion.avm.shadow.java.lang.Short.class.getName()) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            return Short.valueOf(((org.aion.avm.shadow.java.lang.Short)shadowValue).getUnderlying());
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new org.aion.avm.shadow.java.lang.Short(((Short)standardValue).shortValue());
        }
    },
    CHAR(Character.class, org.aion.avm.shadow.java.lang.Character.class.getName(), org.aion.avm.shadow.java.lang.Character.class.getName()) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            return Character.valueOf(((org.aion.avm.shadow.java.lang.Character)shadowValue).getUnderlying());
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new org.aion.avm.shadow.java.lang.Character(((Character)standardValue).charValue());
        }
    },
    INT(Integer.class, org.aion.avm.shadow.java.lang.Integer.class.getName(), org.aion.avm.shadow.java.lang.Integer.class.getName()) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            return Integer.valueOf(((org.aion.avm.shadow.java.lang.Integer)shadowValue).getUnderlying());
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new org.aion.avm.shadow.java.lang.Integer(((Integer)standardValue).intValue());
        }
    },
    FLOAT(Float.class, org.aion.avm.shadow.java.lang.Float.class.getName(), org.aion.avm.shadow.java.lang.Float.class.getName()) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            return Float.valueOf(((org.aion.avm.shadow.java.lang.Float)shadowValue).getUnderlying());
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new org.aion.avm.shadow.java.lang.Float(((Float)standardValue).floatValue());
        }
    },
    LONG(Long.class, org.aion.avm.shadow.java.lang.Long.class.getName(), org.aion.avm.shadow.java.lang.Long.class.getName()) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            return Long.valueOf(((org.aion.avm.shadow.java.lang.Long)shadowValue).getUnderlying());
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new org.aion.avm.shadow.java.lang.Long(((Long)standardValue).longValue());
        }
    },
    DOUBLE(Double.class, org.aion.avm.shadow.java.lang.Double.class.getName(), org.aion.avm.shadow.java.lang.Double.class.getName()) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            return Double.valueOf(((org.aion.avm.shadow.java.lang.Double)shadowValue).getUnderlying());
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new org.aion.avm.shadow.java.lang.Double(((Double)standardValue).doubleValue());
        }
    },
    
    // 2) String + Address.
    STRING(String.class, org.aion.avm.shadow.java.lang.String.class.getName(), org.aion.avm.shadow.java.lang.String.class.getName()) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            return ((org.aion.avm.shadow.java.lang.String)shadowValue).getUnderlying();
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new org.aion.avm.shadow.java.lang.String((String) standardValue);
        }
    },
    ADDRESS(Address.class, Address.class.getName(), Address.class.getName()) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            // This type is the same on both sides.
            return (Address) shadowValue;
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            // This type is the same on both sides.
            return (Address) standardValue;
        }
    },
    
    // 3) Primitive arrays.
    A_BYTE(byte[].class, ByteArray.class.getName(), ByteArray.class.getName()) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            return ((ByteArray)shadowValue).getUnderlying();
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new ByteArray((byte[]) standardValue);
        }
    },
    A_BOOLEAN(boolean[].class, BooleanArray.class.getName(), BooleanArray.class.getName()) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            return ((BooleanArray)shadowValue).getUnderlying();
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new BooleanArray((boolean[]) standardValue);
        }
    },
    A_SHORT(short[].class, ShortArray.class.getName(), ShortArray.class.getName()) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            return ((ShortArray)shadowValue).getUnderlying();
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new ShortArray((short[])standardValue);
        }
    },
    A_CHAR(char[].class, CharArray.class.getName(), CharArray.class.getName()) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            return ((CharArray)shadowValue).getUnderlying();
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new CharArray((char[])standardValue);
        }
    },
    A_INT(int[].class, IntArray.class.getName(), IntArray.class.getName()) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            return ((IntArray)shadowValue).getUnderlying();
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new IntArray((int[])standardValue);
        }
    },
    A_FLOAT(float[].class, FloatArray.class.getName(), FloatArray.class.getName()) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            return ((FloatArray)shadowValue).getUnderlying();
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new FloatArray((float[])standardValue);
        }
    },
    A_LONG(long[].class, LongArray.class.getName(), LongArray.class.getName()) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            return ((LongArray)shadowValue).getUnderlying();
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new LongArray((long[])standardValue);
        }
    },
    A_DOUBLE(double[].class, DoubleArray.class.getName(), DoubleArray.class.getName()) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            return ((DoubleArray)shadowValue).getUnderlying();
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new DoubleArray((double[])standardValue);
        }
    },
    
    // 4) String + Address arrays.
    A_STRING(String[].class
            , PackageConstants.kArrayWrapperDotPrefix + "$L" + org.aion.avm.shadow.java.lang.String.class.getName()
            , PackageConstants.kArrayWrapperDotPrefix + "interface._L" + org.aion.avm.shadow.java.lang.String.class.getName()
    ) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            ObjectArray array = (ObjectArray)shadowValue;
            Object[] underlying = array.getUnderlying();
            String[] converted = new String[underlying.length];
            for (int i = 0; i < underlying.length; ++i) {
                // We also need to convert the nested elements.
                converted[i] = ((org.aion.avm.shadow.java.lang.String)underlying[i]).getUnderlying();
            }
            return converted;
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) throws Exception {
            String[] array = (String[]) standardValue;
            Class<?> wrapperClass = classLoader.loadClass(this.concreteShadowClassName);
            ObjectArray ret = (ObjectArray) wrapperClass.getMethod("initArray", int.class).invoke(null, array.length);
            for (int i = 0; i < array.length; ++i) {
                // Convert these.
                org.aion.avm.shadow.java.lang.String converted = (org.aion.avm.shadow.java.lang.String) STRING.convertToConcreteShadowValue(classLoader, array[i]);
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, i, converted);
            }
            return ret;
            
        }
    },
    A_ADDRESS(Address[].class
            , PackageConstants.kArrayWrapperDotPrefix + "$L" + Address.class.getName()
            , PackageConstants.kArrayWrapperDotPrefix + "interface._L" + Address.class.getName()
    ) {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            ObjectArray array = (ObjectArray)shadowValue;
            Object[] underlying = array.getUnderlying();
            Address[] converted = new Address[underlying.length];
            for (int i = 0; i < underlying.length; ++i) {
                // Nested elements don't need this conversion.
                converted[i] = (Address)underlying[i];
            }
            return converted;
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) throws Exception {
            String[] array = (String[]) standardValue;
            Class<?> wrapperClass = classLoader.loadClass(this.concreteShadowClassName);
            ObjectArray ret = (ObjectArray) wrapperClass.getMethod("initArray", int.class).invoke(null, array.length);
            for (int i = 0; i < array.length; ++i) {
                // Convert these.
                Address converted = (Address) ADDRESS.convertToConcreteShadowValue(classLoader, array[i]);
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, i, converted);
            }
            return ret;
            
        }
    },
    
    // 5) 2D primitive arrays.
    A2_BYTE(byte[][].class, PackageConstants.kArrayWrapperDotPrefix + "$$B", PackageConstants.kArrayWrapperDotPrefix + "$$B") {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            ObjectArray array = (ObjectArray)shadowValue;
            byte[][] target = new byte[array.length()][];
            // Convert sub-elements.
            for (int i = 0; i < target.length; ++i) {
                target[i] = (byte[]) A_BYTE.convertToStandardValue((IObject) array.get(i));
            }
            return target;
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new ByteArray((byte[]) standardValue);
        }
    },
    A2_BOOLEAN(boolean[][].class, PackageConstants.kArrayWrapperDotPrefix + "$$Z", PackageConstants.kArrayWrapperDotPrefix + "$$Z") {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            ObjectArray array = (ObjectArray)shadowValue;
            boolean[][] target = new boolean[array.length()][];
            // Convert sub-elements.
            for (int i = 0; i < target.length; ++i) {
                target[i] = (boolean[]) A_BOOLEAN.convertToStandardValue((IObject) array.get(i));
            }
            return target;
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new BooleanArray((boolean[]) standardValue);
        }
    },
    A2_SHORT(short[][].class, PackageConstants.kArrayWrapperDotPrefix + "$$S", PackageConstants.kArrayWrapperDotPrefix + "$$S") {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            ObjectArray array = (ObjectArray)shadowValue;
            short[][] target = new short[array.length()][];
            // Convert sub-elements.
            for (int i = 0; i < target.length; ++i) {
                target[i] = (short[]) A_SHORT.convertToStandardValue((IObject) array.get(i));
            }
            return target;
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new ShortArray((short[])standardValue);
        }
    },
    A2_CHAR(char[][].class, PackageConstants.kArrayWrapperDotPrefix + "$$C", PackageConstants.kArrayWrapperDotPrefix + "$$C") {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            ObjectArray array = (ObjectArray)shadowValue;
            char[][] target = new char[array.length()][];
            // Convert sub-elements.
            for (int i = 0; i < target.length; ++i) {
                target[i] = (char[]) A_CHAR.convertToStandardValue((IObject) array.get(i));
            }
            return target;
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) throws Exception {
            char[][] array = (char[][]) standardValue;
            Class<?> wrapperClass = classLoader.loadClass(this.concreteShadowClassName);
            ObjectArray ret = (ObjectArray) wrapperClass.getMethod("initArray", int.class).invoke(null, array.length);
            for (int i = 0; i < array.length; ++i) {
                // Convert these.
                CharArray converted = (CharArray) A_CHAR.convertToConcreteShadowValue(classLoader, array[i]);
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, i, converted);
            }
            return ret;
        }
    },
    A2_INT(int[][].class, PackageConstants.kArrayWrapperDotPrefix + "$$I", PackageConstants.kArrayWrapperDotPrefix + "$$I") {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            ObjectArray array = (ObjectArray)shadowValue;
            int[][] target = new int[array.length()][];
            // Convert sub-elements.
            for (int i = 0; i < target.length; ++i) {
                target[i] = (int[]) A_INT.convertToStandardValue((IObject) array.get(i));
            }
            return target;
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) throws Exception {
            int[][] array = (int[][]) standardValue;
            Class<?> wrapperClass = classLoader.loadClass(this.concreteShadowClassName);
            ObjectArray ret = (ObjectArray) wrapperClass.getMethod("initArray", int.class).invoke(null, array.length);
            for (int i = 0; i < array.length; ++i) {
                // Convert these.
                IntArray converted = (IntArray) A_INT.convertToConcreteShadowValue(classLoader, array[i]);
                wrapperClass.getMethod("set", int.class, Object.class).invoke(ret, i, converted);
            }
            return ret;
        }
    },
    A2_FLOAT(float[][].class, PackageConstants.kArrayWrapperDotPrefix + "$$F", PackageConstants.kArrayWrapperDotPrefix + "$$F") {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            ObjectArray array = (ObjectArray)shadowValue;
            float[][] target = new float[array.length()][];
            // Convert sub-elements.
            for (int i = 0; i < target.length; ++i) {
                target[i] = (float[]) A_FLOAT.convertToStandardValue((IObject) array.get(i));
            }
            return target;
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new FloatArray((float[])standardValue);
        }
    },
    A2_LONG(long[][].class, PackageConstants.kArrayWrapperDotPrefix + "$$J", PackageConstants.kArrayWrapperDotPrefix + "$$J") {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            ObjectArray array = (ObjectArray)shadowValue;
            long[][] target = new long[array.length()][];
            // Convert sub-elements.
            for (int i = 0; i < target.length; ++i) {
                target[i] = (long[]) A_LONG.convertToStandardValue((IObject) array.get(i));
            }
            return target;
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new LongArray((long[])standardValue);
        }
    },
    A2_DOUBLE(double[][].class, PackageConstants.kArrayWrapperDotPrefix + "$$D", PackageConstants.kArrayWrapperDotPrefix + "$$D") {
        @Override
        public Object convertToStandardValue(IObject shadowValue) {
            ObjectArray array = (ObjectArray)shadowValue;
            double[][] target = new double[array.length()][];
            // Convert sub-elements.
            for (int i = 0; i < target.length; ++i) {
                target[i] = (double[]) A_DOUBLE.convertToStandardValue((IObject) array.get(i));
            }
            return target;
        }
        @Override
        public IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) {
            return new DoubleArray((double[])standardValue);
        }
    },
    ;

    public static final Map<Class<?>, ShadowTypeBridge> FROM_STANDARD_CLASS;
    public static final Map<String, ShadowTypeBridge> FROM_CONCRETE_SHADOW_CLASS_NAME;
    public static final Map<String, ShadowTypeBridge> FROM_BINDING_SHADOW_CLASS_NAME;
    static {
        FROM_STANDARD_CLASS = new HashMap<>();
        FROM_CONCRETE_SHADOW_CLASS_NAME = new HashMap<>();
        FROM_BINDING_SHADOW_CLASS_NAME = new HashMap<>();
        for (ShadowTypeBridge elt : ShadowTypeBridge.values()) {
            FROM_STANDARD_CLASS.put(elt.standardClass, elt);
            FROM_CONCRETE_SHADOW_CLASS_NAME.put(elt.concreteShadowClassName, elt);
            FROM_BINDING_SHADOW_CLASS_NAME.put(elt.bindingShadowClassName, elt);
        }
    }

    public final Class<?> standardClass;
    public final String concreteShadowClassName;
    public final String bindingShadowClassName;
    
    private ShadowTypeBridge(Class<?> standardClass, String concreteShadowClassName, String bindingShadowClassName) {
        this.standardClass = standardClass;
        this.concreteShadowClassName = concreteShadowClassName;
        this.bindingShadowClassName = bindingShadowClassName;
    }
    
    public abstract Object convertToStandardValue(IObject shadowValue);
    
    public abstract IObject convertToConcreteShadowValue(ClassLoader classLoader, Object standardValue) throws Exception;
}
