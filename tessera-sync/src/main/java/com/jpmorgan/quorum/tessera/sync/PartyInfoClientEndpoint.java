package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.core.api.ServiceFactory;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.transaction.TransactionManager;
import java.util.Objects;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClientEndpoint(
        decoders = {SyncRequestMessageCodec.class, SyncResponseMessageCodec.class},
        encoders = {SyncRequestMessageCodec.class, SyncResponseMessageCodec.class})
public class PartyInfoClientEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoClientEndpoint.class);

    private TransactionManager transactionManager;

    private final PartyInfoService partyInfoService;

    public PartyInfoClientEndpoint() {
        this(ServiceFactory.create().partyInfoService());
    }

    public PartyInfoClientEndpoint(PartyInfoService partyInfoService) {
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
    }

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info("Session id : {}", session.getId());
    }

    @OnMessage
    public void onResponse(Session session, SyncResponseMessage response) {

        if (response.getType() == SyncResponseMessage.Type.PARTY_INFO) {

            final PartyInfo partyInfo = response.getPartyInfo();

            LOGGER.info("Client received message: {} {}", session.getId(), partyInfo);

            partyInfoService.updatePartyInfo(partyInfo);
        }

        if (response.getType() == SyncResponseMessage.Type.TRANSACTION_SYNC) {

            Long transactionCount = response.getTransactionCount();
            Long transactionOffset = response.getTransactionOffset();
            EncodedPayload encodedPayload = response.getTransactions();
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOGGER.info("Closing session : {} because {}", session.getId(), reason);
    }
}
