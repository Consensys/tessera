package com.quorum.tessera.p2p;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.partyinfo.PayloadPublisher;
import com.quorum.tessera.partyinfo.PublishPayloadException;
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

    public RestPayloadPublisher(Client restclient) {
        this(restclient, PayloadEncoder.create());
    }

    public RestPayloadPublisher(Client restclient, PayloadEncoder payloadEncoder) {
        this.restclient = restclient;
        this.payloadEncoder = payloadEncoder;
    }

    @Override
    public void publishPayload(EncodedPayload payload, String targetUrl) {

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
