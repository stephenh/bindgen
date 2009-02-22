package org.exigencecorp.bindgen.example.blocks;

import junit.framework.Assert;
import junit.framework.TestCase;
import bindgen.org.exigencecorp.bindgen.example.blocks.TransactionExampleBinding;

public class TransactionExampleTest extends TestCase {

    public void testBusinessLogicMethod() throws Exception {
        TransactionExample te = new TransactionExample();
        TransactionExampleBinding teb = new TransactionExampleBinding(te);
        TransactionBlock block = teb.businessLogic();
        Assert.assertEquals(true, block.result("good").booleanValue());
    }

    public void testBusinessLogicThatFailsMethod() throws Exception {
        TransactionExample te = new TransactionExample();
        TransactionExampleBinding teb = new TransactionExampleBinding(te);
        TransactionBlock block = teb.businessLogicThatCanFail();
        try {
            block.result("good");
            Assert.fail();
        } catch (Exception e) {
            Assert.assertEquals("I failed", e.getMessage());
        }
    }

}
