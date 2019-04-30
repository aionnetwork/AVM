package org.aion.avm.core.arraywrapping;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.aion.avm.core.SimpleAvm;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that the expected type relationships between arrays hold in our transformed code.
 */
public class ArrayTypeTest {
    private SimpleAvm avm;
    private Class<?> clazz;
    private ArrayTypeContract contract;
    private boolean preserveDebuggability = false;

    @Before
    public void setup() throws ClassNotFoundException {
        avm = new SimpleAvm(1000000000000000000L,
            this.preserveDebuggability,
            ArrayTypeContract.class,
            ArrayTypeContract.SuperestInterface.class,
            ArrayTypeContract.SuperInterface.class,
            ArrayTypeContract.SuperAbstract.class,
            ArrayTypeContract.SuperClass.class,
            ArrayTypeContract.SubClass.class
        );
        clazz = avm.getClassLoader().loadUserClassByOriginalName(ArrayTypeContract.class.getName(), this.preserveDebuggability);
        contract = new ArrayTypeContract();
    }

    @After
    public void teardown() {
        avm.shutdown();
        contract = null;
    }
    
    @Test
    public void testPrimitiveArraysCastToObject() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testPrimitiveArraysCastToObject"));
        method.invoke(clazz.getConstructor().newInstance());
    }
    
    @Test
    public void test2DprimitiveArraysCastToObjectAndObjectArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("test2DprimitiveArraysCastToObjectAndObjectArray"));
        assertEquals(contract.test2DprimitiveArraysCastToObjectAndObjectArray(), method.invoke(clazz.getConstructor().newInstance()));
    }
    
    @Test
    public void test4DprimitiveArraysCastToObjectAndObjectArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("test3DprimitiveArraysCastToObjectAndObjectArray"));
        assertEquals(contract.test3DprimitiveArraysCastToObjectAndObjectArray(), method.invoke(clazz.getConstructor().newInstance()));
    }
    
    @Test
    public void testPrimitiveArraysCanClone() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testPrimitiveArraysCanClone"));
        assertEquals(contract.testPrimitiveArraysCanClone(), method.invoke(clazz.getConstructor().newInstance()));
    }

    @Test
    public void testObjectHashCode() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testObjectHashCode"));
        method.invoke(clazz.getConstructor().newInstance());
    }

    @Test
    public void testObjectToString() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testObjectToString"));
        org.aion.avm.shadow.java.lang.String expectedShadowString = new org.aion.avm.shadow.java.lang.String("java.lang.Object@3java.lang.Object@4SubClassSuperClass");
        assertEquals(expectedShadowString, method.invoke(clazz.getConstructor().newInstance()));
    }

    @Test
    public void testObjectEquals1() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testObjectEquals1"));
        assertEquals(contract.testObjectEquals1(), method.invoke(clazz.getConstructor().newInstance()));
    }

    @Test
    public void testObjectEquals2() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testObjectEquals2"));
        assertEquals(contract.testObjectEquals2(), method.invoke(clazz.getConstructor().newInstance()));
    }

    @Test
    public void testObjectArrayUnifications() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testMdObjectArraysUnifyToObject"));
        method.invoke(clazz.getConstructor().newInstance());
    }

    @Test
    public void testMdObjectArrayUnifications() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testMdObjectArraysUnifyTo1DObjectArray"));
        assertEquals(contract.testMdObjectArraysUnifyTo1DObjectArray(), method.invoke(clazz.getConstructor().newInstance()));
    }

    @Test
    public void test1DobjectArrayUnificationToSupers() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("test1DobjectArrayUnificationToSupers"));
        assertEquals(contract.test1DobjectArrayUnificationToSupers(), method.invoke(clazz.getConstructor().newInstance()));
    }
    
    @Test
    public void test1DobjectArrayUnificationToSupersWithCasts() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("test1DobjectArrayUnificationToSupersWithCasts"));
        assertEquals(contract.test1DobjectArrayUnificationToSupersWithCasts(), method.invoke(clazz.getConstructor().newInstance()));
    }
    
    @Test
    public void test1DobjectArrayUnificationToSupersFromMethods() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("test1DobjectArrayUnificationToSupersFromMethods"));
        assertEquals(contract.test1DobjectArrayUnificationToSupersFromMethods(), method.invoke(clazz.getConstructor().newInstance()));
    }
    
    @Test
    public void testSubClassIsSuperestInterface() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testSubClassIsSuperestInterface"));
        method.invoke(clazz.getConstructor().newInstance());
    }
    
    @Test
    public void testSubClassIsSuperInterface() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testSubClassIsSuperInterface"));
        method.invoke(clazz.getConstructor().newInstance());
    }
    
    @Test
    public void testSubClassIsSuperAbstract() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testSubClassIsSuperAbstract"));
        method.invoke(clazz.getConstructor().newInstance());
    }
    
    @Test
    public void test1DobjectArrayUnificationToObjectArray() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("test1DobjectArrayUnificationToObjectArray"));
        method.invoke(clazz.getConstructor().newInstance());
    }
    
    @Test
    public void test1DobjectArrayUnificationToObject() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("test1DobjectArrayUnificationToObject"));
        method.invoke(clazz.getConstructor().newInstance());
    }
    
    @Test
    public void testTypeRulesOnArraysWithSameDimensionality1() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testTypeRulesOnArraysWithSameDimensionality1"));
        assertEquals(contract.testTypeRulesOnArraysWithSameDimensionality1(), method.invoke(clazz.getConstructor().newInstance()));
    }
    
    @Test
    public void testTypeRulesOnArraysWithSameDimensionality2() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testTypeRulesOnArraysWithSameDimensionality2"));
        assertEquals(contract.testTypeRulesOnArraysWithSameDimensionality2(), method.invoke(clazz.getConstructor().newInstance()));
    }
    
    @Test
    public void testMultiDimensionalArraysCastToLowerDimensionalObjectArray1() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testMultiDimensionalArraysCastToLowerDimensionalObjectArray1"));
        assertEquals(contract.testMultiDimensionalArraysCastToLowerDimensionalObjectArray1(), method.invoke(clazz.getConstructor().newInstance()));
    }
    
    @Test
    public void testMultiDimensionalArraysCastToLowerDimensionalObjectArray2() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testMultiDimensionalArraysCastToLowerDimensionalObjectArray2"));
        assertEquals(contract.testMultiDimensionalArraysCastToLowerDimensionalObjectArray2(), method.invoke(clazz.getConstructor().newInstance()));
    }
    
    @Test
    public void testHigherDimensionalObjectArraysCastToLowerDimensionalSelves() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testHigherDimensionalObjectArraysCastToLowerDimensionalSelves"));
        assertEquals(contract.testHigherDimensionalObjectArraysCastToLowerDimensionalSelves(), method.invoke(clazz.getConstructor().newInstance()));
    }
    
    @Test
    public void testAnyMultiDimensionalArrayCastsToObject() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testAnyMultiDimensionalArrayCastsToObject"));
        method.invoke(clazz.getConstructor().newInstance());
    }
    
    @Test
    public void testMultipleArrayTypeCastings() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Method method = clazz.getMethod(NamespaceMapper.mapMethodName("testMultipleArrayTypeCastings"));
        method.invoke(clazz.getConstructor().newInstance());
    }

}
