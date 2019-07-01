
package com.quorum.tessera.grpc.api;

import com.quorum.tessera.config.AppType;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;

public class Q2TGrpcAppTest {
    
    private Q2TGrpcApp q2TGrpcApp;
    
    @Before
    public void onSetup() {
        q2TGrpcApp = new Q2TGrpcApp();
    }
    
    @Test
    public void appType() {
        assertThat(q2TGrpcApp.getAppType()).isEqualTo(AppType.Q2T);
        
    }
    
}
