package org.aion.avm.core.stacktracking;

import i.StackWatcher;


public class TestResource {
    // We can't directly rely on the "Helpers" class name since a different name is used in the instrumentation.
    public static StackWatcher EXTERNAL_STACK_WATCHER;

    public int depth = 0;
    public int upCounter = 0;

    public TestResource(){
        super();
    }

    private void countDown(int i){
        if (i > 0){
            countDown(i - 1);
        }else{
            return;
        }
    }

    private void countDownWithException(int i) throws Exception {
        if (i > 0){
            countDownWithException(i - 1);
        }else{
            throw new Exception("CDE");
        }
    }

    private void countUp(){
        this.upCounter++;
        countUp();
    }

    private void throwCDE() throws Exception{
        throw new Exception("CDE");
    }


    public void testStackOverflow(){
        testStackOverflow();
    }

    public void testStackOverflowConsistency()
    {
        countUp();
    }

    public boolean testStackTrackingConsistency(){
        int d1 = EXTERNAL_STACK_WATCHER.getCurStackDepth();
        int s1 = EXTERNAL_STACK_WATCHER.getCurStackSize();
        countDown(20);
        int d2 = EXTERNAL_STACK_WATCHER.getCurStackDepth();
        int s2 = EXTERNAL_STACK_WATCHER.getCurStackSize();
        return (d1 == d2) && (s1 == s2);
    }


    public boolean testLocalTryCatch(){
        int d1 = EXTERNAL_STACK_WATCHER.getCurStackDepth();
        int s1 = EXTERNAL_STACK_WATCHER.getCurStackSize();
        boolean b1 = false;
        boolean b2 = false;

        // System.out.println("entry");
        // System.out.println(d1);
        // System.out.println(s1);

        try{
            throwCDE();
        }catch (Exception e){
            int d2 = EXTERNAL_STACK_WATCHER.getCurStackDepth();
            int s2 = EXTERNAL_STACK_WATCHER.getCurStackSize();
            // System.out.println("catch");
            // System.out.println(d2);
            // System.out.println(s2);
            b1 = (d1 == d2) && (s1 == s2);
        }finally{
            int d3 = EXTERNAL_STACK_WATCHER.getCurStackDepth();
            int s3 = EXTERNAL_STACK_WATCHER.getCurStackSize();
            // System.out.println("finally");
            // System.out.println(d3);
            // System.out.println(s3);
            b2 = (d1 == d3) && (s1 == s3);
        }
        return b1 && b2;
    }

    public boolean testRemoteTryCatch(){
        int d1 = EXTERNAL_STACK_WATCHER.getCurStackDepth();
        int s1 = EXTERNAL_STACK_WATCHER.getCurStackSize();
        boolean b1 = false;
        boolean b2 = false;

        // System.out.println("entry");
        // System.out.println(d1);
        // System.out.println(s1);

        try {
            countDownWithException(20);
        }catch (Exception e){
            int d2 = EXTERNAL_STACK_WATCHER.getCurStackDepth();
            int s2 = EXTERNAL_STACK_WATCHER.getCurStackSize();

            // System.out.println("catch");
            // System.out.println(d2);
            // System.out.println(s2);

            b1 = (d1 == d2) && (s1 == s2);
        }finally{
            int d3 = EXTERNAL_STACK_WATCHER.getCurStackDepth();
            int s3 = EXTERNAL_STACK_WATCHER.getCurStackSize();

            // System.out.println("finally");
            // System.out.println(d3);
            // System.out.println(s3);

            b2 = (d1 == d3) && (s1 == s3);
        }

        return b1 && b2;
    }
}
