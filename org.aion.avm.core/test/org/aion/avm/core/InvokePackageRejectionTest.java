package org.aion.avm.core;

import org.aion.avm.core.miscvisitors.UserClassMappingVisitor;
import org.aion.avm.core.rejection.RejectedClassException;
import org.aion.avm.core.types.ClassInfo;
import org.aion.avm.core.types.Forest;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.invoke.*;
import java.lang.reflect.Member;
import java.util.Collections;

import static org.aion.avm.core.InvokedynamicTransformationTest.getSlashClassNameFrom;
import static org.aion.avm.core.util.Helpers.loadRequiredResourceAsBytes;

public class InvokePackageRejectionTest {
    @Test
    public void given_InvokePackageRejectedClass_then_ExceptionShouldBeThrown() {
        tryTransformClass(WrongMethodTypeExceptionCall.class);
        tryTransformClass(VarHandleCall.class);
        tryTransformClass(SwitchPointCall.class);
        tryTransformClass(SerializedLambdaCall.class);
        tryTransformClass(MethodHandleProxiesCall.class);
        tryTransformClass(MethodHandleInfoCall.class);
        tryTransformClass(LambdaConversionExceptionCall.class);
        tryTransformClass(VolatileCallSiteCall.class);
        tryTransformClass(MutableCallSiteCall.class);
        tryTransformClass(ConstantCallSiteCall.class);
        // todo it is not allowed for a user to call anything on the Types below. Only reference them
//        tryTransformClass(MethodHandlesCall.class);
//        tryTransformClass(CallSiteCall.class);
//        tryTransformClass(LambdaMetafactoryCall.class);
//        tryTransformClass(StringConcatFactoryCall.class);
//        tryTransformClass(StringConcatExceptionCall.class);
//        tryTransformClass(MethodTypeCall.class);
    }

    private void tryTransformClass(Class<?> clazz) {
        final var testClassDotName = clazz.getName();
        final var slashClassName = getSlashClassNameFrom(testClassDotName);
        final var originalBytecode = loadRequiredResourceAsBytes(slashClassName);
        try {
            transformBytecode(originalBytecode, testClassDotName);
            Assert.fail();
        } catch (RejectedClassException e) {
            final var message = e.getMessage();
            final var rejectedClassName = extractRejectedClassNameFromWrapperName(slashClassName);
            Assert.assertTrue("message: '" + message + "' must contain: " + rejectedClassName, message.contains(rejectedClassName));
        }
    }

    private static String extractRejectedClassNameFromWrapperName(String slashWrapperName) {
        final var fromIndexInclusive = (InvokePackageRejectionTest.class.getName() + "$").length();
        final var toIndexExclusive = slashWrapperName.length() - "Call.class".length();
        return slashWrapperName.substring(fromIndexInclusive, toIndexExclusive);
    }

    private void transformBytecode(byte[] originalBytecode, String classDotName) {
        final Forest<String, ClassInfo> classHierarchy = new HierarchyTreeBuilder()
                .addClass(classDotName, "java.lang.Object", false, originalBytecode)
                .addClass("org.aion.avm.core.InvokePackageRejectionTest", "java.lang.Object", false, originalBytecode)
                .addClass("java.lang.invoke.StringConcatException", "java.lang.Object", false, originalBytecode)
                .asMutableForest();
        new ClassToolchain.Builder(originalBytecode, ClassReader.EXPAND_FRAMES)
                .addNextVisitor(new UserClassMappingVisitor(ClassWhiteList.extractDeclaredClasses(classHierarchy)))
                .addWriter(new TypeAwareClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS,
                        new ParentPointers(Collections.singleton(classDotName), classHierarchy),
                        new HierarchyTreeBuilder()))
                .build()
                .runAndGetBytecode();
    }

    private static class WrongMethodTypeExceptionCall {
        private WrongMethodTypeExceptionCall() {
            new WrongMethodTypeException();
        }
    }

    private static class VarHandleCall {
        private VarHandleCall() {
            Class clazz = VarHandle.class;
        }
    }

    private static class SwitchPointCall {
        private SwitchPointCall() {
            new SwitchPoint();
        }
    }

    private static class SerializedLambdaCall {
        private SerializedLambdaCall() {
            new SerializedLambda(null, null, null
                    , null, 0, null, null
                    , null, null, null);
        }
    }

    private static class MethodHandleProxiesCall {
        private MethodHandleProxiesCall() {
            MethodHandleProxies.isWrapperInstance(null);
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

    private static class LambdaConversionExceptionCall {
        private LambdaConversionExceptionCall() {
            new LambdaConversionException();
        }
    }

    private static class VolatileCallSiteCall {
        private VolatileCallSiteCall() {
            new VolatileCallSite((MethodType) null);
        }
    }

    private static class MutableCallSiteCall {
        private MutableCallSiteCall() {
            new MutableCallSite((MethodType) null);
        }
    }

    private static class ConstantCallSiteCall {
        private ConstantCallSiteCall() {
            new ConstantCallSite(null);
        }
    }

    private static class MethodTypeCall {
        private MethodTypeCall() {
            MethodType.methodType(null);
        }
    }

    private static class CallSiteCall {
        private CallSiteCall() {
            Class clazz = CallSite.class;
        }
    }

    private static class LambdaMetafactoryCall {
        private LambdaMetafactoryCall() {
            Class clazz = LambdaMetafactory.class;
        }
    }

    private static class StringConcatFactoryCall {
        private StringConcatFactoryCall() {
            Class clazz = StringConcatFactory.class;
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

}