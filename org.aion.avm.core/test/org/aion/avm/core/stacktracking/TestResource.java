package org.aion.avm.core.stacktracking;

public class TestResource {

    int depth = 0;

    public static void main(String[] args) {
        AVMStackWatcher.setPolicy(AVMStackWatcher.POLICY_DEPTH);
        AVMStackWatcher.setMaxStackDepth(50);
        TestResource tb = new TestResource();
        try{
            tb.deep();
        }catch (Exception e){
            System.out.println("We should not reach here");
        }
    }

    public void deep() throws Exception{
        this.depth++;
        if (this.depth == 40){
            deepThrow();
        }else if (this.depth == 20){
            deepCatch();
        }else{
            deep();
        }
    }

    public void deepCatch(){
        try{
            deep();
        }catch (Exception e){
            //System.out.println(this.depth);
            //System.out.println(AVMStackWatcher.getCurStackDepth());
        }
    }

    public void deepThrow() throws Exception{
        throw new Exception("Deep Throw");
    }

    public void deepT() {
        try{
            this.deep();
        }catch(Exception e){
            int a = 1;
        }

        int b = 2;
        if (b == 4){
            return;
        }

        try{
            this.deep();
        }catch(Exception e){
            int c = 3;
        }

    }
}
