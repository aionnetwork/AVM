package testutils;

import org.aion.avm.internal.IABISupport;
import org.aion.avm.internal.IObject;

import org.junit.Assert;


/**
 * Used when testing the ABIEncoder.  This implementation just returns a normal ObjectArray, not the more specific
 * type.  This makes this factory sufficient when just interacting with the array but not when using it in
 * instrumented code (since the types won't map properly:  both byte[][] and int[][] map to ObjectArray).
 */
public class TestArrayWrapperFactory implements IABISupport {
    @Override
    public String convertToShadowMethodName(String original) {
        return original;
    }
    @Override
    public Object convertToStandardValue(Object privateValue) {
        return ((Wrapper)privateValue).real;
    }
    @Override
    public IObject convertToShadowValue(Object publicValue) {
        return new Wrapper(publicValue);
    }
    @Override
    public Class<?> convertConcreteShadowToStandardType(Class<?> privateType) {
        return privateType;
    }
    @Override
    public Class<?> convertToConcreteShadowType(Class<?> publicType) {
        return publicType;
    }
    @Override
    public Class<?> convertToBindingShadowType(Class<?> publicType) {
        return publicType;
    }
    @Override
    public Class<?> mapFromBindingTypeToConcreteType(Class<?> bindingShadowType) {
        return bindingShadowType;
    }


    private static class Wrapper implements IObject {
        public final Object real;
        public Wrapper(Object real) {
            this.real = real;
        }
        @Override
        public org.aion.avm.shadow.java.lang.Class<?> avm_getClass() {
            Assert.fail("Not used in test");
            return null;
        }
        @Override
        public int avm_hashCode() {
            Assert.fail("Not used in test");
            return 0;
        }
        @Override
        public boolean avm_equals(IObject obj) {
            Assert.fail("Not used in test");
            return false;
        }
        @Override
        public org.aion.avm.shadow.java.lang.String avm_toString() {
            Assert.fail("Not used in test");
            return null;
        }
    }
}
