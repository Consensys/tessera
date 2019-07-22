
package com.quorum.tessera.app;

import com.quorum.tessera.config.AppType;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class ApplicationFactoryTest {
    
    @Test
    public void testServiceLoaderFindSampleAppAndNothingElse() {
        Optional<TesseraRestApplication> app = ApplicationFactory.create(AppType.Q2T);
        assertThat(app).isPresent().containsInstanceOf(SampleApp.class);
    }
    
    @Test
    public void testServiceLoaderDoesnotFindAppForAppType() {
        Optional<TesseraRestApplication> app = ApplicationFactory.create(AppType.P2P);
        assertThat(app).isNotPresent();
    }
    
}
