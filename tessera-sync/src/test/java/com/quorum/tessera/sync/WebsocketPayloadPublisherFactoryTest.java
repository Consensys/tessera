package com.quorum.tessera.sync;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.PayloadPublisher;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class WebsocketPayloadPublisherFactoryTest {

    private WebsocketPayloadPublisherFactory websocketPayloadPublisherFactory = new WebsocketPayloadPublisherFactory();

    @Test
    public void create() {
        Config config = mock(Config.class);
        PayloadPublisher p = websocketPayloadPublisherFactory.create(config);
        assertThat(p).isNotNull().isExactlyInstanceOf(WebsocketPayloadPublisher.class);
        assertThat(websocketPayloadPublisherFactory.communicationType()).isEqualTo(CommunicationType.WEB_SOCKET);
    }

}
