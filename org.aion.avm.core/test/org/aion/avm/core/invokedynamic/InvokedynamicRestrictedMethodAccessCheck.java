package org.aion.avm.core.invokedynamic;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.miscvisitors.NamespaceMapper;
import org.aion.avm.core.miscvisitors.PreRenameClassAccessRules;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.rejection.RejectedClassException;
import org.aion.avm.core.rejection.RejectionClassVisitor;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.invoke.*;
import java.lang.reflect.Member;
import java.util.Set;
import java.util.function.Predicate;

import static org.aion.avm.core.invokedynamic.InvokedynamicUtils.getSlashClassNameFrom;
import static org.aion.avm.core.util.Helpers.loadRequiredResourceAsBytes;

public class InvokedynamicRestrictedMethodAccessCheck {
    @Test
    public void given_userCallingToBootstrapMethods_then_dappRejected() {
        {
            final var notAllowedToCallMethod = "metafactory";
            final var methodOwner = "LambdaMetafactory";
            final var methodOwnerContainer = LambdaBootsrapMethodCall.class;
            final var expectedFailureCheck = new RejectedBecauseMethodNotInWhitelistForShadowedClass(methodOwner, notAllowedToCallMethod);
            final var userDefinedClassDotNames = new String[]{"org.aion.avm.core.invokedynamic.LambdaBootsrapMethodCall"};
            makeSureTransformationFail(methodOwnerContainer, expectedFailureCheck, userDefinedClassDotNames);
        }
        {
            final var notAllowedToCallMethod = "makeConcat";
            final var methodOwner = "StringConcatFactory";
            final var methodOwnerContainer = StringConcatFactoryMethodCall.class;
            final var expectedFailureCheck = new RejectedBecauseMethodNotInWhitelistForShadowedClass(methodOwner, notAllowedToCallMethod);
            final var userDefinedClassDotNames = new String[]{"org.aion.avm.core.invokedynamic.StringConcatFactoryMethodCall"};
            makeSureTransformationFail(methodOwnerContainer, expectedFailureCheck, userDefinedClassDotNames);
        }
    }

    @Test
    public void given_referenceToTheseClasses_then_dappRejected() {
        {
            final var notAllowedToReferClass = "java/lang/invoke/ConstantCallSite";
            final var notAllowedToReferClassContainer = ConstantCallSiteCall.class;
            final var expectedFailureCheck = new RejectedBecauseClassNotInWhitelist(notAllowedToReferClass);
            final var userDefinedClassNames = new String[]{
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck$ConstantCallSiteCall",
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck"
            };
            makeSureTransformationFail(notAllowedToReferClassContainer, expectedFailureCheck, userDefinedClassNames);
        }
        {
            final var notAllowedToReferClass = "java/lang/invoke/MutableCallSite";
            final var notAllowedToReferClassContainer = MutableCallSiteCall.class;
            final var expectedFailureCheck = new RejectedBecauseClassNotInWhitelist(notAllowedToReferClass);
            final var userDefinedClassNames = new String[]{
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck$MutableCallSiteCall",
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck"
            };
            makeSureTransformationFail(notAllowedToReferClassContainer, expectedFailureCheck, userDefinedClassNames);
        }
        {
            final var notAllowedToReferClass = "java/lang/invoke/VolatileCallSite";
            final var notAllowedToReferClassContainer = VolatileCallSiteCall.class;
            final var expectedFailureCheck = new RejectedBecauseClassNotInWhitelist(notAllowedToReferClass);
            final var userDefinedClassNames = new String[]{
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck$VolatileCallSiteCall",
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck"
            };
            makeSureTransformationFail(notAllowedToReferClassContainer, expectedFailureCheck, userDefinedClassNames);
        }
        {
            final var notAllowedToReferClass = "java/lang/invoke/LambdaConversionException";
            final var notAllowedToReferClassContainer = LambdaConversionExceptionCall.class;
            final var expectedFailureCheck = new RejectedBecauseClassNotInWhitelist(notAllowedToReferClass);
            final var userDefinedClassNames = new String[]{
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck$LambdaConversionExceptionCall",
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck"
            };
            makeSureTransformationFail(notAllowedToReferClassContainer, expectedFailureCheck, userDefinedClassNames);
        }
        {
            final var notAllowedToReferClass = "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck$MethodHandleInfoCall$1";
            final var notAllowedToReferClassContainer = MethodHandleInfoCall.class;
            final var expectedFailureCheck = new RejectedBecauseClassNotInWhitelist(notAllowedToReferClass);
            final var userDefinedClassNames = new String[]{
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck$MethodHandleInfoCall",
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck"
            };
            makeSureTransformationFail(notAllowedToReferClassContainer, expectedFailureCheck, userDefinedClassNames);
        }
        {
            final var notAllowedToReferClass = "java/lang/invoke/MethodHandleProxies";
            final var notAllowedToReferClassContainer = MethodHandleProxiesCall.class;
            final var expectedFailureCheck = new RejectedBecauseClassNotInWhitelist(notAllowedToReferClass);
            final var userDefinedClassNames = new String[]{
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck$MethodHandleProxiesCall",
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck"
            };
            makeSureTransformationFail(notAllowedToReferClassContainer, expectedFailureCheck, userDefinedClassNames);
        }
        {
            final var notAllowedToReferClass = "java/lang/invoke/SerializedLambda";
            final var notAllowedToReferClassContainer = SerializedLambdaCall.class;
            final var expectedFailureCheck = new RejectedBecauseClassNotInWhitelist(notAllowedToReferClass);
            final var userDefinedClassNames = new String[]{
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck$SerializedLambdaCall",
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck"
            };
            makeSureTransformationFail(notAllowedToReferClassContainer, expectedFailureCheck, userDefinedClassNames);
        }
        {
            final var notAllowedToReferClass = "java/lang/invoke/SwitchPoint";
            final var notAllowedToReferClassContainer = SwitchPointCall.class;
            final var expectedFailureCheck = new RejectedBecauseClassNotInWhitelist(notAllowedToReferClass);
            final var userDefinedClassNames = new String[]{
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck$SwitchPointCall",
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck"
            };
            makeSureTransformationFail(notAllowedToReferClassContainer, expectedFailureCheck, userDefinedClassNames);
        }
        {
            final var notAllowedToReferClass = "java/lang/invoke/WrongMethodTypeException";
            final var notAllowedToReferClassContainer = WrongMethodTypeExceptionCall.class;
            final var expectedFailureCheck = new RejectedBecauseClassNotInWhitelist(notAllowedToReferClass);
            final var userDefinedClassNames = new String[]{
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck$WrongMethodTypeExceptionCall",
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck"
            };
            makeSureTransformationFail(notAllowedToReferClassContainer, expectedFailureCheck, userDefinedClassNames);
        }
        {
            final var methodOwner = "java/lang/invoke/VarHandle";
            final var methodOwnerContainer = VarHandleCall.class;
            final var expectedFailureCheck = new RejectedBecauseClassNotInWhitelist(methodOwner);
            final var userDefinedClassDotNames = new String[]{
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck$VarHandleCall",
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck"
            };
            makeSureTransformationFail(methodOwnerContainer, expectedFailureCheck, userDefinedClassDotNames);
        }
        {
            final var methodOwner = "java/lang/invoke/WrongMethodTypeException";
            final var methodOwnerContainer = WrongMethodTypeExceptionCall.class;
            final var expectedFailureCheck = new RejectedBecauseClassNotInWhitelist(methodOwner);
            final var userDefinedClassDotNames = new String[]{
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck$WrongMethodTypeExceptionCall",
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck"
            };
            makeSureTransformationFail(methodOwnerContainer, expectedFailureCheck, userDefinedClassDotNames);
        }
        {
            final var methodOwner = "java/lang/invoke/StringConcatException";
            final var methodOwnerContainer = StringConcatExceptionCall.class;
            final var expectedFailureCheck = new RejectedBecauseClassNotInWhitelist(methodOwner);
            final var userDefinedClassDotNames = new String[]{
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck$StringConcatExceptionCall",
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck"
            };
            makeSureTransformationFail(methodOwnerContainer, expectedFailureCheck, userDefinedClassDotNames);
        }
    }

    @Test
    public void given_methodCallOnTheseClasses_then_dappRejected() {
        {
            final var weExpectAvmToShadowMethodCallTargetClass = "org.aion.avm.shadow.java.lang.invoke.MethodHandles$Lookup";
            final var methodCallContainer = LookupCall.class;
            final var expectedFailureCheck = new RejectedBecauseMethodCallOnWhitelistedButNotShadowedClass(weExpectAvmToShadowMethodCallTargetClass);
            final var userDefinedClassNames = new String[]{
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck$LookupCall",
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck"
            };
            makeSureTransformationFail(methodCallContainer, expectedFailureCheck, userDefinedClassNames);
        }
        {
            final var weExpectAvmToShadowMethodCallTargetClass = "org.aion.avm.shadow.java.lang.invoke.MethodType";
            final var methodCallContainer = MethodTypeCall.class;
            final var expectedFailureCheck = new RejectedBecauseMethodCallOnWhitelistedButNotShadowedClass(weExpectAvmToShadowMethodCallTargetClass);
            final var userDefinedClassNames = new String[]{
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck$MethodTypeCall",
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck"
            };
            makeSureTransformationFail(methodCallContainer, expectedFailureCheck, userDefinedClassNames);
        }
        {
            final var weExpectAvmToShadowMethodCallTargetClass = "org.aion.avm.shadow.java.lang.invoke.MethodHandles";
            final var targetClassContainer = MethodHandlesCall.class;
            final var expectedFailureCheck = new RejectedBecauseMethodCallOnWhitelistedButNotShadowedClass(weExpectAvmToShadowMethodCallTargetClass);
            final var userDefinedClassDotNames = new String[]{
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck$MethodHandlesCall",
                    "org/aion/avm/core/invokedynamic/InvokedynamicRestrictedMethodAccessCheck"
            };
            makeSureTransformationFail(targetClassContainer, expectedFailureCheck, userDefinedClassDotNames);
        }
    }

    private void makeSureTransformationFail(Class<?> classToTransform,
                                            Predicate<? super Exception> expectedFailureCheck,
                                            String... userDefinedClassDotNames) {
        final var testClassDotName = classToTransform.getName();
        final var slashClassName = getSlashClassNameFrom(testClassDotName);
        final var originalBytecode = loadRequiredResourceAsBytes(slashClassName);
        try {
            transformBytecode(originalBytecode, userDefinedClassDotNames);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(expectedFailureCheck.test(e));
        }
    }

    private static void transformBytecode(byte[] originalBytecode, String... userDefinedClassDotNames) {
        final var setOfClassNames = Set.of(userDefinedClassDotNames);
        final var accessRules = new PreRenameClassAccessRules(setOfClassNames, setOfClassNames);
        final var namespaceMapper = new NamespaceMapper(accessRules);
        new ClassToolchain.Builder(originalBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new RejectionClassVisitor(accessRules, namespaceMapper))
                .addNextVisitor(new UserClassMappingVisitor(namespaceMapper))
                .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                .build()
                .runAndGetBytecode();
    }

    private static class ConstantCallSiteCall {
        private ConstantCallSiteCall() {
            new ConstantCallSite(null);
        }
    }

    private static class MutableCallSiteCall {
        private MutableCallSiteCall() {
            new MutableCallSite((MethodType) null);
        }
    }

    private static class VolatileCallSiteCall {
        private VolatileCallSiteCall() {
            new VolatileCallSite((MethodType) null);
        }
    }

    private static class LambdaConversionExceptionCall {
        private LambdaConversionExceptionCall() {
            new LambdaConversionException();
        }
    }

    private static class MethodHandleInfoCall {
        private MethodHandleInfoCall() {
            new MethodHandleInfo() {
                @Override
                public int getReferenceKind() {
                    return 0;
                }

                @Override
                public Class<?> getDeclaringClass() {
                    return null;
                }

                @Override
                public String getName() {
                    return null;
                }

                @Override
                public MethodType getMethodType() {
                    return null;
                }

                @Override
                public <T extends Member> T reflectAs(Class<T> expected, MethodHandles.Lookup lookup) {
                    return null;
                }

                @Override
                public int getModifiers() {
                    return 0;
                }
            };
        }
    }

    private static class MethodHandleProxiesCall {
        private MethodHandleProxiesCall() {
            MethodHandleProxies.isWrapperInstance(null);
        }
    }

    private static class SerializedLambdaCall {
        private SerializedLambdaCall() {
            new SerializedLambda(null, null, null
                    , null, 0, null, null
                    , null, null, null);
        }
    }

    private static class SwitchPointCall {
        private SwitchPointCall() {
            new SwitchPoint();
        }
    }

    private static class LookupCall {
        private LookupCall(MethodHandles.Lookup lookup) {
            try {
                lookup.defineClass(null);
            } catch (Exception e) {
            }
        }
    }

    private static class VarHandleCall {
        private VarHandleCall() {
            Class clazz = VarHandle.class;
        }
    }

    private static class WrongMethodTypeExceptionCall {
        private WrongMethodTypeExceptionCall() {
            new WrongMethodTypeException();
        }
    }

    private static class MethodTypeCall {
        private MethodTypeCall() {
            MethodType.methodType(null);
        }
    }

    private static class StringConcatExceptionCall {
        private StringConcatExceptionCall() {
            new StringConcatException(null);
        }
    }

    private static class MethodHandlesCall {
        private MethodHandlesCall() {
            MethodHandles.publicLookup();
        }
    }

    private abstract static class ClassCheck implements Predicate<Exception> {
        protected final String className;

        private ClassCheck(String className) {
            this.className = className;
        }
    }

    private static class RejectedBecauseClassNotInWhitelist extends ClassCheck {
        private RejectedBecauseClassNotInWhitelist(String className) {
            super(className);
        }

        @Override
        public boolean test(Exception e) {
            return e instanceof RejectedClassException && e.getMessage().contains(className);
        }
    }

    private static class RejectedBecauseMethodCallOnWhitelistedButNotShadowedClass extends ClassCheck {
        private RejectedBecauseMethodCallOnWhitelistedButNotShadowedClass(String className) {
            super(className);
        }

        @Override
        public boolean test(Exception e) {
            return e.getCause().getMessage().contains(className);
        }
    }

    private static class RejectedBecauseMethodNotInWhitelistForShadowedClass extends ClassCheck {
        private final String methodName;

        private RejectedBecauseMethodNotInWhitelistForShadowedClass(String className, String methodName) {
            super(className);
            this.methodName = methodName;
        }

        @Override
        public boolean test(Exception e) {
            final var msg = e.getMessage();
            return e instanceof RejectedClassException
                    && msg.contains("missing method")
                    && msg.contains(className + "#" + methodName);
        }
    }
}