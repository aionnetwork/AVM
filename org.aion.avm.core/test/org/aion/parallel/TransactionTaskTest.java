package org.aion.parallel;

import org.aion.avm.core.ExecutionType;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.SideEffects;
import org.aion.kernel.TestingState;
import org.aion.types.Log;
import org.junit.Assert;
import org.junit.Test;

public class TransactionTaskTest {

    private byte[] addr1 = Helpers.hexStringToBytes("1111111111111111111111111111111111111111111111111111111111111111");
    private byte[] addr2 = Helpers.hexStringToBytes("2222222222222222222222222222222222222222222222222222222222222222");
    
    @Test
    public void basicConcurrencyTest(){
        TransactionTask task = new TransactionTask(new TestingState(), null, 2, Helpers.ZERO_ADDRESS, ExecutionType.ASSUME_MAINCHAIN, 0);
        SideEffects sideEffects = new SideEffects();
        sideEffects.addLog(Log.dataOnly(addr1, addr2));
        task.pushSideEffects(sideEffects);
        Assert.assertFalse(task.peekSideEffects().getExecutionLogs().isEmpty());
        
        //Starting a new transaction should clear side effects
        task.startNewTransaction();
        Assert.assertTrue(task.peekSideEffects().getExecutionLogs().isEmpty());
    }
}
