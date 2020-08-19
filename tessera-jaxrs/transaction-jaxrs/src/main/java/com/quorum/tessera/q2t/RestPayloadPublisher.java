package com.quorum.tessera.q2t;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.partyinfo.node.Recipient;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.transaction.publish.PublishPayloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RestPayloadPublisher implements PayloadPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestPayloadPublisher.class);

    private final Client restclient;

    private final PayloadEncoder payloadEncoder;

    private final Discovery partyInfoService;

    public RestPayloadPublisher(Client restclient, Discovery partyInfoService) {
        this(restclient, PayloadEncoder.create(), partyInfoService);
    }

    public RestPayloadPublisher(Client restclient, PayloadEncoder payloadEncoder, Discovery partyInfoService) {
        this.restclient = restclient;
        this.payloadEncoder = payloadEncoder;
        this.partyInfoService = partyInfoService;
    }

    @Override
    public void publishPayload(EncodedPayload payload, PublicKey recipientKey) {

        final Recipient retrievedRecipientFromStore =
                partyInfoService.getCurrent().getRecipients().stream()
                        .filter(recipient -> recipientKey.equals(recipient.getKey()))
                        .findAny()
                        .orElseThrow(
                                () ->
                                        new KeyNotFoundException(
                                                "Recipient not found for key: " + recipientKey.encodeToBase64()));

        final String targetUrl = retrievedRecipientFromStore.getUrl();

        LOGGER.info("Publishing message to {}", targetUrl);

        final byte[] encoded = payloadEncoder.encode(payload);

        try (Response response =
                restclient
                        .target(targetUrl)
                        .path("/push")
                        .request()
                        .post(Entity.entity(encoded, MediaType.APPLICATION_OCTET_STREAM_TYPE))) {

            if (Response.Status.OK.getStatusCode() != response.getStatus()
                    && Response.Status.CREATED.getStatusCode() != response.getStatus()) {
                throw new PublishPayloadException("Unable to push payload to recipient url " + targetUrl);
            }

            LOGGER.info("Published to {}", targetUrl);
        }
    }
}
