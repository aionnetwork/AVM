package org.aion.avm.core.classgeneration;


/**
 * Just a test class to be subclassed by the GenetatorTest.
 */
public class TestClass {
    private final Object contents;
    
    public TestClass(Object contents) {
        this.contents = contents;
    }
    
    public Object getContents() {
        return this.contents;
    }
}
