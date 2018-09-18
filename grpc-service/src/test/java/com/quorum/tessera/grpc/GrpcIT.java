package com.quorum.tessera.grpc;

import com.quorum.tessera.api.grpc.PartyInfoGrpcService;
import com.quorum.tessera.api.grpc.TesseraGrpcService;
import com.quorum.tessera.api.grpc.TransactionGrpcService;
import javax.inject.Inject;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = GrpcITConfig.class)
public class GrpcIT {

    @Inject
    private TesseraGrpcService tesseraGrpcService;

    @Inject
    private TransactionGrpcService transactionGrpcService;

    @Inject
    private PartyInfoGrpcService partyInfoGrpcService;
    
    @Test
    public void tesseraGrpcServiceHasBeenCreated() {
        assertThat(tesseraGrpcService).isNotNull();
    }
    
    @Test
    public void transactionGrpcServiceHasBeenCreated() {
        assertThat(transactionGrpcService).isNotNull();
    }

    @Test
    public void partyInfoGrpcServiceHasBeenCreated() {
        assertThat(partyInfoGrpcService).isNotNull();
    }

}
