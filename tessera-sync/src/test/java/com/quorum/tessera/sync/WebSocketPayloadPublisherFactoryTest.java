package com.quorum.tessera.sync;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.PayloadPublisher;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class WebSocketPayloadPublisherFactoryTest {

    private WebSocketPayloadPublisherFactory websocketPayloadPublisherFactory = new WebSocketPayloadPublisherFactory();

    @Test
    public void create() {
        Config config = mock(Config.class);
        PayloadPublisher p = websocketPayloadPublisherFactory.create(config);
        assertThat(p).isNotNull().isExactlyInstanceOf(WebSocketPayloadPublisher.class);
        assertThat(websocketPayloadPublisherFactory.communicationType()).isEqualTo(CommunicationType.WEB_SOCKET);
        PayloadPublisher result = websocketPayloadPublisherFactory.create(config);

        assertThat(result).isExactlyInstanceOf(WebSocketPayloadPublisher.class);
    }
}
