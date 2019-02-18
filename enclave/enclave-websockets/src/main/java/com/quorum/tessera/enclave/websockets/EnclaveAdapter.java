package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.RawTransaction;
import com.quorum.tessera.encryption.PublicKey;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnclaveAdapter implements Enclave {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveAdapter.class);

    private final WebSocketContainer container;

    private EnclaveClientEndpoint client = new EnclaveClientEndpoint();

    private final URI serverUri;

    private Session session;

    private WebSocketTemplate webSocketTemplate;

    public EnclaveAdapter(URI serverUri) {
        this(ContainerProvider.getWebSocketContainer(), serverUri);
    }

    public EnclaveAdapter(WebSocketContainer container, URI serverUri) {
        this.serverUri = serverUri;
        this.container = container;
    }

    @PostConstruct
    public void onConstruct() {
        try{
            session = container.connectToServer(client, serverUri);
            webSocketTemplate = new WebSocketTemplate(session);
        } catch (IOException | DeploymentException ex) {
            throw new EnclaveCommunicationException(ex);
        }
    }

    @PreDestroy
    public void onDestroy() {
        try{
            session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Bye"));
        } catch (IOException ex) {
            LOGGER.warn("IOException while attempting to close remote session", ex.getMessage());
            LOGGER.debug(null, ex);
        }
    }

    @Override
    public PublicKey defaultPublicKey() {

        webSocketTemplate.execute(s -> {
            EnclaveRequest request = EnclaveRequest.Builder.create()
                    .withType(EnclaveRequestType.DEFAULT_PUBLIC_KEY)
                    .build();

            s.getBasicRemote().sendObject(request);
        });

        return client.pollForResult(PublicKey.class).get();
    }

    @Override
    public Set<PublicKey> getForwardingKeys() {
        webSocketTemplate.execute(s -> {
            EnclaveRequest request = EnclaveRequest.Builder.create()
                    .withType(EnclaveRequestType.FORWARDING_KEYS)
                    .build();

            s.getBasicRemote().sendObject(request);
        });

        return client.pollForResult(Set.class).get();
    }

    @Override
    public Set<PublicKey> getPublicKeys() {
        webSocketTemplate.execute(s -> {
            EnclaveRequest request = EnclaveRequest.Builder.create()
                    .withType(EnclaveRequestType.PUBLIC_KEYS)
                    .build();

            s.getBasicRemote().sendObject(request);
        });

        return client.pollForResult(Set.class).get();
    }

    @Override
    public EncodedPayload encryptPayload(byte[] message, PublicKey senderPublicKey, List<PublicKey> recipientPublicKeys) {

        webSocketTemplate.execute(s -> {
            EnclaveRequest request = EnclaveRequest.Builder.create()
                    .withType(EnclaveRequestType.ENCRYPT_PAYLOAD)
                    .withArg(message)
                    .withArg(senderPublicKey)
                    .withArg(recipientPublicKeys)
                    .build();

            s.getBasicRemote().sendObject(request);
        });

        return client.pollForResult(EncodedPayload.class).get();
    }

    @Override
    public EncodedPayload encryptPayload(RawTransaction rawTransaction, List<PublicKey> recipientPublicKeys) {

        webSocketTemplate.execute(s -> {
            EnclaveRequest request = EnclaveRequest.Builder.create()
                    .withType(EnclaveRequestType.ENCRYPT_RAWTXN_PAYLOAD)
                    .withArg(rawTransaction)
                    .withArg(recipientPublicKeys)
                    .build();

            s.getBasicRemote().sendObject(request);
        });

        return client.pollForResult(EncodedPayload.class).get();
    }

    @Override
    public RawTransaction encryptRawPayload(byte[] message, PublicKey sender) {

        webSocketTemplate.execute(s -> {
            EnclaveRequest request = EnclaveRequest.Builder.create()
                    .withType(EnclaveRequestType.ENCRYPT_RAW_PAYLOAD)
                    .withArg(message)
                    .withArg(sender)
                    .build();

            s.getBasicRemote().sendObject(request);

        });
        return client.pollForResult(RawTransaction.class).get();
    }

    @Override
    public byte[] unencryptTransaction(EncodedPayload payload, PublicKey providedKey) {
        webSocketTemplate.execute(s -> {
            EnclaveRequest request = EnclaveRequest.Builder.create()
                    .withType(EnclaveRequestType.UNENCRYPT_TXN)
                    .withArg(payload)
                    .withArg(providedKey).build();
            
            
            s.getBasicRemote().sendObject(request);
        });
        
        return client.pollForResult(ByteBuffer.class).get().array();        
    }

    @Override
    public byte[] createNewRecipientBox(EncodedPayload payload, PublicKey recipientKey) {
        webSocketTemplate.execute(s -> {
            EnclaveRequest request = EnclaveRequest.Builder.create()
                    .withType(EnclaveRequestType.CREATE_NEW_RECIPIENT_BOX)
                    .withArg(payload)
                    .withArg(recipientKey).build();  
            s.getBasicRemote().sendObject(request);
        });
        
        return client.pollForResult(ByteBuffer.class).get().array();  
    }

    @Override
    public com.quorum.tessera.service.Service.Status status() {
        webSocketTemplate.execute(s -> {
            EnclaveRequest request = EnclaveRequest.Builder.create()
                    .withType(EnclaveRequestType.STATUS).build();
            
            
            s.getBasicRemote().sendObject(request);
        });
        
        return client.pollForResult(com.quorum.tessera.service.Service.Status.class).get();       
    }
    
}
