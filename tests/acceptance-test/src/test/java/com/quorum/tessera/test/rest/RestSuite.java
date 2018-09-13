package com.quorum.tessera.test.rest;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.ProcessManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    DeleteIT.class,
    MultipleKeyNodeIT.class,
    PushIT.class,
    ReceiveIT.class,
    ReceiveRawIT.class,
    ResendAllIT.class,
    ResendIndividualIT.class,
    SendIT.class,
    SendRawIT.class,
    TesseraIT.class,
    TransactionForwardingIT.class
})
public class RestSuite {

    private static final ProcessManager PROCESS_MANAGER = new ProcessManager(CommunicationType.REST);

    @BeforeClass
    public static void onSetup() throws Exception {
        PROCESS_MANAGER.startNodes();
    }

    @AfterClass
    public static void onTearDown() throws Exception {
        PROCESS_MANAGER.stopNodes();

    }
}
