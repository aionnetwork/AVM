package org.aion.avm.core.rejection;


/**
 * This is the rejection test which tests the various filtering capabilities of the system.
 * This means that it is completely valid but includes various extraneous things which we will "safely" strip out, on load,
 * and various other valid things which can pass right through.  We can then observe the before and after to verify that
 * this happened as we expected.
 * 
 * Data which will remain:
 * -static fields
 * -static methods
 * -instance fields
 * -instance methods
 * -inner classes
 * -internal and external inheritance
 * -checked exceptions
 * 
 * Data which will be removed:
 * -annotations
 * -line number data (optional - will only be tested if built)
 */
@Removable
public class FilteringResource implements Runnable {
    @Removable
    public static final String CONSTANT = "A STRING CONSTANT";
    
    @Removable
    public static Object buildOneInstance(@Removable String option) {
        return new FilteringResource(option);
    }
    
    
    public final @Removable String option;
    public @Removable int pretendState;
    
    @Removable
    public FilteringResource(@Removable String option) {
        this.option = option;
        this.pretendState = 42;
    }
    
    @Removable
    public Object getGluedResult(Class<?> input) throws B {
        @Removable String partial = this.option + this.pretendState;
        return partial + input;
    }
    
    @Override
    public void run() {
        this.pretendState += 1;
    }
    
    @Removable
    public static class A extends Exception {
        @Removable
        private static final long serialVersionUID = 1L;
    }
    
    
    public static class B extends A {
        private static final long serialVersionUID = 1L;
    }
}
