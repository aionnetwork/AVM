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
import org.aion.avm.internal.PackageConstants;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link PlainTypeSuperResolver} and {@link TypeAwareClassWriter}!
 *
 * These two classes should give identical responses to each query, and so this is checked for each
 * test case.
 */
public final class PlainTypeSuperResolverTest {
    private static boolean preserveDebuggability = false;
    private ClassRenamer classRenamer;
    private PlainTypeSuperResolver resolver;
    private TypeAwareClassWriter typeAwareClassWriter;

    private String preRenameArray = byte[].class.getName();
    private String preRenamePlainType = java.lang.System.class.getName();

    private String postRenamePlainType;
    private String postRenamePrimitiveArray = org.aion.avm.arraywrapper.FloatArray.class.getName();
    private String postRenameConcreteTypeArray;
    private String postRenameUnifyingTypeArray;
    private String exceptionWrapper;

    @Before
    public void setup() {
        ClassHierarchy hierarchy = new ClassHierarchyBuilder()
            .addShadowJcl()
            .addPostRenameJclExceptions()
            .addHandwrittenArrayWrappers()
            .build();
        this.classRenamer = new ClassRenamerBuilder(NameStyle.DOT_NAME, preserveDebuggability)
            .loadPreRenameJclExceptionClasses(fetchPreRenameSlashStyleJclExceptions())
            .build();
        this.resolver = new PlainTypeSuperResolver(hierarchy, this.classRenamer);

        this.typeAwareClassWriter = new ClassWriter(hierarchy, this.classRenamer);

        String exception = this.classRenamer.toPostRename(java.lang.NullPointerException.class.getName(), ArrayType.PRECISE_TYPE);
        this.exceptionWrapper = this.classRenamer.toExceptionWrapper(exception);
        this.postRenamePlainType = this.classRenamer.toPostRename(this.preRenamePlainType, ArrayType.PRECISE_TYPE);
        this.postRenameConcreteTypeArray = ArrayRenamer.wrapAsConcreteObjectArray(NameStyle.DOT_NAME, this.postRenamePlainType, 1);
        this.postRenameUnifyingTypeArray = ArrayRenamer.wrapAsUnifyingObjectArray(NameStyle.DOT_NAME, this.postRenamePlainType, 1);
    }

    @Test
    public void testSuperOfTwoNonPlainTypes() {
        String class1 = java.lang.String[].class.getName();

        // Strip trailing ';' character.
        String array = class1.substring(0, class1.length() - 1);

        assertNull(this.resolver.getTightestSuperClassIfGivenPlainType(array, this.exceptionWrapper));
    }

    @Test
    public void testSuperOfPreRenamePlainTypeAndPostRenameOther() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(this.preRenamePlainType, this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName, commonSuper);

        // --------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.preRenamePlainType, this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfPostRenamePlainTypeAndPreRenameOther() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(this.postRenamePlainType, this.preRenameArray);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName, commonSuper);

        // --------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenamePlainType, this.preRenameArray);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfPreRenamePlainTypeAndPreRenameOther() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(this.preRenamePlainType, this.preRenameArray);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName, commonSuper);

        // --------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.preRenamePlainType, this.preRenameArray);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfPostRenamePlainTypeAndExceptionWrapper() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(this.postRenamePlainType, this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName, commonSuper);

        // --------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenamePlainType, this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfPostRenamePlainTypeAndPostRenamePrimitiveArray() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(this.postRenamePlainType, this.postRenamePrimitiveArray);
        assertEquals(CommonType.SHADOW_OBJECT.dotName, commonSuper);

        // --------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenamePlainType, this.postRenamePrimitiveArray);
        assertEquals(CommonType.SHADOW_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfPostRenamePlainTypeAndPostRenameConcreteArrayType() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(this.postRenamePlainType, this.postRenameConcreteTypeArray);
        assertEquals(CommonType.SHADOW_OBJECT.dotName, commonSuper);

        // --------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenamePlainType, this.postRenameConcreteTypeArray);
        assertEquals(CommonType.SHADOW_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfPostRenamePlainTypeAndPostRenameUnifyingArrayType() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(this.postRenamePlainType, this.postRenameUnifyingTypeArray);
        assertEquals(CommonType.I_OBJECT.dotName, commonSuper);

        // --------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(this.postRenamePlainType, this.postRenameUnifyingTypeArray);
        assertEquals(CommonType.I_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfTwoPreRenamePlainTypes() {
        String plain1 = java.lang.Object.class.getName();
        String plain2 = java.lang.NullPointerException.class.getName();
        String plain3 = java.lang.IllegalArgumentException.class.getName();
        String plain4 = java.lang.Integer.class.getName();
        String plain5 = java.lang.Byte.class.getName();
        String exceptionSuper = java.lang.RuntimeException.class.getName();

        String commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(plain1, plain2);
        assertEquals(plain1, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(plain2, plain3);
        assertEquals(exceptionSuper, commonSuper);

        // The tightest super is ambiguous in this case, so java.lang.Object is returned.
        commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(plain4, plain5);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName, commonSuper);

        // ---------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(plain1, plain2);
        assertEquals(plain1.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(plain2, plain3);
        assertEquals(exceptionSuper.replaceAll("\\.", "/"), commonSuper);

        // The tightest super is ambiguous in this case, so java.lang.Object is returned.
        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(plain4, plain5);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfTwoPostRenamePlainTypes() {
        String preRenameConcreteClass1 = java.lang.OutOfMemoryError.class.getName();
        String preRenameConcreteClass2 = java.lang.StackOverflowError.class.getName();
        String preRenameInterface1 = java.io.Serializable.class.getName();
        String preRenameInterface2 = java.lang.CharSequence.class.getName();

        String concreteSuper = java.lang.VirtualMachineError.class.getName();

        String concreteClass1 = this.classRenamer.toPostRename(preRenameConcreteClass1, ArrayType.PRECISE_TYPE);
        String concreteClass2 = this.classRenamer.toPostRename(preRenameConcreteClass2, ArrayType.PRECISE_TYPE);
        String interface1 = this.classRenamer.toPostRename(preRenameInterface1, ArrayType.PRECISE_TYPE);
        String interface2 = this.classRenamer.toPostRename(preRenameInterface2, ArrayType.PRECISE_TYPE);

        String concreteSuperRenamed = this.classRenamer.toPostRename(concreteSuper, ArrayType.PRECISE_TYPE);

        // Try out all 3 permutations of concrete class & interface.
        String commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(concreteClass1, concreteClass2);
        assertEquals(concreteSuperRenamed, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(concreteClass1, interface1);
        assertEquals(interface1, commonSuper);

        commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(interface1, interface2);
        assertEquals(CommonType.I_OBJECT.dotName, commonSuper);

        // --------------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(concreteClass1, concreteClass2);
        assertEquals(concreteSuperRenamed.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(concreteClass1, interface1);
        assertEquals(interface1.replaceAll("\\.", "/"), commonSuper);

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(interface1, interface2);
        assertEquals(CommonType.I_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfPreRenamePlainTypeAndJavaLangObject() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(java.lang.Byte.class.getName(), java.lang.Object.class.getName());
        assertEquals(java.lang.Object.class.getName(), commonSuper);

        // --------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(java.lang.Byte.class.getName(), java.lang.Object.class.getName());
        assertEquals(java.lang.Object.class.getName().replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfPostRenamePlainTypeAndJavaLangObject() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(s.java.lang.Byte.class.getName(), java.lang.Object.class.getName());
        assertEquals(java.lang.Object.class.getName(), commonSuper);

        // --------------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(s.java.lang.Byte.class.getName(), java.lang.Object.class.getName());
        assertEquals(java.lang.Object.class.getName().replaceAll("\\.", "/"), commonSuper);
    }

    private Set<String> fetchPreRenameSlashStyleJclExceptions() {
        Set<String> jclExceptions = new HashSet<>();
        for (CommonType type : CommonType.values()) {
            if (type.isShadowException) {
                jclExceptions.add(type.dotName.substring(PackageConstants.kShadowDotPrefix.length()).replaceAll("/", "\\."));
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
