package org.aion.avm.core.arraywrapping;

/**
 * Tests on array type rules as well as some inherited Object functionality such as hashCode, clone
 * and toString as well as array's length function.
 *
 * We test on directly assigning references and getting references from method calls and on forcing
 * checkcasts to cover each of these cases.
 */
public class ArrayTypeContract {

    // Test primitives
    
    public void testPrimitiveArraysCastToObject() {
    	Object object = new byte[] {};
    	invokeObjectOperations(object);
    	object = new boolean[] {};
    	invokeObjectOperations(object);
    	object = new char[] {};
    	invokeObjectOperations(object);
    	object = new short[] {};
    	invokeObjectOperations(object);
    	object = new int[] {};
    	invokeObjectOperations(object);
    	object = new float[] {};
    	invokeObjectOperations(object);
    	object = new double[] {};
    	invokeObjectOperations(object);
    	object = new long[] {};
    	invokeObjectOperations(object);
    }
    
    public int test2DprimitiveArraysCastToObjectAndObjectArray() {
    	int magic = 0;
    	
    	Object object = new byte[][] {};
    	invokeObjectOperations(object);
    	Object[] objectArray = new byte[][] {};
    	invokeArrayOperations(objectArray);
    	object = new boolean[][] {};
    	invokeObjectOperations(object);
    	objectArray = new boolean[][] {};
    	invokeArrayOperations(objectArray);
    	object = new char[][] {};
    	invokeObjectOperations(object);
    	objectArray = new char[][] {};
    	invokeArrayOperations(objectArray);
    	object = new short[][] {};
    	invokeObjectOperations(object);
    	objectArray = new short[][] {};
    	invokeArrayOperations(objectArray);
    	object = new int[][] {};
    	invokeObjectOperations(object);
    	objectArray = new int[][] {};
    	invokeArrayOperations(objectArray);
    	object = new float[][] {};
    	invokeObjectOperations(object);
    	objectArray = new float[][] {};
    	invokeArrayOperations(objectArray);
    	object = new double[][] {};
    	invokeObjectOperations(object);
    	objectArray = new double[][] {};
    	invokeArrayOperations(objectArray);
    	object = new long[][] {};
    	invokeObjectOperations(object);
    	objectArray = new long[][] {};
    	invokeArrayOperations(objectArray);
    	
    	return magic;
    }
    
    public int test3DprimitiveArraysCastToObjectAndObjectArray() {
    	int magic = 0;
    	
    	Object object = new byte[][][] {};
    	invokeObjectOperations(object);
    	Object[][] objectArray = new byte[][][] {};
    	invokeArrayOperations(objectArray);
    	object = new boolean[][][] {};
    	invokeObjectOperations(object);
    	objectArray = new boolean[][][] {};
    	invokeArrayOperations(objectArray);
    	object = new char[][][] {};
    	invokeObjectOperations(object);
    	objectArray = new char[][][] {};
    	invokeArrayOperations(objectArray);
    	object = new short[][][] {};
    	invokeObjectOperations(object);
    	objectArray = new short[][][] {};
    	invokeArrayOperations(objectArray);
    	object = new int[][][] {};
    	invokeObjectOperations(object);
    	objectArray = new int[][][] {};
    	invokeArrayOperations(objectArray);
    	object = new float[][][] {};
    	invokeObjectOperations(object);
    	objectArray = new float[][][] {};
    	invokeArrayOperations(objectArray);
    	object = new double[][][] {};
    	invokeObjectOperations(object);
    	objectArray = new double[][][] {};
    	invokeArrayOperations(objectArray);
    	object = new long[][][] {};
    	invokeObjectOperations(object);
    	objectArray = new long[][][] {};
    	invokeArrayOperations(objectArray);
    	
    	return magic;
    }
    
    public int testPrimitiveArraysCanClone() {
    	byte[] bytes = new byte[5];
    	boolean[] booleans = new boolean[7];
    	char[] chars = new char[] { 'a', 'b' };
    	short[] shorts = new short[3];
    	int[] ints = new int[0];
    	float[] floats = new float[1];
    	double[] doubles = new double[8];
    	long[] longs = new long[] { 4, 12, 19 };
    	
    	return (bytes.equals(bytes.clone()) ? 1 : 0) 
    		+ (booleans.equals(booleans.clone()) ? 1 : 0)
    		+ (chars.equals(chars.clone()) ? 1 : 0) 
    		+ (shorts.equals(shorts.clone()) ? 1 : 0)
    		+ (ints.equals(ints.clone()) ? 1 : 0) 
    		+ (floats.equals(floats.clone()) ? 1 : 0)
    		+ (doubles.equals(doubles.clone()) ? 1 : 0) 
    		+ (longs.equals(longs.clone()) ? 1 : 0);
    }

    // Test Object behaviour

    public int testObjectHashCode() {
        return new Object().hashCode() + getObject().hashCode();
    }

    public String testObjectToString() {
    	Object newObject = new Object();
    	Object gotObject = getObject();
    	Object subClass = new SubClass();
    	SuperestInterface superClass = new SuperClass();
    	
    	String newObjectString = (newObject.toString() == null) ? "null" : newObject.toString();
    	String gotObjectString = (gotObject.toString() == null) ? "null" : gotObject.toString();
    	String subString = subClass.toString();
    	String superString = superClass.toString();
        return newObjectString + gotObjectString + subString + superString;
    }

    public boolean testObjectEquals1() {
        return new Object().equals(new Object());
    }

    public boolean testObjectEquals2() {
        Object object = getObject();
        return object.equals(object);
    }

    public void testMdObjectArraysUnifyToObject() {
        Object object = new Object[]{};
        invokeObjectOperations(object);
        object = getObjectArray1D();
        invokeObjectOperations(object);
        object = (Object[]) getObjectArray1D();
        invokeObjectOperations(object);
        object = (Object) new Object[]{};
        invokeObjectOperations(object);

        object = new Object[5][];
        invokeObjectOperations(object);
        object = getObjectArray2D();
        invokeObjectOperations(object);
        object = (Object[]) new Object[8][];
        invokeObjectOperations(object);
        object = (Object) new Object[8][];
        invokeObjectOperations(object);

        object = new Object[10][][];
        invokeObjectOperations(object);
        object = getObjectArray3D();
        invokeObjectOperations(object);
        object = (Object[]) new Object[10][][];
        invokeObjectOperations(object);
        object = (Object) new Object[10][][];
        invokeObjectOperations(object);
    }

    public int testMdObjectArraysUnifyTo1DObjectArray() {
        int magic = 0;

        Object[] object = new Object[]{};
        magic += invokeArrayOperations(object);
        object = getObjectArray1D();
        magic += invokeArrayOperations(object);
        object = (Object[]) getObjectArray1D();
        magic += invokeArrayOperations(object);

        object = new Object[5][];
        magic += invokeArrayOperations(object);
        object = getObjectArray2D();
        magic += invokeArrayOperations(object);
        object = (Object[]) new Object[8][];
        magic += invokeArrayOperations(object);

        object = new Object[10][][];
        magic += invokeArrayOperations(object);
        object = getObjectArray3D();
        magic += invokeArrayOperations(object);
        object = (Object[]) new Object[10][][];
        magic += invokeArrayOperations(object);

        return magic;
    }

    // Test 1D object behaviour
    
    public void testSubClassIsSuperestInterface() {
    	SuperestInterface[] superestInterface = new SubClass[] {};
    }
    
    public void testSubClassIsSuperInterface() {
    	SuperInterface[] superInterface = new SubClass[] {};
    }
    
    public void testSubClassIsSuperAbstract() {
    	SuperAbstract[] superAbstract = new SubClass[] {};
    }
    
    public int test1DobjectArrayUnificationToSupersWithCasts() {
    	int magic = 0;

        SuperestInterface[] superestInterfaces = (SuperestInterface[]) new SuperestInterface[]{};
        magic += invokeArrayOperations(superestInterfaces);
        SuperInterface[] superInterfaces = (SuperInterface[]) new SuperInterface[]{};
        magic += invokeArrayOperations(superInterfaces);
        SuperAbstract[] superAbstracts = (SuperAbstract[]) new SuperAbstract[5];
        magic += invokeArrayOperations(superAbstracts);
        SuperClass[] superClasses = (SuperClass[]) new SuperClass[]{};
        magic += invokeArrayOperations(superClasses);
        SubClass[] subClasses = (SubClass[]) new SubClass[3];
        magic += invokeArrayOperations(subClasses);

        superestInterfaces = (SuperestInterface[]) new SuperestInterface[2];
        magic += invokeArrayOperations(superestInterfaces);
        superestInterfaces = (SuperestInterface[]) superInterfaces;
        magic += invokeArrayOperations(superestInterfaces);
        superestInterfaces = (SuperestInterface[]) superAbstracts;
        magic += invokeArrayOperations(superestInterfaces);
        superestInterfaces = (SuperestInterface[]) superClasses;
        magic += invokeArrayOperations(superestInterfaces);
        superestInterfaces = (SuperestInterface[]) subClasses;
        magic += invokeArrayOperations(superestInterfaces);

        superInterfaces = (SuperInterface[]) new SuperInterface[9];
        magic += invokeArrayOperations(superInterfaces);
        superInterfaces = (SuperInterface[]) superAbstracts;
        magic += invokeArrayOperations(superInterfaces);
        superInterfaces = (SuperInterface[]) superClasses;
        magic += invokeArrayOperations(superInterfaces);
        superInterfaces = (SuperInterface[]) subClasses;
        magic += invokeArrayOperations(superInterfaces);

        superAbstracts = (SuperAbstract[]) new SuperAbstract[4];
        magic += invokeArrayOperations(superAbstracts);
        superAbstracts = (SuperAbstract[]) superClasses;
        magic += invokeArrayOperations(superAbstracts);
        superAbstracts = (SuperAbstract[]) subClasses;
        magic += invokeArrayOperations(superAbstracts);

        superClasses = (SuperClass[]) new SuperClass[1];
        magic += invokeArrayOperations(superClasses);
        superClasses = (SuperClass[]) subClasses;
        magic += invokeArrayOperations(superClasses);

        subClasses = (SubClass[]) new SubClass[8];
        magic += invokeArrayOperations(subClasses);

        return magic;
    }
    
    public int test1DobjectArrayUnificationToSupersFromMethods() {
    	int magic = 0;

        SuperestInterface[] superestInterfaces = self(new SuperestInterface[]{});
        magic += invokeArrayOperations(superestInterfaces);
        SuperInterface[] superInterfaces = self(new SuperInterface[]{});
        magic += invokeArrayOperations(superInterfaces);
        SuperAbstract[] superAbstracts = self(new SuperAbstract[5]);
        magic += invokeArrayOperations(superAbstracts);
        SuperClass[] superClasses = self(new SuperClass[]{});
        magic += invokeArrayOperations(superClasses);
        SubClass[] subClasses = self(new SubClass[3]);
        magic += invokeArrayOperations(subClasses);

        superestInterfaces = self(new SuperestInterface[2]);
        magic += invokeArrayOperations(superestInterfaces);
        superestInterfaces = self(superInterfaces);
        magic += invokeArrayOperations(superestInterfaces);
        superestInterfaces = self(superAbstracts);
        magic += invokeArrayOperations(superestInterfaces);
        superestInterfaces = self(superClasses);
        magic += invokeArrayOperations(superestInterfaces);
        superestInterfaces = self(subClasses);
        magic += invokeArrayOperations(superestInterfaces);

        superInterfaces = self(new SuperInterface[9]);
        magic += invokeArrayOperations(superInterfaces);
        superInterfaces = self(superAbstracts);
        magic += invokeArrayOperations(superInterfaces);
        superInterfaces = self(superClasses);
        magic += invokeArrayOperations(superInterfaces);
        superInterfaces = self(subClasses);
        magic += invokeArrayOperations(superInterfaces);

        superAbstracts = self(new SuperAbstract[4]);
        magic += invokeArrayOperations(superAbstracts);
        superAbstracts = self(superClasses);
        magic += invokeArrayOperations(superAbstracts);
        superAbstracts = self(subClasses);
        magic += invokeArrayOperations(superAbstracts);

        superClasses = self(new SuperClass[1]);
        magic += invokeArrayOperations(superClasses);
        superClasses = self(subClasses);
        magic += invokeArrayOperations(superClasses);

        subClasses = self(new SubClass[8]);
        magic += invokeArrayOperations(subClasses);

        return magic;
    }

    public int test1DobjectArrayUnificationToSupers() {
        int magic = 0;

        SuperestInterface[] superestInterfaces = new SuperestInterface[]{};
        magic += invokeArrayOperations(superestInterfaces);
        SuperInterface[] superInterfaces = new SuperInterface[]{};
        magic += invokeArrayOperations(superInterfaces);
        SuperAbstract[] superAbstracts = new SuperAbstract[5];
        magic += invokeArrayOperations(superAbstracts);
        SuperClass[] superClasses = new SuperClass[]{};
        magic += invokeArrayOperations(superClasses);
        SubClass[] subClasses = new SubClass[3];
        magic += invokeArrayOperations(subClasses);

        superestInterfaces = new SuperestInterface[2];
        magic += invokeArrayOperations(superestInterfaces);
        superestInterfaces = superInterfaces;
        magic += invokeArrayOperations(superestInterfaces);
        superestInterfaces = superAbstracts;
        magic += invokeArrayOperations(superestInterfaces);
        superestInterfaces = superClasses;
        magic += invokeArrayOperations(superestInterfaces);
        superestInterfaces = subClasses;
        magic += invokeArrayOperations(superestInterfaces);

        superInterfaces = new SuperInterface[9];
        magic += invokeArrayOperations(superInterfaces);
        superInterfaces = superAbstracts;
        magic += invokeArrayOperations(superInterfaces);
        superInterfaces = superClasses;
        magic += invokeArrayOperations(superInterfaces);
        superInterfaces = subClasses;
        magic += invokeArrayOperations(superInterfaces);

        superAbstracts = new SuperAbstract[4];
        magic += invokeArrayOperations(superAbstracts);
        superAbstracts = superClasses;
        magic += invokeArrayOperations(superAbstracts);
        superAbstracts = subClasses;
        magic += invokeArrayOperations(superAbstracts);

        superClasses = new SuperClass[1];
        magic += invokeArrayOperations(superClasses);
        superClasses = subClasses;
        magic += invokeArrayOperations(superClasses);

        subClasses = new SubClass[8];
        magic += invokeArrayOperations(subClasses);

        return magic;
    }

    public void test1DobjectArrayUnificationToObjectArray() {
        Object[] object = new SuperestInterface[] {};
        invokeObjectOperations(object);
        object = self(new SuperestInterface[2]);
        invokeObjectOperations(object);
        object = (Object[]) new SuperestInterface[] {};
        invokeObjectOperations(object);
        
        object = new SuperInterface[] {};
        invokeObjectOperations(object);
        object = self(new SuperInterface[1]);
        invokeObjectOperations(object);
        object = (Object[]) new SuperInterface[7];
        invokeObjectOperations(object);
        
        object = new SuperAbstract[3];
        invokeObjectOperations(object);
        object = self(new SuperAbstract[] {});
        invokeObjectOperations(object);
        object = (Object[]) self(new SuperAbstract[1]);
        invokeObjectOperations(object);
        
        object = new SuperClass[8];
        invokeObjectOperations(object);
        object = self(new SuperClass[] {});
        invokeObjectOperations(object);
        object = (Object[]) self(new SuperClass[8]);
        invokeObjectOperations(object);
        
        object = new SubClass[] {};
        invokeObjectOperations(object);
        object = self(new SubClass[] {});
        invokeObjectOperations(object);
        object = (Object[]) new SubClass[] {};
        invokeObjectOperations(object);
    }

    public void test1DobjectArrayUnificationToObject() {
    	Object object = new SuperestInterface[] {};
        invokeObjectOperations(object);
        object = self(new SuperestInterface[2]);
        invokeObjectOperations(object);
        object = (Object) new SuperestInterface[] {};
        invokeObjectOperations(object);
        
        object = new SuperInterface[] {};
        invokeObjectOperations(object);
        object = self(new SuperInterface[1]);
        invokeObjectOperations(object);
        object = (Object) new SuperInterface[7];
        invokeObjectOperations(object);
        
        object = new SuperAbstract[3];
        invokeObjectOperations(object);
        object = self(new SuperAbstract[] {});
        invokeObjectOperations(object);
        object = (Object) self(new SuperAbstract[1]);
        invokeObjectOperations(object);
        
        object = new SuperClass[8];
        invokeObjectOperations(object);
        object = self(new SuperClass[] {});
        invokeObjectOperations(object);
        object = (Object) self(new SuperClass[8]);
        invokeObjectOperations(object);
        
        object = new SubClass[] {};
        invokeObjectOperations(object);
        object = self(new SubClass[] {});
        invokeObjectOperations(object);
        object = (Object) new SubClass[] {};
        invokeObjectOperations(object);
    }

    // Test multi-dimensional object behaviour. Test names are a little awkward here
    // because of the ambivalence between Object and object.
    
    public int testTypeRulesOnArraysWithSameDimensionality1() {
    	int magic = 0;
    	
    	SuperestInterface[][] superestInterfaces2D = new SuperInterface[][] {};
    	magic += invokeArrayOperations(superestInterfaces2D);
    	superestInterfaces2D = self(superestInterfaces2D);
    	magic += invokeArrayOperations(superestInterfaces2D);
    	superestInterfaces2D = (SuperestInterface[][]) new SuperInterface[][] {};
    	magic += invokeArrayOperations(superestInterfaces2D);
    	superestInterfaces2D = new SuperAbstract[][] {};
    	magic += invokeArrayOperations(superestInterfaces2D);
    	superestInterfaces2D = self(new SuperAbstract[][] {});
    	magic += invokeArrayOperations(superestInterfaces2D);
    	superestInterfaces2D = (SuperestInterface[][]) new SuperAbstract[][] {};
    	magic += invokeArrayOperations(superestInterfaces2D);
    	superestInterfaces2D = new SuperClass[][] {};
    	magic += invokeArrayOperations(superestInterfaces2D);
    	superestInterfaces2D = self(new SuperClass[][] {});
    	magic += invokeArrayOperations(superestInterfaces2D);
    	superestInterfaces2D = (SuperestInterface[][]) new SuperClass[][] {};
    	magic += invokeArrayOperations(superestInterfaces2D);
    	superestInterfaces2D = new SubClass[][] {};
    	magic += invokeArrayOperations(superestInterfaces2D);
    	superestInterfaces2D = self(new SubClass[][] {});
    	magic += invokeArrayOperations(superestInterfaces2D);
    	superestInterfaces2D = (SuperestInterface[][]) new SubClass[][] {};
    	magic += invokeArrayOperations(superestInterfaces2D);
    	
    	SuperInterface[][] superInterfaces2D = new SuperAbstract[][] {};
    	magic += invokeArrayOperations(superInterfaces2D);
    	superInterfaces2D = self(new SuperAbstract[][] {});
    	magic += invokeArrayOperations(superInterfaces2D);
    	superInterfaces2D = (SuperInterface[][]) new SuperAbstract[][] {};
    	magic += invokeArrayOperations(superInterfaces2D);
    	superInterfaces2D = new SuperClass[][] {};
    	magic += invokeArrayOperations(superInterfaces2D);
    	superInterfaces2D = self(new SuperClass[][] {});
    	magic += invokeArrayOperations(superInterfaces2D);
    	superInterfaces2D = (SuperInterface[][]) new SuperClass[][] {};
    	magic += invokeArrayOperations(superInterfaces2D);
    	superInterfaces2D = new SubClass[][] {};
    	magic += invokeArrayOperations(superInterfaces2D);
    	superInterfaces2D = self(new SubClass[][] {});
    	magic += invokeArrayOperations(superInterfaces2D);
    	superInterfaces2D = (SuperInterface[][]) new SubClass[][] {};
    	magic += invokeArrayOperations(superInterfaces2D);
    	
    	SuperAbstract[][] superAbstract2D = new SuperClass[][] {};
    	magic += invokeArrayOperations(superAbstract2D);
    	superAbstract2D = self(new SuperClass[][] {});
    	magic += invokeArrayOperations(superAbstract2D);
    	superAbstract2D = (SuperAbstract[][]) new SuperClass[][] {};
    	magic += invokeArrayOperations(superAbstract2D);
    	superAbstract2D = new SubClass[][] {};
    	magic += invokeArrayOperations(superAbstract2D);
    	superAbstract2D = self(new SubClass[][] {});
    	magic += invokeArrayOperations(superAbstract2D);
    	superAbstract2D = (SuperAbstract[][]) new SubClass[][] {};
    	magic += invokeArrayOperations(superAbstract2D);
    	
    	SuperClass[][] superClass2D = new SubClass[][] {};
    	magic += invokeArrayOperations(superClass2D);
    	superClass2D = self(new SubClass[][] {});
    	magic += invokeArrayOperations(superClass2D);
    	superClass2D = (SuperClass[][]) new SubClass[][] {};
    	magic += invokeArrayOperations(superClass2D);
    	
    	return magic;
    }
    
    public int testTypeRulesOnArraysWithSameDimensionality2() {
    	int magic = 0;
    	
    	SuperestInterface[][][] superestInterfaces3D = new SuperInterface[][][] {};
    	magic += invokeArrayOperations(superestInterfaces3D);
    	superestInterfaces3D = self(superestInterfaces3D);
    	magic += invokeArrayOperations(superestInterfaces3D);
    	superestInterfaces3D = (SuperestInterface[][][]) new SuperInterface[][][] {};
    	magic += invokeArrayOperations(superestInterfaces3D);
    	superestInterfaces3D = new SuperAbstract[][][] {};
    	magic += invokeArrayOperations(superestInterfaces3D);
    	superestInterfaces3D = self(new SuperAbstract[][][] {});
    	magic += invokeArrayOperations(superestInterfaces3D);
    	superestInterfaces3D = (SuperestInterface[][][]) new SuperAbstract[][][] {};
    	magic += invokeArrayOperations(superestInterfaces3D);
    	superestInterfaces3D = new SuperClass[][][] {};
    	magic += invokeArrayOperations(superestInterfaces3D);
    	superestInterfaces3D = self(new SuperClass[][][] {});
    	magic += invokeArrayOperations(superestInterfaces3D);
    	superestInterfaces3D = (SuperestInterface[][][]) new SuperClass[][][] {};
    	magic += invokeArrayOperations(superestInterfaces3D);
    	superestInterfaces3D = new SubClass[][][] {};
    	magic += invokeArrayOperations(superestInterfaces3D);
    	superestInterfaces3D = self(new SubClass[][][] {});
    	magic += invokeArrayOperations(superestInterfaces3D);
    	superestInterfaces3D = (SuperestInterface[][][]) new SubClass[][][] {};
    	magic += invokeArrayOperations(superestInterfaces3D);
    	
    	SuperInterface[][][] superInterfaces3D = new SuperAbstract[][][] {};
    	magic += invokeArrayOperations(superInterfaces3D);
    	superInterfaces3D = self(new SuperAbstract[][][] {});
    	magic += invokeArrayOperations(superInterfaces3D);
    	superInterfaces3D = (SuperInterface[][][]) new SuperAbstract[][][] {};
    	magic += invokeArrayOperations(superInterfaces3D);
    	superInterfaces3D = new SuperClass[][][] {};
    	magic += invokeArrayOperations(superInterfaces3D);
    	superInterfaces3D = self(new SuperClass[][][] {});
    	magic += invokeArrayOperations(superInterfaces3D);
    	superInterfaces3D = (SuperInterface[][][]) new SuperClass[][][] {};
    	magic += invokeArrayOperations(superInterfaces3D);
    	superInterfaces3D = new SubClass[][][] {};
    	magic += invokeArrayOperations(superInterfaces3D);
    	superInterfaces3D = self(new SubClass[][][] {});
    	magic += invokeArrayOperations(superInterfaces3D);
    	superInterfaces3D = (SuperInterface[][][]) new SubClass[][][] {};
    	magic += invokeArrayOperations(superInterfaces3D);
    	
    	SuperAbstract[][][] superAbstract3D = new SuperClass[][][] {};
    	magic += invokeArrayOperations(superAbstract3D);
    	superAbstract3D = self(new SuperClass[][][] {});
    	magic += invokeArrayOperations(superAbstract3D);
    	superAbstract3D = (SuperAbstract[][][]) new SuperClass[][][] {};
    	magic += invokeArrayOperations(superAbstract3D);
    	superAbstract3D = new SubClass[][][] {};
    	magic += invokeArrayOperations(superAbstract3D);
    	superAbstract3D = self(new SubClass[][][] {});
    	magic += invokeArrayOperations(superAbstract3D);
    	superAbstract3D = (SuperAbstract[][][]) new SubClass[][][] {};
    	magic += invokeArrayOperations(superAbstract3D);
    	
    	SuperClass[][][] superClass3D = new SubClass[][][] {};
    	magic += invokeArrayOperations(superClass3D);
    	superClass3D = self(new SubClass[][][] {});
    	magic += invokeArrayOperations(superClass3D);
    	superClass3D = (SuperClass[][][]) new SubClass[][][] {};
    	magic += invokeArrayOperations(superClass3D);
    	
    	return magic;
    }
    
    public int testMultiDimensionalArraysCastToLowerDimensionalObjectArray1() {
    	int magic = 0;
    	
    	Object[] objectArray = new SuperInterface[][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = self(new SuperestInterface[][] {});
    	magic += invokeArrayOperations(objectArray);
    	objectArray = (Object[]) new SuperInterface[][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = new SuperAbstract[][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = self(new SuperAbstract[][] {});
    	magic += invokeArrayOperations(objectArray);
    	objectArray = (Object[]) new SuperAbstract[][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = new SuperClass[][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = self(new SuperClass[][] {});
    	magic += invokeArrayOperations(objectArray);
    	objectArray = (Object[]) new SuperClass[][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = new SubClass[][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = self(new SubClass[][] {});
    	magic += invokeArrayOperations(objectArray);
    	objectArray = (Object[]) new SubClass[][] {};
    	magic += invokeArrayOperations(objectArray);
    	
    	objectArray = new SuperAbstract[][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = self(new SuperAbstract[][] {});
    	magic += invokeArrayOperations(objectArray);
    	objectArray = (Object[]) new SuperAbstract[][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = new SuperClass[][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = self(new SuperClass[][] {});
    	magic += invokeArrayOperations(objectArray);
    	objectArray = (Object[]) new SuperClass[][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = new SubClass[][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = self(new SubClass[][] {});
    	magic += invokeArrayOperations(objectArray);
    	objectArray = (Object[]) new SubClass[][] {};
    	magic += invokeArrayOperations(objectArray);
    	
    	objectArray = new SuperClass[][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = self(new SuperClass[][] {});
    	magic += invokeArrayOperations(objectArray);
    	objectArray = (Object[]) new SuperClass[][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = new SubClass[][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = self(new SubClass[][] {});
    	magic += invokeArrayOperations(objectArray);
    	objectArray = (Object[]) new SubClass[][] {};
    	magic += invokeArrayOperations(objectArray);
    	
    	objectArray = new SubClass[][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = self(new SubClass[][] {});
    	magic += invokeArrayOperations(objectArray);
    	objectArray = (Object[]) new SubClass[][] {};
    	magic += invokeArrayOperations(objectArray);
    	
    	return magic;
    }
    
    public int testMultiDimensionalArraysCastToLowerDimensionalObjectArray2() {
    	int magic = 0;
    	
    	Object[][] objectArray = new SuperInterface[][][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = self(new SuperestInterface[][][] {});
    	magic += invokeArrayOperations(objectArray);
    	objectArray = (Object[][]) new SuperInterface[][][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = new SuperAbstract[][][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = self(new SuperAbstract[][][] {});
    	magic += invokeArrayOperations(objectArray);
    	objectArray = (Object[][]) new SuperAbstract[][][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = new SuperClass[][][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = self(new SuperClass[][][] {});
    	magic += invokeArrayOperations(objectArray);
    	objectArray = (Object[][]) new SuperClass[][][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = new SubClass[][][] {};
    	magic += invokeArrayOperations(objectArray);
    	objectArray = self(new SubClass[][][] {});
    	magic += invokeArrayOperations(objectArray);
    	objectArray = (Object[][]) new SubClass[][][] {};
    	magic += invokeArrayOperations(objectArray);
    	
    	Object[][] objectArray2 = new SuperAbstract[][][] {};
    	magic += invokeArrayOperations(objectArray2);
    	objectArray2 = self(new SuperAbstract[][][] {});
    	magic += invokeArrayOperations(objectArray2);
    	objectArray2 = (Object[][]) new SuperAbstract[][][] {};
    	magic += invokeArrayOperations(objectArray2);
    	objectArray2 = new SuperClass[][][] {};
    	magic += invokeArrayOperations(objectArray2);
    	objectArray2 = self(new SuperClass[][][] {});
    	magic += invokeArrayOperations(objectArray2);
    	objectArray2 = (Object[][]) new SuperClass[][][] {};
    	magic += invokeArrayOperations(objectArray2);
    	objectArray2 = new SubClass[][][] {};
    	magic += invokeArrayOperations(objectArray2);
    	objectArray2 = self(new SubClass[][][] {});
    	magic += invokeArrayOperations(objectArray2);
    	objectArray2 = (Object[][]) new SubClass[][][] {};
    	magic += invokeArrayOperations(objectArray2);
    	
    	Object[] objectArray3 = new SuperClass[][][] {};
    	magic += invokeArrayOperations(objectArray3);
    	objectArray3 = self(new SuperClass[][][] {});
    	magic += invokeArrayOperations(objectArray3);
    	objectArray3 = (Object[]) new SuperClass[][][] {};
    	magic += invokeArrayOperations(objectArray3);
    	objectArray3 = new SubClass[][][] {};
    	magic += invokeArrayOperations(objectArray3);
    	objectArray3 = self(new SubClass[][][] {});
    	magic += invokeArrayOperations(objectArray3);
    	objectArray3 = (Object[]) new SubClass[][][] {};
    	magic += invokeArrayOperations(objectArray3);
    	
    	Object[][][] objectArray4 = new SubClass[][][] {};
    	magic += invokeArrayOperations(objectArray4);
    	objectArray4 = self(new SubClass[][][] {});
    	magic += invokeArrayOperations(objectArray4);
    	objectArray4 = (Object[][][]) new SubClass[][][] {};
    	magic += invokeArrayOperations(objectArray4);
    	
    	return magic;
    }
    
    public int testHigherDimensionalObjectArraysCastToLowerDimensionalSelves() {
    	int magic = 0;
    	
    	Object object = new Object[] {};
    	invokeObjectOperations(object);
    	object = new Object[][][] {};
    	invokeObjectOperations(object);
    	Object[] objectArray1D = new Object[][] {};
    	magic += invokeArrayOperations(objectArray1D);
    	objectArray1D = new Object[][][] {};
    	magic += invokeArrayOperations(objectArray1D);
    	Object[][] objectArray2D = new Object[][][] {};
    	magic += invokeArrayOperations(objectArray2D);
    	
    	return magic;
    }
    
    public void testAnyMultiDimensionalArrayCastsToObject() {
    	Object object = new Object[4][2][1];
    	invokeObjectOperations(object);
    	object = new SuperestInterface[][][] {};
    	invokeObjectOperations(object);
    	object = new SuperInterface[][] {};
    	invokeObjectOperations(object);
    	object = new SuperAbstract[][][] {};
    	invokeObjectOperations(object);
    	object = new SuperClass[][][] {};
    	invokeObjectOperations(object);
    	object = new SubClass[][][] {};
    	invokeObjectOperations(object);
    }
    
    public void testMultipleArrayTypeCastings() {
    	downToSubClass(
    		toObjectArray2D(
    			toObject(
    				downToSuperAbstract(
    					upToSuperestInterface(new SubClass[][][] {})))));
    }

    // -------------------------------------HELPERS-------------------------------------------------

    private void invokeObjectOperations(Object object) {
        object.hashCode();
        object.equals(object);
    }

    private int invokeArrayOperations(Object[] array) {
        array.hashCode();
        array.clone();
        array.clone().hashCode();
        return array.length + array.clone().length + array.clone().clone().length;
    }
    
    private Object getObject() {
        return new Object();
    }

    private Object[] getObjectArray1D() {
        return new Object[]{};
    }

    private Object[][] getObjectArray2D() {
        return new Object[][]{};
    }

    private Object[][][] getObjectArray3D() {
        return new Object[][][]{};
    }
    
    private SuperestInterface[] self(SuperestInterface[] superestInterface) {
    	return superestInterface;
    }
    
    private SuperInterface[] self(SuperInterface[] superInterface) {
    	return superInterface;
    }
    
    private SuperAbstract[] self(SuperAbstract[] superAbstract) {
    	return superAbstract;
    }
    
    private SuperClass[] self(SuperClass[] superClass) {
    	return superClass;
    }
    
    private SubClass[] self(SubClass[] subClass) {
    	return subClass;
    }
    
    private SuperestInterface[][] self(SuperestInterface[][] superestInterface) {
    	return superestInterface;
    }
    
    private SuperAbstract[][] self(SuperAbstract[][] superAbstract) {
    	return superAbstract;
    }
    
    private SuperClass[][] self(SuperClass[][] superClass) {
    	return superClass;
    }
    
    private SubClass[][] self(SubClass[][] subClass) {
    	return subClass;
    }
    
    private SuperestInterface[][][] self(SuperestInterface[][][] superestInterface) {
    	return superestInterface;
    }
    
    private SuperAbstract[][][] self(SuperAbstract[][][] superAbstract) {
    	return superAbstract;
    }
    
    private SuperClass[][][] self(SuperClass[][][] superClass) {
    	return superClass;
    }
    
    private SubClass[][][] self(SubClass[][][] subClass) {
    	return subClass;
    }
    
    private SuperestInterface[][][] upToSuperestInterface(SubClass[][][] subClass) {
    	return (SuperestInterface[][][]) subClass;
    }
    
    private SuperAbstract[][][] downToSuperAbstract(SuperestInterface[][][] superestInterface) {
    	return (SuperAbstract[][][]) superestInterface;
    }
    
    private Object toObject(SuperAbstract[][][] superAbstract) {
    	return superAbstract;
    }
    
    private Object[][] toObjectArray2D(Object object) {
    	return (Object[][]) object;
    }
    
    private SubClass[][][] downToSubClass(Object[][] objectArray2D) {
    	return (SubClass[][][]) objectArray2D;
    }

    // ------------------------------- CLASSES & INTERFACES ----------------------------------------

    public interface SuperestInterface {
        int get();
    }

    public interface SuperInterface extends SuperestInterface {
        int put();
    }

    abstract class SuperAbstract implements SuperInterface {
        abstract boolean set();

        @Override
        public int hashCode() {
            return 17;
        }

        @Override
        public String toString() {
            return "SuperAbstract";
        }
    }

    public class SuperClass extends SuperAbstract {

        @Override
        public int get() {
            return 3;
        }

        @Override
        public int put() {
            return 2;
        }

        @Override
        public boolean set() {
            return true;
        }

        public long has() {
            return 1;
        }

        @Override
        public int hashCode() {
            return 17;
        }

        @Override
        public String toString() {
            return "SuperClass";
        }

    }

    public class SubClass extends SuperClass {

        @Override
        public int get() {
            return 1;
        }

        @Override
        public int put() {
            return 0;
        }

        @Override
        public boolean set() {
            return false;
        }

        @Override
        public long has() {
            return 0;
        }

        @Override
        public int hashCode() {
            return 17;
        }

        @Override
        public String toString() {
            return "SubClass";
        }

    }

}
