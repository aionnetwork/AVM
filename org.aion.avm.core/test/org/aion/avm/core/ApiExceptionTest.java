package org.aion.avm.core;

import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.junit.Ignore;
import org.junit.Test;

public class ApiExceptionTest {

    private byte[] from = Helpers.randomBytes(Address.LENGTH);
    private byte[] to = Helpers.randomBytes(Address.LENGTH);
    private long energyLimit = 5000000;

    private Block block = new Block(1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);

    @Ignore
    @Test
    public void testCatchApiException() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(ApiExceptionTestResource.class);
        byte[] txData = Helpers.encodeCodeAndData(jar, null);

        Transaction tx = new Transaction(Transaction.Type.CREATE, from, to, 0, txData, energyLimit);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        TransactionResult result = new AvmImpl(new KernelInterfaceImpl()).run(context);

        System.out.println(result.getReturnData());
    }
}
