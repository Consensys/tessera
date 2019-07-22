
package com.quorum.tessera.grpc.p2p;

import com.quorum.tessera.config.AppType;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;


public class P2PGrpcAppTest {
    
    private P2PGrpcApp p2pGrpcApp;
    
    @Before
    public void onSetup() {
        p2pGrpcApp = new P2PGrpcApp();
    }
    
    @Test
    public void appType() {
        assertThat(p2pGrpcApp.getAppType()).isEqualTo(AppType.P2P);
        
    }
}
