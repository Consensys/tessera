
package com.quorum.tessera.sync;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;


public class WebsocketResendBatchPublisherFactoryTest {
    
    private WebsocketResendBatchPublisherFactory websocketResendBatchPublisherFactory;
    
    @Before
    public void onSetUp() {
        websocketResendBatchPublisherFactory = new WebsocketResendBatchPublisherFactory();
        assertThat(websocketResendBatchPublisherFactory.communicationType()).isEqualTo(CommunicationType.WEB_SOCKET);
    }
    
    @Test
    public void create() {
        Config config = mock(Config.class);
        ResendBatchPublisher resendBatchPublisher = websocketResendBatchPublisherFactory.create(config);
        assertThat(resendBatchPublisher).isExactlyInstanceOf(WebsocketPayloadPublisher.class);
    }
    
}
