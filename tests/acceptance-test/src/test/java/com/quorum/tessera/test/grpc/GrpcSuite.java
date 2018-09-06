package com.quorum.tessera.test.grpc;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.ProcessManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    SendGrpcIT.class,
    TesseraGrpcIT.class
})
public class GrpcSuite {
    
    private static final ProcessManager PROCESS_MANAGER = new ProcessManager(CommunicationType.GRPC);
    
    @BeforeClass
    public static void onSetup() throws Exception {
        PROCESS_MANAGER.startNodes();
    }

    @AfterClass
    public static void onTearDown() throws Exception {
        PROCESS_MANAGER.stopNodes();
    }

}
