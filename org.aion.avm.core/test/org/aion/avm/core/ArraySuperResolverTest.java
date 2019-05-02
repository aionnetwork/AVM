package org.aion.avm.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.Set;
import org.aion.avm.ArrayRenamer;
import org.aion.avm.NameStyle;
import org.aion.avm.core.ClassRenamer.ArrayType;
import org.aion.avm.core.types.ClassHierarchy;
import org.aion.avm.core.types.ClassHierarchyBuilder;
import org.aion.avm.core.types.CommonType;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link ArraySuperResolver} and {@link TypeAwareClassWriter}!
 *
 * These two classes should give identical responses to each query, and so this is checked for each
 * test case.
 */
public class ArraySuperResolverTest {
    private static boolean preserveDebuggability = false;
    private ClassRenamer classRenamer;
    private ArraySuperResolver resolver;
    private TypeAwareClassWriter typeAwareClassWriter;

    private String specialArray = org.aion.avm.arraywrapper.Array.class.getName();
    private String specialIArray = org.aion.avm.arraywrapper.IArray.class.getName();
    private String specialObjectArray = org.aion.avm.arraywrapper.ObjectArray.class.getName();
    private String specialIObjectArray = i.IObjectArray.class.getName();

    private String preRenameNonArray = java.lang.String.class.getName();
    private String postRenameConcreteClass = s.java.lang.String.class.getName();
    private String postRenameInterface = s.java.lang.Comparable.class.getName();
    private String preRenamePrimitiveArray1D = int[].class.getName();
    private String preRenamePrimitiveArray1Dother = byte[].class.getName();
    private String preRenamePrimitiveArrayMD = boolean[][].class.getName();
    private String preRenamePrimitiveArrayMDother = char[][][].class.getName();
    private String preRenameObjectArray = java.lang.String[].class.getName();

    private String postRenamePrimitiveArray1D;
    private String postRenamePrimitiveArray1Dother;
    private String postRenamePrimitiveArrayMD;
    private String postRenamePrimitiveArrayMDother;
    private String postRenameObjectArrayConcreteType;
    private String postRenameObjectArrayUnifyingType;
    private String exceptionWrapper;

    @Before
    public void setup() {
        // Remove the trailing ';' character.
        this.preRenameObjectArray = this.preRenameObjectArray.substring(0, this.preRenameObjectArray.length() - 1);

        ClassHierarchy hierarchy = new ClassHierarchyBuilder()
            .addShadowJcl()
            .addPostRenameJclExceptions()
            .addHandwrittenArrayWrappers()
            .build();
        this.classRenamer = new ClassRenamerBuilder(NameStyle.DOT_NAME, preserveDebuggability)
            .loadPostRenameJclExceptionClasses(fetchPostRenameSlashStyleJclExceptions())
            .build();
        this.resolver = new ArraySuperResolver(hierarchy, this.classRenamer);

        this.typeAwareClassWriter = new ClassWriter(hierarchy, this.classRenamer);

        this.postRenamePrimitiveArray1D = this.classRenamer.toPostRename(this.preRenamePrimitiveArray1D, ArrayType.PRECISE_TYPE);
        this.postRenamePrimitiveArrayMD = this.classRenamer.toPostRename(this.preRenamePrimitiveArrayMD, ArrayType.PRECISE_TYPE);
        this.postRenameObjectArrayConcreteType = this.classRenamer.toPostRename(this.preRenameObjectArray, ArrayType.PRECISE_TYPE);
        this.postRenameObjectArrayUnifyingType = this.classRenamer.toPostRename(this.preRenameObjectArray, ArrayType.UNIFYING_TYPE);
        this.postRenamePrimitiveArray1Dother = this.classRenamer.toPostRename(this.preRenamePrimitiveArray1Dother, ArrayType.PRECISE_TYPE);
        this.postRenamePrimitiveArrayMDother = this.classRenamer.toPostRename(this.preRenamePrimitiveArrayMDother, ArrayType.PRECISE_TYPE);
        this.exceptionWrapper = this.classRenamer.toExceptionWrapper(s.java.lang.RuntimeException.class.getName());
    }

    @Test
    public void testSuperOfTwoNonArrays() {
        assertNull(this.resolver.getTightestSuperClassIfGivenArray(this.preRenameNonArray, this.preRenameNonArray));
    }

    @Test
    public void testSuperOfPreRenameArrayAndPostRenameNonArray() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.preRenamePrimitiveArray1D, this.postRenameConcreteClass);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.preRenamePrimitiveArray1D, this.postRenameConcreteClass);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfPostRenameArrayAndPreRenameNonArray() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.postRenameObjectArrayUnifyingType, this.preRenameNonArray);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenameObjectArrayUnifyingType, this.preRenameNonArray);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfPostRenameArrayAndExceptionWrapper() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.postRenamePrimitiveArrayMD, this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenamePrimitiveArrayMD, this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfPostRenamePrimitiveArrayAndConcreteClass() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.postRenamePrimitiveArray1D, this.postRenameConcreteClass);
        assertEquals(CommonType.SHADOW_OBJECT.dotName, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.postRenamePrimitiveArrayMD, this.postRenameConcreteClass);
        assertEquals(CommonType.SHADOW_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenamePrimitiveArray1D, this.postRenameConcreteClass);
        assertEquals(CommonType.SHADOW_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenamePrimitiveArrayMD, this.postRenameConcreteClass);
        assertEquals(CommonType.SHADOW_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfConcreteTypeObjectArrayAndConcreteClass() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.postRenameObjectArrayConcreteType, this.postRenameConcreteClass);
        assertEquals(CommonType.SHADOW_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenameObjectArrayConcreteType, this.postRenameConcreteClass);
        assertEquals(CommonType.SHADOW_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfUnifyingTypeObjectArrayAndConcreteClass() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.postRenameObjectArrayUnifyingType, this.postRenameConcreteClass);
        assertEquals(CommonType.I_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenameObjectArrayUnifyingType, this.postRenameConcreteClass);
        assertEquals(CommonType.I_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfPostRenamePrimitiveArrayAndInterface() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.postRenamePrimitiveArray1D, this.postRenameInterface);
        assertEquals(CommonType.I_OBJECT.dotName, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.postRenamePrimitiveArrayMD, this.postRenameInterface);
        assertEquals(CommonType.I_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenamePrimitiveArray1D, this.postRenameInterface);
        assertEquals(CommonType.I_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenamePrimitiveArrayMD, this.postRenameInterface);
        assertEquals(CommonType.I_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfConcreteTypeObjectArrayAndInterface() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.postRenameObjectArrayConcreteType, this.postRenameInterface);
        assertEquals(CommonType.I_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenameObjectArrayConcreteType, this.postRenameInterface);
        assertEquals(CommonType.I_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfUnifyingTypeObjectArrayAndInterface() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.postRenameObjectArrayUnifyingType, this.postRenameInterface);
        assertEquals(CommonType.I_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenameObjectArrayUnifyingType, this.postRenameInterface);
        assertEquals(CommonType.I_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfTwoPreRenamePrimitiveArraysSameDimension() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.preRenamePrimitiveArray1D, this.preRenamePrimitiveArray1D);
        assertEquals(preRenamePrimitiveArray1D, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.preRenamePrimitiveArray1D, this.preRenamePrimitiveArray1Dother);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.preRenamePrimitiveArray1D, this.preRenamePrimitiveArray1D);
        assertEquals(preRenamePrimitiveArray1D.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.preRenamePrimitiveArray1D, this.preRenamePrimitiveArray1Dother);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfTwoPreRenamePrimitiveArraysDiffDimension() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.preRenamePrimitiveArray1D, this.preRenamePrimitiveArrayMD);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.preRenamePrimitiveArray1D, this.preRenamePrimitiveArrayMD);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfPreRenamePrimitiveAndObjectArrays() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.preRenamePrimitiveArrayMD, this.preRenameObjectArray);
        assertEquals(ArrayRenamer.prependPreRenameObjectArrayPrefix(CommonType.JAVA_LANG_OBJECT.dotName, 1), commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.preRenamePrimitiveArrayMD, this.preRenameObjectArray);
        assertEquals(ArrayRenamer.prependPreRenameObjectArrayPrefix(CommonType.JAVA_LANG_OBJECT.dotName, 1).replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfTwoPreRenameObjectArraysSameDimension() {
        String array1 = java.lang.OutOfMemoryError[].class.getName();
        String array2 = java.lang.StackOverflowError[].class.getName();
        String superArray = java.lang.VirtualMachineError[].class.getName();

        // Strip the trailing ';' characters.
        array1 = array1.substring(0, array1.length() - 1);
        array2 = array2.substring(0, array2.length() - 1);
        superArray = superArray.substring(0, superArray.length() - 1);

        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(array1, array2);
        assertEquals(superArray, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(array1, array2);
        assertEquals(superArray.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfTwoPreRenameObjectArraysDiffDimension() {
        String array1 = java.lang.OutOfMemoryError[].class.getName();
        String array2 = java.lang.StackOverflowError[][].class.getName();

        // Strip the trailing ';' characters.
        array1 = array1.substring(0, array1.length() - 1);
        array2 = array2.substring(0, array2.length() - 1);

        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(array1, array2);
        assertEquals(ArrayRenamer.prependPreRenameObjectArrayPrefix(CommonType.JAVA_LANG_OBJECT.dotName, 1), commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(array1, array2);
        assertEquals(ArrayRenamer.prependPreRenameObjectArrayPrefix(CommonType.JAVA_LANG_OBJECT.dotName, 1).replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfTwoPostRenamePrimitiveArraysSameDimension() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.postRenamePrimitiveArray1D, this.postRenamePrimitiveArray1D);
        assertEquals(this.postRenamePrimitiveArray1D, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.postRenamePrimitiveArray1D, this.postRenamePrimitiveArray1Dother);
        assertEquals(CommonType.SHADOW_OBJECT.dotName, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.postRenamePrimitiveArrayMD, this.postRenamePrimitiveArrayMD);
        assertEquals(this.postRenamePrimitiveArrayMD, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.postRenamePrimitiveArrayMD, this.postRenamePrimitiveArrayMDother);
        assertEquals(CommonType.SHADOW_OBJECT.dotName, commonSuper);

        //--------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenamePrimitiveArray1D, this.postRenamePrimitiveArray1D);
        assertEquals(this.postRenamePrimitiveArray1D.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenamePrimitiveArray1D, this.postRenamePrimitiveArray1Dother);
        assertEquals(CommonType.SHADOW_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenamePrimitiveArrayMD, this.postRenamePrimitiveArrayMD);
        assertEquals(this.postRenamePrimitiveArrayMD.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenamePrimitiveArrayMD, this.postRenamePrimitiveArrayMDother);
        assertEquals(CommonType.SHADOW_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfTwoPostRenamePrimitiveArraysDiffDimension() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.postRenamePrimitiveArray1D, this.postRenamePrimitiveArrayMD);
        assertEquals(CommonType.SHADOW_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenamePrimitiveArray1D, this.postRenamePrimitiveArrayMD);
        assertEquals(CommonType.SHADOW_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfPostRenamePrimitiveAndConcreteTypeObjectArrays() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.postRenamePrimitiveArray1D, this.postRenameObjectArrayConcreteType);
        assertEquals(CommonType.SHADOW_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenamePrimitiveArray1D, this.postRenameObjectArrayConcreteType);
        assertEquals(CommonType.SHADOW_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfPostRenamePrimitiveAndUnifyingTypeObjectArrays() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.postRenamePrimitiveArrayMD, this.postRenameObjectArrayUnifyingType);
        assertEquals(CommonType.I_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenamePrimitiveArrayMD, this.postRenameObjectArrayUnifyingType);
        assertEquals(CommonType.I_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfTwoPostRenameConcreteTypeObjectArraysSameDimension() {
        String class1 = java.lang.OutOfMemoryError.class.getName();
        String class2 = java.lang.StackOverflowError.class.getName();
        String superClass = java.lang.VirtualMachineError.class.getName();

        String postRenameClass1 = this.classRenamer.toPostRename(class1, ArrayType.PRECISE_TYPE);
        String postRenameClass2 = this.classRenamer.toPostRename(class2, ArrayType.PRECISE_TYPE);
        String postRenameSuper = this.classRenamer.toPostRename(superClass, ArrayType.PRECISE_TYPE);

        String array1 = ArrayRenamer.wrapAsConcreteObjectArray(NameStyle.DOT_NAME, postRenameClass1, 1);
        String array2 = ArrayRenamer.wrapAsConcreteObjectArray(NameStyle.DOT_NAME, postRenameClass2, 1);
        String superArray = ArrayRenamer.wrapAsConcreteObjectArray(NameStyle.DOT_NAME, postRenameSuper, 1);

        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(array1, array2);
        assertEquals(superArray, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(array1, array2);
        assertEquals(superArray.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfTwoPostRenameConcreteTypeObjectArraysDiffDimension() {
        String class1 = java.lang.OutOfMemoryError.class.getName();
        String class2 = java.lang.StackOverflowError.class.getName();

        String postRenameClass1 = this.classRenamer.toPostRename(class1, ArrayType.PRECISE_TYPE);
        String postRenameClass2 = this.classRenamer.toPostRename(class2, ArrayType.PRECISE_TYPE);

        String array1 = ArrayRenamer.wrapAsConcreteObjectArray(NameStyle.DOT_NAME, postRenameClass1, 1);
        String array2 = ArrayRenamer.wrapAsConcreteObjectArray(NameStyle.DOT_NAME, postRenameClass2, 2);

        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(array1, array2);
        assertEquals(CommonType.SHADOW_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(array1, array2);
        assertEquals(CommonType.SHADOW_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfTwoPostRenameUnifyingTypeObjectArraysSameDimension() {
        String class1 = java.lang.OutOfMemoryError.class.getName();
        String class2 = java.lang.StackOverflowError.class.getName();
        String superClass = java.lang.VirtualMachineError.class.getName();

        String postRenameClass1 = this.classRenamer.toPostRename(class1, ArrayType.UNIFYING_TYPE);
        String postRenameClass2 = this.classRenamer.toPostRename(class2, ArrayType.UNIFYING_TYPE);
        String postRenameSuper = this.classRenamer.toPostRename(superClass, ArrayType.UNIFYING_TYPE);

        String array1 = ArrayRenamer.wrapAsConcreteObjectArray(NameStyle.DOT_NAME, postRenameClass1, 3);
        String array2 = ArrayRenamer.wrapAsConcreteObjectArray(NameStyle.DOT_NAME, postRenameClass2, 3);
        String superArray = ArrayRenamer.wrapAsConcreteObjectArray(NameStyle.DOT_NAME, postRenameSuper, 3);

        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(array1, array2);
        assertEquals(superArray, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(array1, array2);
        assertEquals(superArray.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfTwoPostRenameUnifyingTypeObjectArraysDiffDimension() {
        String class1 = java.lang.OutOfMemoryError.class.getName();
        String class2 = java.lang.StackOverflowError.class.getName();

        String postRenameClass1 = this.classRenamer.toPostRename(class1, ArrayType.UNIFYING_TYPE);
        String postRenameClass2 = this.classRenamer.toPostRename(class2, ArrayType.UNIFYING_TYPE);

        String array1 = ArrayRenamer.wrapAsUnifyingObjectArray(NameStyle.DOT_NAME, postRenameClass1, 2);
        String array2 = ArrayRenamer.wrapAsUnifyingObjectArray(NameStyle.DOT_NAME, postRenameClass2, 3);

        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(array1, array2);
        assertEquals(CommonType.I_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(array1, array2);
        assertEquals(CommonType.I_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfConcreteAndUnifyingArraysWithSameBaseTypeSameDimension() {
        String baseType = java.lang.String.class.getName();
        String postRenameBaseType = this.classRenamer.toPostRename(baseType, ArrayType.PRECISE_TYPE);

        String concreteArray = ArrayRenamer.wrapAsConcreteObjectArray(NameStyle.DOT_NAME, postRenameBaseType, 2);
        String unifyingArray = ArrayRenamer.wrapAsUnifyingObjectArray(NameStyle.DOT_NAME, postRenameBaseType, 2);

        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(concreteArray, unifyingArray);
        assertEquals(unifyingArray, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(concreteArray, unifyingArray);
        assertEquals(unifyingArray.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfConcreteAndUnifyingArraysWithSameBaseTypeDiffDimension() {
        String baseType = java.lang.String.class.getName();
        String postRenameBaseType = this.classRenamer.toPostRename(baseType, ArrayType.PRECISE_TYPE);

        String concreteArray = ArrayRenamer.wrapAsConcreteObjectArray(NameStyle.DOT_NAME, postRenameBaseType, 2);
        String unifyingArray = ArrayRenamer.wrapAsUnifyingObjectArray(NameStyle.DOT_NAME, postRenameBaseType, 1);

        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(concreteArray, unifyingArray);
        assertEquals(CommonType.I_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(concreteArray, unifyingArray);
        assertEquals(CommonType.I_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfConcreteAndUnifyingArraysSameDimension() {
        String class1 = java.lang.OutOfMemoryError.class.getName();
        String class2 = java.lang.StackOverflowError.class.getName();
        String superClass = java.lang.VirtualMachineError.class.getName();

        String postRenameClass1 = this.classRenamer.toPostRename(class1, ArrayType.UNIFYING_TYPE);
        String postRenameClass2 = this.classRenamer.toPostRename(class2, ArrayType.UNIFYING_TYPE);
        String postRenameSuper = this.classRenamer.toPostRename(superClass, ArrayType.UNIFYING_TYPE);

        String concreteArray = ArrayRenamer.wrapAsConcreteObjectArray(NameStyle.DOT_NAME, postRenameClass1, 2);
        String unifyingArray = ArrayRenamer.wrapAsUnifyingObjectArray(NameStyle.DOT_NAME, postRenameClass2, 2);
        String superArray = ArrayRenamer.wrapAsUnifyingObjectArray(NameStyle.DOT_NAME, postRenameSuper, 2);

        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(concreteArray, unifyingArray);
        assertEquals(superArray, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(concreteArray, unifyingArray);
        assertEquals(superArray.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfConcreteAndUnifyingArraysDiffDimension() {
        String class1 = java.lang.OutOfMemoryError.class.getName();
        String class2 = java.lang.StackOverflowError.class.getName();

        String postRenameClass1 = this.classRenamer.toPostRename(class1, ArrayType.UNIFYING_TYPE);
        String postRenameClass2 = this.classRenamer.toPostRename(class2, ArrayType.UNIFYING_TYPE);

        String concreteArray = ArrayRenamer.wrapAsConcreteObjectArray(NameStyle.DOT_NAME, postRenameClass1, 2);
        String unifyingArray = ArrayRenamer.wrapAsUnifyingObjectArray(NameStyle.DOT_NAME, postRenameClass2, 1);

        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(concreteArray, unifyingArray);
        assertEquals(CommonType.I_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(concreteArray, unifyingArray);
        assertEquals(CommonType.I_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperWhenMultipleTightestCommonSupersExistPostRename() {
        String class1 = java.lang.Integer.class.getName();
        String class2 = java.lang.Character.class.getName();
        String superClass = i.IObject.class.getName();

        String postRenameClass1 = this.classRenamer.toPostRename(class1, ArrayType.UNIFYING_TYPE);
        String postRenameClass2 = this.classRenamer.toPostRename(class2, ArrayType.UNIFYING_TYPE);

        String array1 = ArrayRenamer.wrapAsConcreteObjectArray(NameStyle.DOT_NAME, postRenameClass1, 1);
        String array2 = ArrayRenamer.wrapAsConcreteObjectArray(NameStyle.DOT_NAME, postRenameClass2, 1);
        String expectedSuper = ArrayRenamer.wrapAsUnifyingObjectArray(NameStyle.DOT_NAME, superClass, 1);

        // IObject is given when the tightest super is ambiguous.
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(array1, array2);
        assertEquals(expectedSuper, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(array1, array2);
        assertEquals(expectedSuper.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperWhenMultipleTightestCommonSupersExistPreRename() {
        String array1 = java.lang.Integer[].class.getName();
        String array2 = java.lang.Character[].class.getName();
        String superArray = java.lang.Object[].class.getName();

        // Strip the trailing ';' character.
        array1 = array1.substring(0, array1.length() - 1);
        array2 = array2.substring(0, array2.length() - 1);
        superArray = superArray.substring(0, superArray.length() - 1);

        // java.lang.Object is given when the tightest super is ambiguous.
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(array1, array2);
        assertEquals(superArray, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(array1, array2);
        assertEquals(superArray.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfPreRenameArrayAndJavaLangObject() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.preRenameObjectArray, CommonType.JAVA_LANG_OBJECT.dotName);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.preRenameObjectArray, CommonType.JAVA_LANG_OBJECT.dotName);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfPostRenameArrayAndJavaLangObject() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.postRenamePrimitiveArray1D, CommonType.JAVA_LANG_OBJECT.dotName);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenamePrimitiveArray1D, CommonType.JAVA_LANG_OBJECT.dotName);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    // Below tests test out the 'special' arrays. These types are hidden, so that any special type trying
    // to unify with a non-special type will result in a non-special array type unification.

    @Test
    public void testSuperOfSpecialArrayAndOtherArray() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialArray, this.postRenamePrimitiveArray1D);
        assertEquals(this.specialArray, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialArray, this.postRenamePrimitiveArrayMD);
        assertEquals(CommonType.SHADOW_OBJECT.dotName, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialArray, this.postRenameObjectArrayConcreteType);
        assertEquals(CommonType.SHADOW_OBJECT.dotName, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialArray, this.postRenameObjectArrayUnifyingType);
        assertEquals(CommonType.I_OBJECT.dotName, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialArray, this.specialArray);
        assertEquals(this.specialArray, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialArray, this.specialIArray);
        assertEquals(this.specialIArray, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialArray, this.specialObjectArray);
        assertEquals(this.specialArray, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialArray, this.specialIObjectArray);
        assertEquals(this.specialIArray, commonSuper);

        // --------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialArray, this.postRenamePrimitiveArray1D);
        assertEquals(this.specialArray.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialArray, this.postRenamePrimitiveArrayMD);
        assertEquals(CommonType.SHADOW_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialArray, this.postRenameObjectArrayConcreteType);
        assertEquals(CommonType.SHADOW_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialArray, this.postRenameObjectArrayUnifyingType);
        assertEquals(CommonType.I_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialArray, this.specialArray);
        assertEquals(this.specialArray.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialArray, this.specialIArray);
        assertEquals(this.specialIArray.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialArray, this.specialObjectArray);
        assertEquals(this.specialArray.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialArray, this.specialIObjectArray);
        assertEquals(this.specialIArray.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfSpecialIArrayAndOtherArray() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialIArray, this.postRenamePrimitiveArray1D);
        assertEquals(this.specialIArray, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialIArray, this.postRenamePrimitiveArrayMD);
        assertEquals(CommonType.I_OBJECT.dotName, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialIArray, this.postRenameObjectArrayConcreteType);
        assertEquals(CommonType.I_OBJECT.dotName, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialIArray, this.postRenameObjectArrayUnifyingType);
        assertEquals(CommonType.I_OBJECT.dotName, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialIArray, this.specialArray);
        assertEquals(this.specialIArray, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialIArray, this.specialIArray);
        assertEquals(this.specialIArray, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialIArray, this.specialObjectArray);
        assertEquals(this.specialIArray, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialIArray, this.specialIObjectArray);
        assertEquals(this.specialIArray, commonSuper);

        // ------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialIArray, this.postRenamePrimitiveArray1D);
        assertEquals(this.specialIArray.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialIArray, this.postRenamePrimitiveArrayMD);
        assertEquals(CommonType.I_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialIArray, this.postRenameObjectArrayConcreteType);
        assertEquals(CommonType.I_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialIArray, this.postRenameObjectArrayUnifyingType);
        assertEquals(CommonType.I_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialIArray, this.specialArray);
        assertEquals(this.specialIArray.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialIArray, this.specialIArray);
        assertEquals(this.specialIArray.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialIArray, this.specialObjectArray);
        assertEquals(this.specialIArray.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialIArray, this.specialIObjectArray);
        assertEquals(this.specialIArray.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfSpecialObjectArrayAndOtherArray() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialObjectArray, this.postRenamePrimitiveArray1D);
        assertEquals(CommonType.SHADOW_OBJECT.dotName, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialObjectArray, this.postRenamePrimitiveArrayMD);
        assertEquals(CommonType.SHADOW_OBJECT.dotName, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialObjectArray, this.postRenameObjectArrayConcreteType);
        assertEquals(this.specialObjectArray, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialObjectArray, this.postRenameObjectArrayUnifyingType);
        assertEquals(CommonType.I_OBJECT.dotName, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialObjectArray, this.specialArray);
        assertEquals(this.specialArray, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialObjectArray, this.specialIArray);
        assertEquals(this.specialIArray, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialObjectArray, this.specialObjectArray);
        assertEquals(this.specialObjectArray, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialObjectArray, this.specialIObjectArray);
        assertEquals(this.specialIObjectArray, commonSuper);

        // ---------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialObjectArray, this.postRenamePrimitiveArray1D);
        assertEquals(CommonType.SHADOW_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialObjectArray, this.postRenamePrimitiveArrayMD);
        assertEquals(CommonType.SHADOW_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialObjectArray, this.postRenameObjectArrayConcreteType);
        assertEquals(this.specialObjectArray.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialObjectArray, this.postRenameObjectArrayUnifyingType);
        assertEquals(CommonType.I_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialObjectArray, this.specialArray);
        assertEquals(this.specialArray.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialObjectArray, this.specialIArray);
        assertEquals(this.specialIArray.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialObjectArray, this.specialObjectArray);
        assertEquals(this.specialObjectArray.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialObjectArray, this.specialIObjectArray);
        assertEquals(this.specialIObjectArray.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfSpecialIObjectArrayAndOtherArray() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialIObjectArray, this.postRenamePrimitiveArray1D);
        assertEquals(CommonType.I_OBJECT.dotName, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialIObjectArray, this.postRenamePrimitiveArrayMD);
        assertEquals(CommonType.I_OBJECT.dotName, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialIObjectArray, this.postRenameObjectArrayConcreteType);
        assertEquals(this.specialIObjectArray, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialIObjectArray, this.postRenameObjectArrayUnifyingType);
        assertEquals(this.specialIObjectArray, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialIObjectArray, this.specialArray);
        assertEquals(this.specialIArray, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialIObjectArray, this.specialIArray);
        assertEquals(this.specialIArray, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialIObjectArray, this.specialObjectArray);
        assertEquals(this.specialIObjectArray, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenArray(this.specialIObjectArray, this.specialIObjectArray);
        assertEquals(this.specialIObjectArray, commonSuper);

        // -------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialIObjectArray, this.postRenamePrimitiveArray1D);
        assertEquals(CommonType.I_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialIObjectArray, this.postRenamePrimitiveArrayMD);
        assertEquals(CommonType.I_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialIObjectArray, this.postRenameObjectArrayConcreteType);
        assertEquals(this.specialIObjectArray.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialIObjectArray, this.postRenameObjectArrayUnifyingType);
        assertEquals(this.specialIObjectArray.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialIObjectArray, this.specialArray);
        assertEquals(this.specialIArray.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialIObjectArray, this.specialIArray);
        assertEquals(this.specialIArray.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialIObjectArray, this.specialObjectArray);
        assertEquals(this.specialIObjectArray.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.specialIObjectArray, this.specialIObjectArray);
        assertEquals(this.specialIObjectArray.replaceAll("\\.", "/"), commonSuper);
    }

    private Set<String> fetchPostRenameSlashStyleJclExceptions() {
        Set<String> jclExceptions = new HashSet<>();
        for (CommonType type : CommonType.values()) {
            if (type.isShadowException) {
                jclExceptions.add(type.dotName);
            }
        }
        return jclExceptions;
    }

    private static class ClassWriter extends TypeAwareClassWriter {
        public ClassWriter(ClassHierarchy classHierarchy, ClassRenamer classRenamer) {
            super(0, classHierarchy, classRenamer);
        }
    }
}
