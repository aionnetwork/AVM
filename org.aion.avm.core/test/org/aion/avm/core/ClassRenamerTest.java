package org.aion.avm.core;

import static org.junit.Assert.assertEquals;

import i.PackageConstants;
import i.RuntimeAssertionError;
import java.util.HashSet;
import java.util.Set;
import org.aion.avm.NameStyle;
import org.aion.avm.core.ClassRenamer.ArrayType;
import org.aion.avm.core.rejection.RejectedClassException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClassRenamerTest {
    private static String userClass = "A";
    private static ClassRenamer preRenameLoadedDotRenamerDebugModeEnabled;
    private static ClassRenamer preRenameLoadedDotRenamer;
    private static ClassRenamer preRenameLoadedSlashRenamerDebugModeEnabled;
    private static ClassRenamer preRenameLoadedSlashRenamer;

    private static ClassRenamer postRenameLoadedDotRenamerDebugModeEnabled;
    private static ClassRenamer postRenameLoadedDotRenamer;
    private static ClassRenamer postRenameLoadedSlashRenamerDebugModeEnabled;
    private static ClassRenamer postRenameLoadedSlashRenamer;

    @BeforeClass
    public static void setup() {
        initializePreRenameLoadedClassRenamers();
        initializePostRenameLoadedClassRenamers();
    }
    
    @Test
    public void testPrimitive1DArrayDotNameRenaming() {
        String array = byte[].class.getName();
        String expected = a.ByteArray.class.getName();

        checkRenamingDotStyle(array, expected, expected, ArrayType.PRECISE_TYPE);
        checkRenamingDotStyle(array, expected, expected, ArrayType.UNIFYING_TYPE);
    }

    @Test
    public void testPrimitive1DArraySlashNameRenaming() {
        String array = byte[].class.getName();
        String expected = a.ByteArray.class.getName().replaceAll("\\.", "/");

        checkRenamingSlashStyle(array, expected, expected, ArrayType.PRECISE_TYPE);
        checkRenamingSlashStyle(array, expected, expected, ArrayType.UNIFYING_TYPE);
    }

    @Test
    public void testPrimitiveMDArrayDotNameRenaming() {
        String array = boolean[][][].class.getName();
        String expected = PackageConstants.kArrayWrapperDotPrefix + array.replaceAll("\\[", "\\$");

        checkRenamingDotStyle(array, expected, expected, ArrayType.PRECISE_TYPE);
        checkRenamingDotStyle(array, expected, expected, ArrayType.UNIFYING_TYPE);
    }

    @Test
    public void testPrimitiveMDArraySlashNameRenaming() {
        String array = boolean[][][].class.getName();
        String expected = PackageConstants.kArrayWrapperSlashPrefix + array.replaceAll("\\[", "\\$");

        checkRenamingSlashStyle(array, expected, expected, ArrayType.PRECISE_TYPE);
        checkRenamingSlashStyle(array, expected, expected, ArrayType.UNIFYING_TYPE);
    }

    @Test
    public void testObjectArrayDotNameRenaming() {
        String array = s.java.math.BigInteger[][].class.getName();
        String expectedConcreteName = PackageConstants.kArrayWrapperDotPrefix + array.replaceAll("\\[", "\\$");
        String expectedUnifyingName = PackageConstants.kArrayWrapperUnifyingDotPrefix + array.replaceAll("\\[", "_");

        // Remove the trailing commas from these names...
        array = array.substring(0, array.length() - 1);
        expectedConcreteName = expectedConcreteName.substring(0, expectedConcreteName.length() - 1);
        expectedUnifyingName = expectedUnifyingName.substring(0, expectedUnifyingName.length() - 1);

        checkRenamingDotStyle(array, expectedConcreteName, expectedConcreteName, ArrayType.PRECISE_TYPE);
        checkRenamingDotStyle(array, expectedUnifyingName, expectedUnifyingName, ArrayType.UNIFYING_TYPE);
    }

    @Test
    public void testObjectArraySlashNameRenaming() {
        String array = s.java.math.BigInteger[][].class.getName().replaceAll("\\.", "/");
        String expectedConcreteName = PackageConstants.kArrayWrapperSlashPrefix + array.replaceAll("\\[", "\\$").replaceAll("\\.", "/");
        String expectedUnifyingName = PackageConstants.kArrayWrapperUnifyingSlashPrefix + array.replaceAll("\\[", "_").replaceAll("\\.", "/");

        // Remove the trailing commas from these names...
        array = array.substring(0, array.length() - 1);
        expectedConcreteName = expectedConcreteName.substring(0, expectedConcreteName.length() - 1);
        expectedUnifyingName = expectedUnifyingName.substring(0, expectedUnifyingName.length() - 1);

        checkRenamingSlashStyle(array, expectedConcreteName, expectedConcreteName, ArrayType.PRECISE_TYPE);
        checkRenamingSlashStyle(array, expectedUnifyingName, expectedUnifyingName, ArrayType.UNIFYING_TYPE);
    }
    
    @Test
    public void testApiDotNameRenaming() {
        String blockchain = avm.Blockchain.class.getName();
        String result = avm.Result.class.getName();
        String address = avm.Address.class.getName();

        String expectedBlockchain = p.avm.Blockchain.class.getName();
        String expectedResult = p.avm.Result.class.getName();
        String expectedAddress = p.avm.Address.class.getName();

        checkRenamingDotStyle(blockchain, expectedBlockchain, expectedBlockchain, ArrayType.NOT_ARRAY);
        checkRenamingDotStyle(result, expectedResult, expectedResult, ArrayType.NOT_ARRAY);
        checkRenamingDotStyle(address, expectedAddress, expectedAddress, ArrayType.NOT_ARRAY);
    }

    @Test
    public void testApiSlashNameRenaming() {
        String blockchain = avm.Blockchain.class.getName().replaceAll("\\.", "/");
        String result = avm.Result.class.getName().replaceAll("\\.", "/");
        String address = avm.Address.class.getName().replaceAll("\\.", "/");

        String expectedBlockchain = p.avm.Blockchain.class.getName().replaceAll("\\.", "/");
        String expectedResult = p.avm.Result.class.getName().replaceAll("\\.", "/");
        String expectedAddress = p.avm.Address.class.getName().replaceAll("\\.", "/");

        checkRenamingSlashStyle(blockchain, expectedBlockchain, expectedBlockchain, ArrayType.NOT_ARRAY);
        checkRenamingSlashStyle(result, expectedResult, expectedResult, ArrayType.NOT_ARRAY);
        checkRenamingSlashStyle(address, expectedAddress, expectedAddress, ArrayType.NOT_ARRAY);
    }
    
    @Test
    public void testJclDotNameRenaming() {
        String object = java.lang.Object.class.getName();
        String throwable = java.lang.Throwable.class.getName();
        String exception = java.lang.Exception.class.getName();
        String io = java.io.Serializable.class.getName();
        String concurrent = java.util.concurrent.TimeUnit.class.getName();
        String function = java.util.function.Function.class.getName();
        String util = java.util.Set.class.getName();
        String math = java.math.RoundingMode.class.getName();

        String expectedObject = s.java.lang.Object.class.getName();
        String expectedThrowable = s.java.lang.Throwable.class.getName();
        String expectedException = s.java.lang.Exception.class.getName();
        String expectedIo = s.java.io.Serializable.class.getName();
        String expectedConcurrent = s.java.util.concurrent.TimeUnit.class.getName();
        String expectedFunction = s.java.util.function.Function.class.getName();
        String expectedUtil = s.java.util.Set.class.getName();
        String expectedMath = s.java.math.RoundingMode.class.getName();

        checkRenamingDotStyle(object, expectedObject, expectedObject, ArrayType.NOT_ARRAY);
        checkRenamingDotStyle(throwable, expectedThrowable, expectedThrowable, ArrayType.NOT_ARRAY);
        checkRenamingDotStyle(exception, expectedException, expectedException, ArrayType.NOT_ARRAY);
        checkRenamingDotStyle(io, expectedIo, expectedIo, ArrayType.NOT_ARRAY);
        checkRenamingDotStyle(concurrent, expectedConcurrent, expectedConcurrent, ArrayType.NOT_ARRAY);
        checkRenamingDotStyle(function, expectedFunction, expectedFunction, ArrayType.NOT_ARRAY);
        checkRenamingDotStyle(util, expectedUtil, expectedUtil, ArrayType.NOT_ARRAY);
        checkRenamingDotStyle(math, expectedMath, expectedMath, ArrayType.NOT_ARRAY);
    }

    @Test
    public void testJclSlashNameRenaming() {
        String object = java.lang.Object.class.getName().replaceAll("\\.", "/");
        String throwable = java.lang.Throwable.class.getName().replaceAll("\\.", "/");
        String exception = java.lang.Exception.class.getName().replaceAll("\\.", "/");
        String io = java.io.Serializable.class.getName().replaceAll("\\.", "/");
        String concurrent = java.util.concurrent.TimeUnit.class.getName().replaceAll("\\.", "/");
        String function = java.util.function.Function.class.getName().replaceAll("\\.", "/");
        String util = java.util.Set.class.getName().replaceAll("\\.", "/");
        String math = java.math.RoundingMode.class.getName().replaceAll("\\.", "/");

        String expectedObject = s.java.lang.Object.class.getName().replaceAll("\\.", "/");
        String expectedThrowable = s.java.lang.Throwable.class.getName().replaceAll("\\.", "/");
        String expectedException = s.java.lang.Exception.class.getName().replaceAll("\\.", "/");
        String expectedIo = s.java.io.Serializable.class.getName().replaceAll("\\.", "/");
        String expectedConcurrent = s.java.util.concurrent.TimeUnit.class.getName().replaceAll("\\.", "/");
        String expectedFunction = s.java.util.function.Function.class.getName().replaceAll("\\.", "/");
        String expectedUtil = s.java.util.Set.class.getName().replaceAll("\\.", "/");
        String expectedMath = s.java.math.RoundingMode.class.getName().replaceAll("\\.", "/");

        checkRenamingSlashStyle(object, expectedObject, expectedObject, ArrayType.NOT_ARRAY);
        checkRenamingSlashStyle(throwable, expectedThrowable, expectedThrowable, ArrayType.NOT_ARRAY);
        checkRenamingSlashStyle(exception, expectedException, expectedException, ArrayType.NOT_ARRAY);
        checkRenamingSlashStyle(io, expectedIo, expectedIo, ArrayType.NOT_ARRAY);
        checkRenamingSlashStyle(concurrent, expectedConcurrent, expectedConcurrent, ArrayType.NOT_ARRAY);
        checkRenamingSlashStyle(function, expectedFunction, expectedFunction, ArrayType.NOT_ARRAY);
        checkRenamingSlashStyle(util, expectedUtil, expectedUtil, ArrayType.NOT_ARRAY);
        checkRenamingSlashStyle(math, expectedMath, expectedMath, ArrayType.NOT_ARRAY);
    }
    
    @Test
    public void testUserlibDotNameRenaming() {
        String buffer = org.aion.avm.userlib.AionBuffer.class.getName();
        String decoder = org.aion.avm.userlib.abi.ABIDecoder.class.getName();

        String expectedBufferDebug = buffer;
        String expectedDecoderDebug = decoder;
        String expectedBuffer = PackageConstants.kUserDotPrefix + buffer;
        String expectedDecoder = PackageConstants.kUserDotPrefix + decoder;

        checkRenamingDotStyle(buffer, expectedBuffer, expectedBufferDebug, ArrayType.NOT_ARRAY);
        checkRenamingDotStyle(decoder, expectedDecoder, expectedDecoderDebug, ArrayType.NOT_ARRAY);
    }

    @Test
    public void testUserlibSlashNameRenaming() {
        String buffer = org.aion.avm.userlib.AionBuffer.class.getName().replaceAll("\\.", "/");
        String decoder = org.aion.avm.userlib.abi.ABIDecoder.class.getName().replaceAll("\\.", "/");

        String expectedBufferDebug = buffer;
        String expectedDecoderDebug = decoder;
        String expectedBuffer = PackageConstants.kUserSlashPrefix + buffer;
        String expectedDecoder = PackageConstants.kUserSlashPrefix + decoder;

        checkRenamingSlashStyle(buffer, expectedBuffer, expectedBufferDebug, ArrayType.NOT_ARRAY);
        checkRenamingSlashStyle(decoder, expectedDecoder, expectedDecoderDebug, ArrayType.NOT_ARRAY);
    }
    
    @Test
    public void testUserClassDotNameRenaming() {
        String expectedUserClassDebug = userClass;
        String expectedUserClass = PackageConstants.kUserDotPrefix + userClass;

        checkRenamingDotStyle(userClass, expectedUserClass, expectedUserClassDebug, ArrayType.NOT_ARRAY);
    }

    @Test
    public void testUserClassSlashNameRenaming() {
        String expectedUserClassDebug = userClass.replaceAll("\\.", "/");
        String expectedUserClass = PackageConstants.kUserSlashPrefix + userClass.replaceAll("\\.", "/");

        checkRenamingSlashStyle(userClass, expectedUserClass, expectedUserClassDebug, ArrayType.NOT_ARRAY);
    }

    @Test
    public void testJclExceptionDotNameRenaming() {
        String exception = java.lang.VirtualMachineError.class.getName();
        String expectedException = PackageConstants.kShadowDotPrefix + exception;

        checkRenamingDotStyle(exception, expectedException, expectedException, ArrayType.NOT_ARRAY);
    }

    @Test
    public void testJclExceptionSlashNameRenaming() {
        String exception = java.lang.VirtualMachineError.class.getName().replaceAll("\\.", "/");
        String expectedException = PackageConstants.kShadowSlashPrefix + exception;

        checkRenamingSlashStyle(exception, expectedException, expectedException, ArrayType.NOT_ARRAY);
    }

    @Test(expected = RejectedClassException.class)
    public void testPostRenameClassRejection() {
        String nonWhitelisted = java.lang.instrument.ClassDefinition.class.getName();

        preRenameLoadedDotRenamer.toPostRenameOrRejectClass(nonWhitelisted, ArrayType.NOT_ARRAY);
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testPostRenameClassAssertionError() {
        String postRename = PackageConstants.kUserDotPrefix + userClass;

        preRenameLoadedDotRenamer.toPostRename(postRename, ArrayType.NOT_ARRAY);
    }

    @Test
    public void testExceptionWrapperRenaming() {
        String exception = java.lang.StackOverflowError.class.getName();
        String expectedWrapper = PackageConstants.kExceptionWrapperDotPrefix + exception;

        // Do the renaming.
        String wrappedExceptionDebug = preRenameLoadedDotRenamerDebugModeEnabled.toExceptionWrapper(exception);
        String wrappedException = preRenameLoadedDotRenamer.toExceptionWrapper(exception);

        // Verify we got the correct post-rename name.
        assertEquals(expectedWrapper, wrappedExceptionDebug);
        assertEquals(expectedWrapper, wrappedException);

        // Verify we get the original name back.
        assertEquals(exception, preRenameLoadedDotRenamerDebugModeEnabled.toPreRename(wrappedExceptionDebug));
        assertEquals(exception, preRenameLoadedDotRenamer.toPreRename(wrappedException));
    }

    //----------------------------------------------------------------------------------------------

    private static void initializePostRenameLoadedClassRenamers() {
        Set<String> postRenameDotNameExceptions = new HashSet<>();
        postRenameDotNameExceptions.add(PackageConstants.kShadowDotPrefix + java.lang.VirtualMachineError.class.getName());

        Set<String> postRenameDotNameUserClassesDebug = new HashSet<>();
        postRenameDotNameUserClassesDebug.add(userClass);
        postRenameDotNameUserClassesDebug.add(org.aion.avm.userlib.AionBuffer.class.getName());
        postRenameDotNameUserClassesDebug.add(org.aion.avm.userlib.abi.ABIDecoder.class.getName());

        Set<String> postRenameDotNameUserClasses = new HashSet<>();
        postRenameDotNameUserClasses.add(PackageConstants.kUserDotPrefix + userClass);
        postRenameDotNameUserClasses.add(PackageConstants.kUserDotPrefix + org.aion.avm.userlib.AionBuffer.class.getName());
        postRenameDotNameUserClasses.add(PackageConstants.kUserDotPrefix + org.aion.avm.userlib.abi.ABIDecoder.class.getName());

        Set<String> postRenameSlashNameExceptions = new HashSet<>();
        postRenameSlashNameExceptions.add(PackageConstants.kShadowSlashPrefix + java.lang.VirtualMachineError.class.getName().replaceAll("\\.", "/"));

        Set<String> postRenameSlashNameUserClassesDebug = new HashSet<>();
        postRenameSlashNameUserClassesDebug.add(userClass.replaceAll("\\.", "/"));
        postRenameSlashNameUserClassesDebug.add(org.aion.avm.userlib.AionBuffer.class.getName().replaceAll("\\.", "/"));
        postRenameSlashNameUserClassesDebug.add(org.aion.avm.userlib.abi.ABIDecoder.class.getName().replaceAll("\\.", "/"));

        Set<String> postRenameSlashNameUserClasses = new HashSet<>();
        postRenameSlashNameUserClasses.add(PackageConstants.kUserSlashPrefix + userClass.replaceAll("\\.", "/"));
        postRenameSlashNameUserClasses.add(PackageConstants.kUserSlashPrefix + org.aion.avm.userlib.AionBuffer.class.getName().replaceAll("\\.", "/"));
        postRenameSlashNameUserClasses.add(PackageConstants.kUserSlashPrefix + org.aion.avm.userlib.abi.ABIDecoder.class.getName().replaceAll("\\.", "/"));

        postRenameLoadedDotRenamerDebugModeEnabled = new ClassRenamerBuilder(NameStyle.DOT_NAME, true)
            .loadPostRenameUserDefinedClasses(postRenameDotNameUserClassesDebug)
            .loadPostRenameJclExceptionClasses(postRenameDotNameExceptions)
            .build();
        postRenameLoadedDotRenamer = new ClassRenamerBuilder(NameStyle.DOT_NAME, false)
            .loadPostRenameUserDefinedClasses(postRenameDotNameUserClasses)
            .loadPostRenameJclExceptionClasses(postRenameDotNameExceptions)
            .build();
        postRenameLoadedSlashRenamerDebugModeEnabled = new ClassRenamerBuilder(NameStyle.SLASH_NAME, true)
            .loadPostRenameUserDefinedClasses(postRenameSlashNameUserClassesDebug)
            .loadPostRenameJclExceptionClasses(postRenameSlashNameExceptions)
            .build();
        postRenameLoadedSlashRenamer = new ClassRenamerBuilder(NameStyle.SLASH_NAME, false)
            .loadPostRenameUserDefinedClasses(postRenameSlashNameUserClasses)
            .loadPostRenameJclExceptionClasses(postRenameSlashNameExceptions)
            .build();
    }

    private static void initializePreRenameLoadedClassRenamers() {
        Set<String> preRenameDotNameUserClasses = new HashSet<>();
        preRenameDotNameUserClasses.add(userClass);
        preRenameDotNameUserClasses.add(org.aion.avm.userlib.AionBuffer.class.getName());
        preRenameDotNameUserClasses.add(org.aion.avm.userlib.abi.ABIDecoder.class.getName());

        Set<String> preRenameDotNameExceptions = new HashSet<>();
        preRenameDotNameExceptions.add(java.lang.VirtualMachineError.class.getName());

        Set<String> preRenameSlashNameUserClasses = new HashSet<>();
        preRenameSlashNameUserClasses.add(userClass.replaceAll("\\.", "/"));
        preRenameSlashNameUserClasses.add(org.aion.avm.userlib.AionBuffer.class.getName().replaceAll("\\.", "/"));
        preRenameSlashNameUserClasses.add(org.aion.avm.userlib.abi.ABIDecoder.class.getName().replaceAll("\\.", "/"));

        Set<String> preRenameSlashNameExceptions = new HashSet<>();
        preRenameSlashNameExceptions.add(java.lang.VirtualMachineError.class.getName().replaceAll("\\.", "/"));

        preRenameLoadedDotRenamerDebugModeEnabled = new ClassRenamerBuilder(NameStyle.DOT_NAME, true)
            .loadPreRenameUserDefinedClasses(preRenameDotNameUserClasses)
            .loadPreRenameJclExceptionClasses(preRenameDotNameExceptions)
            .build();
        preRenameLoadedDotRenamer = new ClassRenamerBuilder(NameStyle.DOT_NAME, false)
            .loadPreRenameUserDefinedClasses(preRenameDotNameUserClasses)
            .loadPreRenameJclExceptionClasses(preRenameDotNameExceptions)
            .build();
        preRenameLoadedSlashRenamerDebugModeEnabled = new ClassRenamerBuilder(NameStyle.SLASH_NAME, true)
            .loadPreRenameUserDefinedClasses(preRenameSlashNameUserClasses)
            .loadPreRenameJclExceptionClasses(preRenameSlashNameExceptions)
            .build();
        preRenameLoadedSlashRenamer = new ClassRenamerBuilder(NameStyle.SLASH_NAME, false)
            .loadPreRenameUserDefinedClasses(preRenameSlashNameUserClasses)
            .loadPreRenameJclExceptionClasses(preRenameSlashNameExceptions)
            .build();
    }

    /**
     * Causes the calling test to fail if we do not get the expected results:
     *
     * The renamed 'original' should be 'expected' using a normal renamer and should be 'expectedDebug'
     * using a debug renamer.
     *
     * This method calls both the pre-rename loaded renamers and post-rename loaded renamers.
     */
    private void checkRenamingSlashStyle(String original, String expected, String expectedDebug, ArrayType arrayType) {
        checkRenamingSlashStyleInternal(preRenameLoadedSlashRenamer, preRenameLoadedSlashRenamerDebugModeEnabled, original, expected, expectedDebug, arrayType);
        checkRenamingSlashStyleInternal(postRenameLoadedSlashRenamer, postRenameLoadedSlashRenamerDebugModeEnabled, original, expected, expectedDebug, arrayType);
    }

    /**
     * Causes the calling test to fail if we do not get the expected results:
     *
     * The renamed 'original' should be 'expected' using a normal renamer and should be 'expectedDebug'
     * using a debug renamer.
     *
     * This method calls both the pre-rename loaded renamers and post-rename loaded renamers.
     */
    private void checkRenamingDotStyle(String original, String expected, String expectedDebug, ArrayType arrayType) {
        checkRenamingDotStyle(preRenameLoadedDotRenamer, preRenameLoadedDotRenamerDebugModeEnabled, original, expected, expectedDebug, arrayType);
        checkRenamingDotStyle(postRenameLoadedDotRenamer, postRenameLoadedDotRenamerDebugModeEnabled, original, expected, expectedDebug, arrayType);
    }

    private void checkRenamingSlashStyleInternal(ClassRenamer renamer, ClassRenamer debugRenamer, String original, String expected, String expectedDebug, ArrayType arrayType) {
        String renamedArrayDebug = debugRenamer.toPostRename(original, arrayType);
        String renamedArray = renamer.toPostRename(original, arrayType);

        assertEquals(expectedDebug, renamedArrayDebug);
        assertEquals(expected, renamedArray);

        // Verify these rename back to the original array name.
        assertEquals(original, debugRenamer.toPreRename(renamedArrayDebug));
        assertEquals(original, renamer.toPreRename(renamedArray));
    }

    private void checkRenamingDotStyle(ClassRenamer renamer, ClassRenamer debugRenamer, String original, String expected, String expectedDebug, ArrayType arrayType) {
        String renamedArrayDebug = debugRenamer.toPostRename(original, arrayType);
        String renamedArray = renamer.toPostRename(original, arrayType);

        assertEquals(expectedDebug, renamedArrayDebug);
        assertEquals(expected, renamedArray);

        // Verify these rename back to the original array name.
        assertEquals(original, debugRenamer.toPreRename(renamedArrayDebug));
        assertEquals(original, renamer.toPreRename(renamedArray));
    }
}
