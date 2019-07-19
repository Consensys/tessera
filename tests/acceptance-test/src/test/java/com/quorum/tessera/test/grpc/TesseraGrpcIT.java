package com.quorum.tessera.test.grpc;

import com.google.protobuf.Empty;
import com.quorum.tessera.grpc.p2p.TesseraGrpc;
import com.quorum.tessera.grpc.p2p.VersionMessage;
import com.quorum.tessera.test.Party;
import com.quorum.tessera.test.PartyHelper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TesseraGrpcIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(TesseraGrpcIT.class);

    private ManagedChannel channel;

    private TesseraGrpc.TesseraBlockingStub blockingStub;

    @Rule
    public TestName testName = new TestName();

    @Before
    public void onSetUp() {
        Party anyParty = PartyHelper.create().getParties().findAny().get();
        
        channel = ManagedChannelBuilder.forAddress(anyParty.getP2PUri().getHost(), anyParty.getP2PUri().getPort())
            .usePlaintext()
            .build();

        blockingStub = TesseraGrpc.newBlockingStub(channel);
    }

    @After
    public void onTearDown() {
        channel.shutdown();

    }

    @Test
    public void version() throws Exception {

        LOGGER.info("Sending");


        VersionMessage result = blockingStub.getVersion(Empty.getDefaultInstance());
        LOGGER.info("Sent");

        assertThat(result).isNotNull();

    }

}
