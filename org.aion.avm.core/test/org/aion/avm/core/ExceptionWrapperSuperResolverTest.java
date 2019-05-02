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
import i.PackageConstants;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link ExceptionWrapperSuperResolver} and {@link TypeAwareClassWriter}!
 *
 * These two classes should give identical responses to each query, and so this is checked for each
 * test case.
 */
public class ExceptionWrapperSuperResolverTest {
    private static boolean preserveDebuggability = false;
    private ClassRenamer classRenamer;
    private ExceptionWrapperSuperResolver resolver;
    private TypeAwareClassWriter typeAwareClassWriter;

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
        this.resolver = new ExceptionWrapperSuperResolver(hierarchy, this.classRenamer);

        this.typeAwareClassWriter = new ClassWriter(hierarchy, this.classRenamer);

        String exception = s.java.lang.EnumConstantNotPresentException.class.getName();
        this.exceptionWrapper = this.classRenamer.toExceptionWrapper(exception);
    }

    @Test
    public void testSuperOfTwoNonExceptionWrappers() {
        assertNull(this.resolver.getTightestSuperClassIfGivenPlainType(java.lang.Byte.class.getName(), java.lang.Integer.class.getName()));
    }

    @Test
    public void testSuperOfExceptionWrapperAndPreRenameArray() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(int[].class.getName(), this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName, commonSuper);

        // -------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(int[].class.getName(), this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfExceptionWrapperAndPostRenameArray() {
        String array = s.java.lang.Object[].class.getName();
        array = array.substring(0, array.length() - 1); // strip trailing ';' character.

        String renamedArray = ArrayRenamer.wrapAsConcreteObjectArray(NameStyle.DOT_NAME, array, 3);

        String commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(renamedArray, this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName, commonSuper);

        // -------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(renamedArray, this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfExceptionWrapperAndPreRenamePlainType() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(java.math.BigInteger.class.getName(), this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName, commonSuper);

        // -------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(java.math.BigInteger.class.getName(), this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfExceptionWrapperAndPostRenamePlainType() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(s.java.lang.Throwable.class.getName(), this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName, commonSuper);

        // -------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(s.java.lang.Throwable.class.getName(), this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfExceptionWrapperAndJavaLangThrowable() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(CommonType.JAVA_LANG_THROWABLE.dotName, this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_THROWABLE.dotName, commonSuper);

        // -------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(CommonType.JAVA_LANG_THROWABLE.dotName, this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_THROWABLE.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfExceptionWrapperAndPreRenameException() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(java.lang.IllegalStateException.class.getName(), this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_THROWABLE.dotName, commonSuper);

        // -------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(java.lang.IllegalStateException.class.getName(), this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_THROWABLE.dotName.replaceAll("\\.", "/"), commonSuper);
    }

    @Test
    public void testSuperOfTwoExceptionWrappers() {
        String exception1 = java.lang.NullPointerException.class.getName();
        String exception2 = java.lang.IllegalArgumentException.class.getName();
        String superException = java.lang.RuntimeException.class.getName();

        String renamedException1 = this.classRenamer.toPostRename(exception1, ArrayType.PRECISE_TYPE);
        String renamedException2 = this.classRenamer.toPostRename(exception2, ArrayType.PRECISE_TYPE);
        String renamedSuper = this.classRenamer.toPostRename(superException, ArrayType.PRECISE_TYPE);

        String wrapper1 = this.classRenamer.toExceptionWrapper(renamedException1);
        String wrapper2 = this.classRenamer.toExceptionWrapper(renamedException2);
        String superWrapper = this.classRenamer.toExceptionWrapper(renamedSuper);

        assertEquals(superWrapper, this.resolver.getTightestSuperClassIfGivenPlainType(wrapper1, wrapper2));

        // -------------------------

        assertEquals(superWrapper.replaceAll("\\.", "/"), this.typeAwareClassWriter.getCommonSuperClass(wrapper1, wrapper2));
    }

    @Test
    public void testSuperOfExceptionWrapperAndJavaLangObject() {
        String commonSuper = this.resolver.getTightestSuperClassIfGivenPlainType(CommonType.JAVA_LANG_OBJECT.dotName, this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName, commonSuper);

        // -------------------------

        commonSuper = this.typeAwareClassWriter.getCommonSuperClass(CommonType.JAVA_LANG_OBJECT.dotName, this.exceptionWrapper);
        assertEquals(CommonType.JAVA_LANG_OBJECT.dotName.replaceAll("\\.", "/"), commonSuper);
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
