package org.aion.avm.core.stacktracking;

import org.aion.avm.internal.StackWatcher;

public class TestResource {

    public int depth = 0;
    public int upCounter = 0;

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
        int d1 = StackWatcher.getCurStackDepth();
        int s1 = StackWatcher.getCurStackSize();
        countDown(20);
        int d2 = StackWatcher.getCurStackDepth();
        int s2 = StackWatcher.getCurStackSize();
        return (d1 == d2) && (s1 == s2);
    }


    public boolean testLocalTryCatch(){
        int d1 = StackWatcher.getCurStackDepth();
        int s1 = StackWatcher.getCurStackSize();
        boolean b1 = false;
        boolean b2 = false;

        // System.out.println("entry");
        // System.out.println(d1);
        // System.out.println(s1);

        try{
            throwCDE();
        }catch (Exception e){
            int d2 = StackWatcher.getCurStackDepth();
            int s2 = StackWatcher.getCurStackSize();
            // System.out.println("catch");
            // System.out.println(d2);
            // System.out.println(s2);
            b1 = (d1 == d2) && (s1 == s2);
        }finally{
            int d3 = StackWatcher.getCurStackDepth();
            int s3 = StackWatcher.getCurStackSize();
            // System.out.println("finally");
            // System.out.println(d3);
            // System.out.println(s3);
            b2 = (d1 == d3) && (s1 == s3);
        }
        return b1 && b2;
    }

    public boolean testRemoteTryCatch(){
        int d1 = StackWatcher.getCurStackDepth();
        int s1 = StackWatcher.getCurStackSize();
        boolean b1 = false;
        boolean b2 = false;

        // System.out.println("entry");
        // System.out.println(d1);
        // System.out.println(s1);

        try {
            countDownWithException(20);
        }catch (Exception e){
            int d2 = StackWatcher.getCurStackDepth();
            int s2 = StackWatcher.getCurStackSize();

            // System.out.println("catch");
            // System.out.println(d2);
            // System.out.println(s2);

            b1 = (d1 == d2) && (s1 == s2);
        }finally{
            int d3 = StackWatcher.getCurStackDepth();
            int s3 = StackWatcher.getCurStackSize();

            // System.out.println("finally");
            // System.out.println(d3);
            // System.out.println(s3);

            b2 = (d1 == d3) && (s1 == s3);
        }

        return b1 && b2;
    }
}
