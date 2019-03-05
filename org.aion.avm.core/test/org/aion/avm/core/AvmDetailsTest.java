package org.aion.avm.core;

import org.aion.avm.core.util.AvmDetails;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IPersistenceToken;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class AvmDetailsTest {
    @Test
    public void checkWhitelistMethods() throws ClassNotFoundException {
        Map<Class<?>, List<AvmDetails.MethodDescriptor>> methods = AvmDetails.getClassLibraryWhiteList();
        AvmDetails.MethodDescriptor descriptor;
        // primitive param type
        descriptor = new AvmDetails.MethodDescriptor("valueOf", new Class<?>[]{long.class}, true);
        Assert.assertTrue(methods.get(BigInteger.class).contains(descriptor));

        // Array param type
        descriptor = new AvmDetails.MethodDescriptor("valueOf", new Class<?>[]{char[].class}, true);
        Assert.assertTrue(methods.get(String.class).contains(descriptor));

        //IObject param type
        descriptor = new AvmDetails.MethodDescriptor("equals", new Class<?>[]{Object.class}, false);
        Assert.assertTrue(methods.get(Boolean.class).contains(descriptor));

        //constructor
        descriptor = new AvmDetails.MethodDescriptor("<init>", new Class<?>[]{String.class}, false);
        Assert.assertTrue(methods.get(BigInteger.class).contains(descriptor));

        //shadow class as input should fail
        descriptor = new AvmDetails.MethodDescriptor("<init>", new Class<?>[]{org.aion.avm.shadow.java.lang.String.class}, false);
        Assert.assertFalse(methods.get(BigInteger.class).contains(descriptor));

        //java lang class as input should fail
        descriptor = new AvmDetails.MethodDescriptor("<init>", new Class<?>[]{Class.class}, false);
        Assert.assertFalse(methods.get(Class.class).contains(descriptor));

        //internal class as input should fail
        descriptor = new AvmDetails.MethodDescriptor("<init>", new Class<?>[]{IDeserializer.class, IPersistenceToken.class}, false);
        Assert.assertFalse(methods.get(BigInteger.class).contains(descriptor));
    }

}
