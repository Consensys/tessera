package com.quorum.tessera.grpc;

import com.quorum.tessera.grpc.api.APITransactionGrpcService;
import com.quorum.tessera.grpc.p2p.PartyInfoGrpcService;
import com.quorum.tessera.grpc.p2p.TesseraGrpcService;
import com.quorum.tessera.grpc.p2p.P2PTransactionGrpcService;
import javax.inject.Inject;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = GrpcITConfig.class)
public class GrpcIT {

    @Inject private TesseraGrpcService tesseraGrpcService;

    @Inject private P2PTransactionGrpcService p2pTransactionGrpcService;

    @Inject private APITransactionGrpcService apiTransactionGrpcService;

    @Inject private PartyInfoGrpcService partyInfoGrpcService;

    @Test
    public void tesseraGrpcServiceHasBeenCreated() {
        assertThat(tesseraGrpcService).isNotNull();
    }

    @Test
    public void p2pTransactionGrpcServiceHasBeenCreated() {
        assertThat(p2pTransactionGrpcService).isNotNull();
    }

    @Test
    public void apiTransactionGrpcServiceHasBeenCreated() {
        assertThat(apiTransactionGrpcService).isNotNull();
    }

    @Test
    public void partyInfoGrpcServiceHasBeenCreated() {
        assertThat(partyInfoGrpcService).isNotNull();
    }
}
