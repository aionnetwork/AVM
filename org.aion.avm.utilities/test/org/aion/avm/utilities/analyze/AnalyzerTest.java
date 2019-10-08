package org.aion.avm.utilities.analyze;

import org.aion.avm.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

public class AnalyzerTest {

    @Test
    public void analyze() {
        byte[] classBytes = Utilities.loadRequiredResourceAsBytes(Utilities.fulllyQualifiedNameToInternalName(ExampleClass.class.getName())+".class");
        ClassFileInfoBuilder.ClassFileInfo info = ClassFileInfoBuilder.getClassFileInfo(classBytes);
        Assert.assertEquals(info.constantTypeCount.size(), 14);
    }

}
