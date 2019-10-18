package com.quorum.tessera.sync;

import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.partyinfo.PartyInfoParser;
import com.quorum.tessera.partyinfo.ResendRequest;
import com.quorum.tessera.util.Base64Decoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Deprecated
@ClientEndpoint(encoders = {SyncRequestMessageCodec.class})
public class WebSocketP2pClient implements P2pClient<ResendRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebsocketPayloadPublisher.class);

    @Override
    public byte[] push(String targetUrl, byte[] data) {

        final SyncRequestMessage syncRequestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.TRANSACTION_PUSH)
                        .withTransactions(PayloadEncoder.create().decode(data))
                        .withCorrelationId(UUID.randomUUID().toString())
                        .build();

        doSend(targetUrl, syncRequestMessage);

        return new byte[0];
    }

    @Override
    public boolean sendPartyInfo(String targetUrl, byte[] data) {

        final SyncRequestMessage syncRequestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.PARTY_INFO)
                        .withPartyInfo(PartyInfoParser.create().from(data))
                        .withCorrelationId(UUID.randomUUID().toString())
                        .build();
        return doSend(targetUrl, syncRequestMessage);
    }

    @Override
    public boolean makeResendRequest(String targetUrl, ResendRequest request) {

        final SyncRequestMessage syncRequestMessage =
                SyncRequestMessage.Builder.create(SyncRequestMessage.Type.TRANSACTION_SYNC)
                        .withRecipientKey(PublicKey.from(Base64Decoder.create().decode(request.getPublicKey())))
                        .withCorrelationId(UUID.randomUUID().toString())
                        .build();

        return doSend(targetUrl, syncRequestMessage);
    }

    private boolean doSend(String targetUrl, SyncRequestMessage request) {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = UriBuilder.fromUri(URI.create(targetUrl)).path("sync").build();

        try {
            final Session session = container.connectToServer(this, uri);
            WebSocketSessionCallback.execute(
                    () -> {
                        LOGGER.debug("Sending {} ", request);
                        session.getBasicRemote().sendObject(request);
                        return null;
                    });
            return true;
        } catch (DeploymentException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
