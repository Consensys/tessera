package com.quorum.tessera.test.grpc;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.test.CucumberGprcIT;
import com.quorum.tessera.test.DBType;
import com.quorum.tessera.test.ProcessManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import suite.ExecutionContext;
import suite.SocketType;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    SendGrpcIT.class,
    PartyInfoGrpcIT.class,
    TesseraGrpcIT.class,
    CucumberGprcIT.class
})
public class GrpcSuiteH2 {

    private static final ProcessManager PROCESS_MANAGER = new ProcessManager(CommunicationType.GRPC, DBType.H2);

    @BeforeClass
    public static void onSetup() throws Exception {
        ExecutionContext.Builder.create()
                .with(CommunicationType.GRPC)
                .with(DBType.H2)
                .with(SocketType.HTTP)
                .build();
        PROCESS_MANAGER.startNodes();
    }

    @AfterClass
    public static void onTearDown() throws Exception {
        PROCESS_MANAGER.stopNodes();
        ExecutionContext.destoryContext();
    }

}
