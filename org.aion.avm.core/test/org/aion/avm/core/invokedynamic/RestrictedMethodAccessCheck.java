package org.aion.avm.core.invokedynamic;

import org.aion.avm.core.ClassToolchain;
import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.rejection.RejectedClassException;
import org.aion.avm.core.rejection.RejectionClassVisitor;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.util.Set;

import static org.aion.avm.core.invokedynamic.InvokedynamicUtils.getSlashClassNameFrom;
import static org.aion.avm.core.util.Helpers.loadRequiredResourceAsBytes;

// todo unify with other indy unit test
/**
 * Checks that transformation step rejects invokedynamic-related cases where
 * (1) it is allowed to refer to class X (2) but not allowed to call a method on it X.foo()
 */
public class RestrictedMethodAccessCheck {
    @Test
    public void given_userCallingToBootstrapMethods_then_dappRejected() {
//        makeSureTransformationFailOn(LambdaBootsrapMethodCall.class);
//        makeSureTransformationFailOn(StringConcatFactoryMethodCall.class);
    }

    private void makeSureTransformationFailOn(Class<?> clazz) {
        final var testClassDotName = clazz.getName();
        final var slashClassName = getSlashClassNameFrom(testClassDotName);
        final var originalBytecode = loadRequiredResourceAsBytes(slashClassName);
        try {
            transformBytecode(originalBytecode, testClassDotName);
            Assert.fail();
        } catch (RejectedClassException e) {
            e.printStackTrace();
//            final var message = e.getMessage();
//            final var rejectedClassName = extractRejectedClassNameFromWrapperName(slashClassName);
//            Assert.assertTrue("message: '" + message + "' must contain: " + rejectedClassName, message.contains(rejectedClassName));
        }
    }

    private static void transformBytecode(byte[] originalBytecode, String classDotName) {
        final var userDefinedClassDotNames = Set.of("java.lang.Object"
                , "org.aion.avm.core.invokedynamic.Lambda"
                , "org.aion.avm.core.invokedynamic.LambdaBootsrapMethodCall");
        new ClassToolchain.Builder(originalBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new RejectionClassVisitor(userDefinedClassDotNames))
                .addNextVisitor(new UserClassMappingVisitor(userDefinedClassDotNames))
                .addWriter(new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS))
                .build()
                .runAndGetBytecode();
    }
}