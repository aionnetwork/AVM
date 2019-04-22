package avm.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import org.aion.avm.ArrayUtil;
import org.aion.avm.NameStyle;
import org.aion.avm.internal.PackageConstants;
import org.junit.Test;

public class ArrayUtilTest {

    @Test
    public void testIsPreRenamePrimitiveArray1D() {
        String array = byte[].class.getName();

        assertTrue(ArrayUtil.isPreRenameSingleDimensionPrimitiveArray(array));
        assertFalse(ArrayUtil.isPreRenameMultiDimensionPrimitiveArray(array));
        assertTrue(ArrayUtil.isPreRenamePrimitiveArray(array));

        assertFalse(ArrayUtil.isPreRenameObjectArray(array));
        assertTrue(ArrayUtil.isPreRenameArray(array));

        assertFalse(ArrayUtil.isPostRenameArray(NameStyle.DOT_NAME, array));
        assertFalse(ArrayUtil.isPostRenameArray(NameStyle.SLASH_NAME, array));

        assertTrue(ArrayUtil.isSingleDimensionalPrimitiveArray(NameStyle.DOT_NAME, array));
        assertTrue(ArrayUtil.isSingleDimensionalPrimitiveArray(NameStyle.SLASH_NAME, array));
        assertFalse(ArrayUtil.isMultiDimensionalPrimitiveArray(NameStyle.DOT_NAME, array));
        assertFalse(ArrayUtil.isMultiDimensionalPrimitiveArray(NameStyle.SLASH_NAME, array));

        assertTrue(ArrayUtil.isPrimitiveArray(NameStyle.DOT_NAME, array));
        assertTrue(ArrayUtil.isPrimitiveArray(NameStyle.SLASH_NAME, array));
        assertFalse(ArrayUtil.isObjectArray(NameStyle.DOT_NAME, array));
        assertFalse(ArrayUtil.isObjectArray(NameStyle.SLASH_NAME, array));

        assertTrue(ArrayUtil.isArray(NameStyle.DOT_NAME, array));
        assertTrue(ArrayUtil.isArray(NameStyle.SLASH_NAME, array));
    }

    @Test
    public void testIsPreRenamePrimitiveArrayMD() {
        String array = double[][][].class.getName();

        assertFalse(ArrayUtil.isPreRenameSingleDimensionPrimitiveArray(array));
        assertTrue(ArrayUtil.isPreRenameMultiDimensionPrimitiveArray(array));
        assertTrue(ArrayUtil.isPreRenamePrimitiveArray(array));

        assertFalse(ArrayUtil.isPreRenameObjectArray(array));
        assertTrue(ArrayUtil.isPreRenameArray(array));

        assertFalse(ArrayUtil.isPostRenameArray(NameStyle.DOT_NAME, array));
        assertFalse(ArrayUtil.isPostRenameArray(NameStyle.SLASH_NAME, array));

        assertFalse(ArrayUtil.isSingleDimensionalPrimitiveArray(NameStyle.DOT_NAME, array));
        assertFalse(ArrayUtil.isSingleDimensionalPrimitiveArray(NameStyle.SLASH_NAME, array));
        assertTrue(ArrayUtil.isMultiDimensionalPrimitiveArray(NameStyle.DOT_NAME, array));
        assertTrue(ArrayUtil.isMultiDimensionalPrimitiveArray(NameStyle.SLASH_NAME, array));

        assertTrue(ArrayUtil.isPrimitiveArray(NameStyle.DOT_NAME, array));
        assertTrue(ArrayUtil.isPrimitiveArray(NameStyle.SLASH_NAME, array));
        assertFalse(ArrayUtil.isObjectArray(NameStyle.DOT_NAME, array));
        assertFalse(ArrayUtil.isObjectArray(NameStyle.SLASH_NAME, array));

        assertTrue(ArrayUtil.isArray(NameStyle.DOT_NAME, array));
        assertTrue(ArrayUtil.isArray(NameStyle.SLASH_NAME, array));
    }

    @Test
    public void testIsPreRenameObjectArray() {
        String arrayWithTrailingComma = Number[].class.getName();
        String arrayDotName = arrayWithTrailingComma.substring(0, arrayWithTrailingComma.length() - 1);   // strip comma

        assertFalse(ArrayUtil.isPreRenameSingleDimensionPrimitiveArray(arrayDotName));
        assertFalse(ArrayUtil.isPreRenameMultiDimensionPrimitiveArray(arrayDotName));
        assertFalse(ArrayUtil.isPreRenamePrimitiveArray(arrayDotName));

        assertTrue(ArrayUtil.isPreRenameObjectArray(arrayDotName));
        assertTrue(ArrayUtil.isPreRenameArray(arrayDotName));

        assertFalse(ArrayUtil.isPostRenameArray(NameStyle.DOT_NAME, arrayDotName));

        String arraySlashName = arrayDotName.replaceAll("\\.", "/");
        assertFalse(ArrayUtil.isPostRenameArray(NameStyle.SLASH_NAME, arraySlashName));

        assertFalse(ArrayUtil.isPrimitiveArray(NameStyle.DOT_NAME, arrayDotName));
        assertFalse(ArrayUtil.isPrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));
        assertTrue(ArrayUtil.isObjectArray(NameStyle.DOT_NAME, arrayDotName));
        assertTrue(ArrayUtil.isObjectArray(NameStyle.SLASH_NAME, arraySlashName));

        assertTrue(ArrayUtil.isArray(NameStyle.DOT_NAME, arrayDotName));
        assertTrue(ArrayUtil.isArray(NameStyle.SLASH_NAME, arraySlashName));
    }

    @Test
    public void testIsPostRenamePrimitiveArray1DdotName() {
        String arrayDotName = org.aion.avm.arraywrapper.LongArray.class.getName();

        assertTrue(ArrayUtil.isPostRenameSingleDimensionPrimitiveArray(NameStyle.DOT_NAME, arrayDotName));
        assertFalse(ArrayUtil.isPostRenameMultiDimensionPrimitiveArray(NameStyle.DOT_NAME, arrayDotName));
        assertTrue(ArrayUtil.isPostRenamePrimitiveArray(NameStyle.DOT_NAME, arrayDotName));

        assertFalse(ArrayUtil.isPostRenameConcreteTypeObjectArray(NameStyle.DOT_NAME, arrayDotName));
        assertFalse(ArrayUtil.isPostRenameUnifyingTypeObjectArray(NameStyle.DOT_NAME, arrayDotName));
        assertFalse(ArrayUtil.isPostRenameObjectArray(NameStyle.DOT_NAME, arrayDotName));

        assertTrue(ArrayUtil.isSingleDimensionalPrimitiveArray(NameStyle.DOT_NAME, arrayDotName));
        assertFalse(ArrayUtil.isMultiDimensionalPrimitiveArray(NameStyle.DOT_NAME, arrayDotName));

        assertTrue(ArrayUtil.isPostRenameArray(NameStyle.DOT_NAME, arrayDotName));

        assertFalse(ArrayUtil.isPreRenameArray(arrayDotName));

        assertTrue(ArrayUtil.isPrimitiveArray(NameStyle.DOT_NAME, arrayDotName));
        assertFalse(ArrayUtil.isObjectArray(NameStyle.DOT_NAME, arrayDotName));

        assertTrue(ArrayUtil.isArray(NameStyle.DOT_NAME, arrayDotName));
    }

    @Test
    public void testIsPostRenamePrimitiveArray1DslashName() {
        String arraySlashName = org.aion.avm.arraywrapper.CharArray.class.getName().replaceAll("\\.", "/");

        assertTrue(ArrayUtil.isPostRenameSingleDimensionPrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));
        assertFalse(ArrayUtil.isPostRenameMultiDimensionPrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));
        assertTrue(ArrayUtil.isPostRenamePrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));

        assertFalse(ArrayUtil.isPostRenameConcreteTypeObjectArray(NameStyle.SLASH_NAME, arraySlashName));
        assertFalse(ArrayUtil.isPostRenameUnifyingTypeObjectArray(NameStyle.SLASH_NAME, arraySlashName));
        assertFalse(ArrayUtil.isPostRenameObjectArray(NameStyle.SLASH_NAME, arraySlashName));

        assertTrue(ArrayUtil.isSingleDimensionalPrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));
        assertFalse(ArrayUtil.isMultiDimensionalPrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));

        assertTrue(ArrayUtil.isPostRenameArray(NameStyle.SLASH_NAME, arraySlashName));

        assertFalse(ArrayUtil.isPreRenameArray(arraySlashName));

        assertTrue(ArrayUtil.isPrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));
        assertFalse(ArrayUtil.isObjectArray(NameStyle.SLASH_NAME, arraySlashName));

        assertTrue(ArrayUtil.isArray(NameStyle.SLASH_NAME, arraySlashName));
    }

    @Test
    public void testIsPostRenamePrimitiveArrayMDdotName() {
        String arrayDotName = PackageConstants.kArrayWrapperDotPrefix + short[][].class.getName().replaceAll("\\[", "\\$");

        assertFalse(ArrayUtil.isPostRenameSingleDimensionPrimitiveArray(NameStyle.DOT_NAME, arrayDotName));
        assertTrue(ArrayUtil.isPostRenameMultiDimensionPrimitiveArray(NameStyle.DOT_NAME, arrayDotName));
        assertTrue(ArrayUtil.isPostRenamePrimitiveArray(NameStyle.DOT_NAME, arrayDotName));

        assertFalse(ArrayUtil.isPostRenameConcreteTypeObjectArray(NameStyle.DOT_NAME, arrayDotName));
        assertFalse(ArrayUtil.isPostRenameUnifyingTypeObjectArray(NameStyle.DOT_NAME, arrayDotName));
        assertFalse(ArrayUtil.isPostRenameObjectArray(NameStyle.DOT_NAME, arrayDotName));

        assertFalse(ArrayUtil.isSingleDimensionalPrimitiveArray(NameStyle.DOT_NAME, arrayDotName));
        assertTrue(ArrayUtil.isMultiDimensionalPrimitiveArray(NameStyle.DOT_NAME, arrayDotName));

        assertTrue(ArrayUtil.isPostRenameArray(NameStyle.DOT_NAME, arrayDotName));

        assertFalse(ArrayUtil.isPreRenameArray(arrayDotName));

        assertTrue(ArrayUtil.isPrimitiveArray(NameStyle.DOT_NAME, arrayDotName));
        assertFalse(ArrayUtil.isObjectArray(NameStyle.DOT_NAME, arrayDotName));

        assertTrue(ArrayUtil.isArray(NameStyle.DOT_NAME, arrayDotName));
    }

    @Test
    public void testIsPostRenamePrimitiveArrayMDslashName() {
        String arraySlashName = (PackageConstants.kArrayWrapperSlashPrefix + float[][][].class.getName().replaceAll("\\.", "/")).replaceAll("\\[", "\\$");

        assertFalse(ArrayUtil.isPostRenameSingleDimensionPrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));
        assertTrue(ArrayUtil.isPostRenameMultiDimensionPrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));
        assertTrue(ArrayUtil.isPostRenamePrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));

        assertFalse(ArrayUtil.isPostRenameConcreteTypeObjectArray(NameStyle.SLASH_NAME, arraySlashName));
        assertFalse(ArrayUtil.isPostRenameUnifyingTypeObjectArray(NameStyle.SLASH_NAME, arraySlashName));
        assertFalse(ArrayUtil.isPostRenameObjectArray(NameStyle.SLASH_NAME, arraySlashName));

        assertFalse(ArrayUtil.isSingleDimensionalPrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));
        assertTrue(ArrayUtil.isMultiDimensionalPrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));

        assertTrue(ArrayUtil.isPostRenameArray(NameStyle.SLASH_NAME, arraySlashName));

        assertFalse(ArrayUtil.isPreRenameArray(arraySlashName));

        assertTrue(ArrayUtil.isPrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));
        assertFalse(ArrayUtil.isObjectArray(NameStyle.SLASH_NAME, arraySlashName));

        assertTrue(ArrayUtil.isArray(NameStyle.SLASH_NAME, arraySlashName));
    }

    @Test
    public void testIsPostRenameConcreteTypeObjectArrayDotName() {
        String preRenameWithTrailingComma = BigInteger[].class.getName();
        String preRenameNoTrailingComma = preRenameWithTrailingComma.substring(0, preRenameWithTrailingComma.length() - 1);

        String arrayDotName = PackageConstants.kArrayWrapperDotPrefix + preRenameNoTrailingComma.replaceAll("\\[", "\\$");

        assertFalse(ArrayUtil.isPostRenameSingleDimensionPrimitiveArray(NameStyle.DOT_NAME, arrayDotName));
        assertFalse(ArrayUtil.isPostRenameMultiDimensionPrimitiveArray(NameStyle.DOT_NAME, arrayDotName));
        assertFalse(ArrayUtil.isPostRenamePrimitiveArray(NameStyle.DOT_NAME, arrayDotName));

        assertTrue(ArrayUtil.isPostRenameConcreteTypeObjectArray(NameStyle.DOT_NAME, arrayDotName));
        assertFalse(ArrayUtil.isPostRenameUnifyingTypeObjectArray(NameStyle.DOT_NAME, arrayDotName));
        assertTrue(ArrayUtil.isPostRenameObjectArray(NameStyle.DOT_NAME, arrayDotName));

        assertTrue(ArrayUtil.isPostRenameArray(NameStyle.DOT_NAME, arrayDotName));

        assertFalse(ArrayUtil.isPreRenameArray(arrayDotName));

        assertFalse(ArrayUtil.isPrimitiveArray(NameStyle.DOT_NAME, arrayDotName));
        assertTrue(ArrayUtil.isObjectArray(NameStyle.DOT_NAME, arrayDotName));

        assertTrue(ArrayUtil.isArray(NameStyle.DOT_NAME, arrayDotName));
    }

    @Test
    public void testIsPostRenameConcreteTypeObjectArraySlashName() {
        String preRenameWithTrailingComma = BigInteger[].class.getName();
        String preRenameNoTrailingComma = preRenameWithTrailingComma.substring(0, preRenameWithTrailingComma.length() - 1);
        String preRenameSlashName = preRenameNoTrailingComma.replaceAll("\\.", "/");

        String arraySlashName = PackageConstants.kArrayWrapperSlashPrefix + preRenameSlashName.replaceAll("\\[", "\\$");

        assertFalse(ArrayUtil.isPostRenameSingleDimensionPrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));
        assertFalse(ArrayUtil.isPostRenameMultiDimensionPrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));
        assertFalse(ArrayUtil.isPostRenamePrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));

        assertTrue(ArrayUtil.isPostRenameConcreteTypeObjectArray(NameStyle.SLASH_NAME, arraySlashName));
        assertFalse(ArrayUtil.isPostRenameUnifyingTypeObjectArray(NameStyle.SLASH_NAME, arraySlashName));
        assertTrue(ArrayUtil.isPostRenameObjectArray(NameStyle.SLASH_NAME, arraySlashName));

        assertTrue(ArrayUtil.isPostRenameArray(NameStyle.SLASH_NAME, arraySlashName));

        assertFalse(ArrayUtil.isPreRenameArray(arraySlashName));

        assertFalse(ArrayUtil.isPrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));
        assertTrue(ArrayUtil.isObjectArray(NameStyle.SLASH_NAME, arraySlashName));

        assertTrue(ArrayUtil.isArray(NameStyle.SLASH_NAME, arraySlashName));
    }

    @Test
    public void testIsPostRenameUnifyingTypeObjectArrayDotName() {
        String preRenameWithTrailingComma = Byte[][].class.getName();
        String preRenameNoTrailingComma = preRenameWithTrailingComma.substring(0, preRenameWithTrailingComma.length() - 1);

        String arrayDotName = (PackageConstants.kArrayWrapperUnifyingDotPrefix + preRenameNoTrailingComma).replaceAll("\\[", "_");

        assertFalse(ArrayUtil.isPostRenameSingleDimensionPrimitiveArray(NameStyle.DOT_NAME, arrayDotName));
        assertFalse(ArrayUtil.isPostRenameMultiDimensionPrimitiveArray(NameStyle.DOT_NAME, arrayDotName));
        assertFalse(ArrayUtil.isPostRenamePrimitiveArray(NameStyle.DOT_NAME, arrayDotName));

        assertFalse(ArrayUtil.isPostRenameConcreteTypeObjectArray(NameStyle.DOT_NAME, arrayDotName));
        assertTrue(ArrayUtil.isPostRenameUnifyingTypeObjectArray(NameStyle.DOT_NAME, arrayDotName));
        assertTrue(ArrayUtil.isPostRenameObjectArray(NameStyle.DOT_NAME, arrayDotName));

        assertTrue(ArrayUtil.isPostRenameArray(NameStyle.DOT_NAME, arrayDotName));

        assertFalse(ArrayUtil.isPreRenameArray(arrayDotName));

        assertFalse(ArrayUtil.isPrimitiveArray(NameStyle.DOT_NAME, arrayDotName));
        assertTrue(ArrayUtil.isObjectArray(NameStyle.DOT_NAME, arrayDotName));

        assertTrue(ArrayUtil.isArray(NameStyle.DOT_NAME, arrayDotName));
    }

    @Test
    public void testIsPostRenameUnifyingTypeObjectArraySlashName() {
        String preRenameWithTrailingComma = Byte[][].class.getName();
        String preRenameNoTrailingComma = preRenameWithTrailingComma.substring(0, preRenameWithTrailingComma.length() - 1);
        String preRenameSlashName = preRenameNoTrailingComma.replaceAll("\\.", "/");

        String arraySlashName = (PackageConstants.kArrayWrapperUnifyingSlashPrefix + preRenameSlashName).replaceAll("\\[", "_");

        assertFalse(ArrayUtil.isPostRenameSingleDimensionPrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));
        assertFalse(ArrayUtil.isPostRenameMultiDimensionPrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));
        assertFalse(ArrayUtil.isPostRenamePrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));

        assertFalse(ArrayUtil.isPostRenameConcreteTypeObjectArray(NameStyle.SLASH_NAME, arraySlashName));
        assertTrue(ArrayUtil.isPostRenameUnifyingTypeObjectArray(NameStyle.SLASH_NAME, arraySlashName));
        assertTrue(ArrayUtil.isPostRenameObjectArray(NameStyle.SLASH_NAME, arraySlashName));

        assertTrue(ArrayUtil.isPostRenameArray(NameStyle.SLASH_NAME, arraySlashName));

        assertFalse(ArrayUtil.isPreRenameArray(arraySlashName));

        assertFalse(ArrayUtil.isPrimitiveArray(NameStyle.SLASH_NAME, arraySlashName));
        assertTrue(ArrayUtil.isObjectArray(NameStyle.SLASH_NAME, arraySlashName));

        assertTrue(ArrayUtil.isArray(NameStyle.SLASH_NAME, arraySlashName));
    }

    @Test
    public void testIsArrayOnNonArray() {
        String nonArrayDotName = Comparable.class.getName();
        String nonArraySlashName = nonArrayDotName.replaceAll("\\.", "/");

        assertFalse(ArrayUtil.isArray(NameStyle.DOT_NAME, nonArrayDotName));
        assertFalse(ArrayUtil.isArray(NameStyle.SLASH_NAME, nonArraySlashName));
    }

    @Test
    public void testDimensionOfPreRenamePrimitiveArray() {
        String array1 = double[].class.getName();
        String array2 = float[][].class.getName();
        String array3 = byte[][][].class.getName();

        assertEquals(1, ArrayUtil.dimensionOfPreRenamePrimitiveArray(array1));
        assertEquals(2, ArrayUtil.dimensionOfPreRenamePrimitiveArray(array2));
        assertEquals(3, ArrayUtil.dimensionOfPreRenamePrimitiveArray(array3));

        assertEquals(1, ArrayUtil.dimensionOfPreRenameArray(array1));
        assertEquals(2, ArrayUtil.dimensionOfPreRenameArray(array2));
        assertEquals(3, ArrayUtil.dimensionOfPreRenameArray(array3));

        assertEquals(1, ArrayUtil.dimensionOfArray(NameStyle.DOT_NAME, array1));
        assertEquals(2, ArrayUtil.dimensionOfArray(NameStyle.DOT_NAME, array2));
        assertEquals(3, ArrayUtil.dimensionOfArray(NameStyle.DOT_NAME, array3));
        assertEquals(1, ArrayUtil.dimensionOfArray(NameStyle.SLASH_NAME, array1));
        assertEquals(2, ArrayUtil.dimensionOfArray(NameStyle.SLASH_NAME, array2));
        assertEquals(3, ArrayUtil.dimensionOfArray(NameStyle.SLASH_NAME, array3));
    }

    @Test
    public void testDimensionOfPreRenameObjectArray() {
        String array1WithTrailingComma = Integer[].class.getName();
        String array2WithTrailingComma = StringBuffer[][].class.getName();
        String array3WithTrailingComma = TimeUnit[][][].class.getName();

        String array1 = array1WithTrailingComma.substring(0, array1WithTrailingComma.length() - 1); // strip comma
        String array2 = array2WithTrailingComma.substring(0, array2WithTrailingComma.length() - 1); // strip comma
        String array3 = array3WithTrailingComma.substring(0, array3WithTrailingComma.length() - 1); // strip comma

        assertEquals(1, ArrayUtil.dimensionOfPreRenameObjectArray(array1));
        assertEquals(2, ArrayUtil.dimensionOfPreRenameObjectArray(array2));
        assertEquals(3, ArrayUtil.dimensionOfPreRenameObjectArray(array3));

        assertEquals(1, ArrayUtil.dimensionOfPreRenameArray(array1));
        assertEquals(2, ArrayUtil.dimensionOfPreRenameArray(array2));
        assertEquals(3, ArrayUtil.dimensionOfPreRenameArray(array3));

        assertEquals(1, ArrayUtil.dimensionOfArray(NameStyle.DOT_NAME, array1));
        assertEquals(2, ArrayUtil.dimensionOfArray(NameStyle.DOT_NAME, array2));
        assertEquals(3, ArrayUtil.dimensionOfArray(NameStyle.DOT_NAME, array3));
        assertEquals(1, ArrayUtil.dimensionOfArray(NameStyle.SLASH_NAME, array1));
        assertEquals(2, ArrayUtil.dimensionOfArray(NameStyle.SLASH_NAME, array2));
        assertEquals(3, ArrayUtil.dimensionOfArray(NameStyle.SLASH_NAME, array3));
    }

    @Test
    public void testDimensionOfPostRenamePrimitiveArrayDotName() {
        String array1dotName = org.aion.avm.arraywrapper.DoubleArray.class.getName();
        String array2dotName = PackageConstants.kArrayWrapperDotPrefix + float[][].class.getName().replaceAll("\\[", "\\$");
        String array3dotName = PackageConstants.kArrayWrapperDotPrefix + byte[][][].class.getName().replaceAll("\\[", "\\$");

        assertEquals(1, ArrayUtil.dimensionOfPostRenamePrimitiveArray(NameStyle.DOT_NAME, array1dotName));
        assertEquals(2, ArrayUtil.dimensionOfPostRenamePrimitiveArray(NameStyle.DOT_NAME, array2dotName));
        assertEquals(3, ArrayUtil.dimensionOfPostRenamePrimitiveArray(NameStyle.DOT_NAME, array3dotName));

        assertEquals(1, ArrayUtil.dimensionOfPostRenameArray(NameStyle.DOT_NAME, array1dotName));
        assertEquals(2, ArrayUtil.dimensionOfPostRenameArray(NameStyle.DOT_NAME, array2dotName));
        assertEquals(3, ArrayUtil.dimensionOfPostRenameArray(NameStyle.DOT_NAME, array3dotName));

        assertEquals(1, ArrayUtil.dimensionOfArray(NameStyle.DOT_NAME, array1dotName));
        assertEquals(2, ArrayUtil.dimensionOfArray(NameStyle.DOT_NAME, array2dotName));
        assertEquals(3, ArrayUtil.dimensionOfArray(NameStyle.DOT_NAME, array3dotName));
    }

    @Test
    public void testDimensionOfPostRenamePrimitiveArraySlashName() {
        String array1slashName = org.aion.avm.arraywrapper.CharArray.class.getName().replaceAll("\\.", "/");
        String array2slashName = PackageConstants.kArrayWrapperSlashPrefix + int[][].class.getName().replaceAll("\\[", "\\$");
        String array3slashName = PackageConstants.kArrayWrapperSlashPrefix + long[][][].class.getName().replaceAll("\\[", "\\$");

        assertEquals(1, ArrayUtil.dimensionOfPostRenamePrimitiveArray(NameStyle.SLASH_NAME, array1slashName));
        assertEquals(2, ArrayUtil.dimensionOfPostRenamePrimitiveArray(NameStyle.SLASH_NAME, array2slashName));
        assertEquals(3, ArrayUtil.dimensionOfPostRenamePrimitiveArray(NameStyle.SLASH_NAME, array3slashName));

        assertEquals(1, ArrayUtil.dimensionOfPostRenameArray(NameStyle.SLASH_NAME, array1slashName));
        assertEquals(2, ArrayUtil.dimensionOfPostRenameArray(NameStyle.SLASH_NAME, array2slashName));
        assertEquals(3, ArrayUtil.dimensionOfPostRenameArray(NameStyle.SLASH_NAME, array3slashName));

        assertEquals(1, ArrayUtil.dimensionOfArray(NameStyle.SLASH_NAME, array1slashName));
        assertEquals(2, ArrayUtil.dimensionOfArray(NameStyle.SLASH_NAME, array2slashName));
        assertEquals(3, ArrayUtil.dimensionOfArray(NameStyle.SLASH_NAME, array3slashName));
    }

    @Test
    public void testDimensionOfPostRenameConcreteTypeObjectArrayDotName() {
        String array1preRename = Byte[].class.getName();
        String array2preRename = CharSequence[][].class.getName();
        String array3preRename = Serializable[][][].class.getName();

        String array1preRenameNoTrailingComma = array1preRename.substring(0, array1preRename.length() - 1);
        String array2preRenameNoTrailingComma = array2preRename.substring(0, array2preRename.length() - 1);
        String array3preRenameNoTrailingComma = array3preRename.substring(0, array3preRename.length() - 1);

        String array1 = (PackageConstants.kArrayWrapperDotPrefix + array1preRenameNoTrailingComma).replaceAll("\\[", "\\$");
        String array2 = (PackageConstants.kArrayWrapperDotPrefix + array2preRenameNoTrailingComma).replaceAll("\\[", "\\$");
        String array3 = (PackageConstants.kArrayWrapperDotPrefix + array3preRenameNoTrailingComma).replaceAll("\\[", "\\$");

        assertEquals(1, ArrayUtil.dimensionOfPostRenameObjectArray(NameStyle.DOT_NAME, array1));
        assertEquals(2, ArrayUtil.dimensionOfPostRenameObjectArray(NameStyle.DOT_NAME, array2));
        assertEquals(3, ArrayUtil.dimensionOfPostRenameObjectArray(NameStyle.DOT_NAME, array3));

        assertEquals(1, ArrayUtil.dimensionOfPostRenameArray(NameStyle.DOT_NAME, array1));
        assertEquals(2, ArrayUtil.dimensionOfPostRenameArray(NameStyle.DOT_NAME, array2));
        assertEquals(3, ArrayUtil.dimensionOfPostRenameArray(NameStyle.DOT_NAME, array3));

        assertEquals(1, ArrayUtil.dimensionOfArray(NameStyle.DOT_NAME, array1));
        assertEquals(2, ArrayUtil.dimensionOfArray(NameStyle.DOT_NAME, array2));
        assertEquals(3, ArrayUtil.dimensionOfArray(NameStyle.DOT_NAME, array3));
    }

    @Test
    public void testDimensionOfPostRenameConcreteTypeObjectArraySlashName() {
        String array1preRename = Float[].class.getName();
        String array2preRename = System[][].class.getName();
        String array3preRename = Object[][][].class.getName();

        String array1preRenameNoTrailingComma = array1preRename.substring(0, array1preRename.length() - 1);
        String array2preRenameNoTrailingComma = array2preRename.substring(0, array2preRename.length() - 1);
        String array3preRenameNoTrailingComma = array3preRename.substring(0, array3preRename.length() - 1);

        String array1 = (PackageConstants.kArrayWrapperSlashPrefix + array1preRenameNoTrailingComma.replaceAll("\\.", "/")).replaceAll("\\[", "\\$");
        String array2 = (PackageConstants.kArrayWrapperSlashPrefix + array2preRenameNoTrailingComma.replaceAll("\\.", "/")).replaceAll("\\[", "\\$");
        String array3 = (PackageConstants.kArrayWrapperSlashPrefix + array3preRenameNoTrailingComma.replaceAll("\\.", "/")).replaceAll("\\[", "\\$");

        assertEquals(1, ArrayUtil.dimensionOfPostRenameObjectArray(NameStyle.SLASH_NAME, array1));
        assertEquals(2, ArrayUtil.dimensionOfPostRenameObjectArray(NameStyle.SLASH_NAME, array2));
        assertEquals(3, ArrayUtil.dimensionOfPostRenameObjectArray(NameStyle.SLASH_NAME, array3));

        assertEquals(1, ArrayUtil.dimensionOfPostRenameArray(NameStyle.SLASH_NAME, array1));
        assertEquals(2, ArrayUtil.dimensionOfPostRenameArray(NameStyle.SLASH_NAME, array2));
        assertEquals(3, ArrayUtil.dimensionOfPostRenameArray(NameStyle.SLASH_NAME, array3));

        assertEquals(1, ArrayUtil.dimensionOfArray(NameStyle.SLASH_NAME, array1));
        assertEquals(2, ArrayUtil.dimensionOfArray(NameStyle.SLASH_NAME, array2));
        assertEquals(3, ArrayUtil.dimensionOfArray(NameStyle.SLASH_NAME, array3));
    }

    @Test
    public void testDimensionOfPostRenameUnifyingTypeObjectArrayDotName() {
        String array1preRename = Byte[].class.getName();
        String array2preRename = CharSequence[][].class.getName();
        String array3preRename = Serializable[][][].class.getName();

        String array1preRenameNoTrailingComma = array1preRename.substring(0, array1preRename.length() - 1);
        String array2preRenameNoTrailingComma = array2preRename.substring(0, array2preRename.length() - 1);
        String array3preRenameNoTrailingComma = array3preRename.substring(0, array3preRename.length() - 1);

        String array1 = PackageConstants.kArrayWrapperUnifyingDotPrefix + array1preRenameNoTrailingComma.replaceAll("\\[", "_");
        String array2 = PackageConstants.kArrayWrapperUnifyingDotPrefix + array2preRenameNoTrailingComma.replaceAll("\\[", "_");
        String array3 = PackageConstants.kArrayWrapperUnifyingDotPrefix + array3preRenameNoTrailingComma.replaceAll("\\[", "_");

        assertEquals(1, ArrayUtil.dimensionOfPostRenameObjectArray(NameStyle.DOT_NAME, array1));
        assertEquals(2, ArrayUtil.dimensionOfPostRenameObjectArray(NameStyle.DOT_NAME, array2));
        assertEquals(3, ArrayUtil.dimensionOfPostRenameObjectArray(NameStyle.DOT_NAME, array3));

        assertEquals(1, ArrayUtil.dimensionOfPostRenameArray(NameStyle.DOT_NAME, array1));
        assertEquals(2, ArrayUtil.dimensionOfPostRenameArray(NameStyle.DOT_NAME, array2));
        assertEquals(3, ArrayUtil.dimensionOfPostRenameArray(NameStyle.DOT_NAME, array3));

        assertEquals(1, ArrayUtil.dimensionOfArray(NameStyle.DOT_NAME, array1));
        assertEquals(2, ArrayUtil.dimensionOfArray(NameStyle.DOT_NAME, array2));
        assertEquals(3, ArrayUtil.dimensionOfArray(NameStyle.DOT_NAME, array3));
    }

    @Test
    public void testDimensionOfPostRenameUnifyingTypeObjectArraySlashName() {
        String array1preRename = Float[].class.getName();
        String array2preRename = System[][].class.getName();
        String array3preRename = Object[][][].class.getName();

        String array1preRenameNoTrailingComma = array1preRename.substring(0, array1preRename.length() - 1);
        String array2preRenameNoTrailingComma = array2preRename.substring(0, array2preRename.length() - 1);
        String array3preRenameNoTrailingComma = array3preRename.substring(0, array3preRename.length() - 1);

        String array1 = (PackageConstants.kArrayWrapperUnifyingSlashPrefix + array1preRenameNoTrailingComma.replaceAll("\\.", "/")).replaceAll("\\[", "_");
        String array2 = (PackageConstants.kArrayWrapperUnifyingSlashPrefix + array2preRenameNoTrailingComma.replaceAll("\\.", "/")).replaceAll("\\[", "_");
        String array3 = (PackageConstants.kArrayWrapperUnifyingSlashPrefix + array3preRenameNoTrailingComma.replaceAll("\\.", "/")).replaceAll("\\[", "_");

        assertEquals(1, ArrayUtil.dimensionOfPostRenameObjectArray(NameStyle.SLASH_NAME, array1));
        assertEquals(2, ArrayUtil.dimensionOfPostRenameObjectArray(NameStyle.SLASH_NAME, array2));
        assertEquals(3, ArrayUtil.dimensionOfPostRenameObjectArray(NameStyle.SLASH_NAME, array3));

        assertEquals(1, ArrayUtil.dimensionOfPostRenameArray(NameStyle.SLASH_NAME, array1));
        assertEquals(2, ArrayUtil.dimensionOfPostRenameArray(NameStyle.SLASH_NAME, array2));
        assertEquals(3, ArrayUtil.dimensionOfPostRenameArray(NameStyle.SLASH_NAME, array3));

        assertEquals(1, ArrayUtil.dimensionOfArray(NameStyle.SLASH_NAME, array1));
        assertEquals(2, ArrayUtil.dimensionOfArray(NameStyle.SLASH_NAME, array2));
        assertEquals(3, ArrayUtil.dimensionOfArray(NameStyle.SLASH_NAME, array3));
    }
}
