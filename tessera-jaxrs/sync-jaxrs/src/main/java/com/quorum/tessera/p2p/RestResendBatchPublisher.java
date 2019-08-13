package com.quorum.tessera.p2p;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.partyinfo.PublishPayloadException;
import com.quorum.tessera.partyinfo.PushBatchRequest;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RestResendBatchPublisher implements ResendBatchPublisher {

private static final Logger LOGGER = LoggerFactory.getLogger(RestResendBatchPublisher.class);

    private final PayloadEncoder payloadEncoder;

    private final Client restclient;

    public RestResendBatchPublisher(Client restclient) {
        this(PayloadEncoder.create(),restclient);
    }

    public RestResendBatchPublisher(
            final PayloadEncoder payloadEncoder,
            final Client restclient) {
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
        this.restclient = Objects.requireNonNull(restclient);
    }

    @Override
    public void publishBatch(final List<EncodedPayload> payloads, final String targetUrl) {

        LOGGER.info("Publishing message to {}", targetUrl);

        final List<byte[]> encodedPayloads = payloads.stream().map(payloadEncoder::encode).collect(Collectors.toList());
        
        PushBatchRequest pushBatchRequest = new PushBatchRequest(encodedPayloads);
        
        final Response response =
                restclient.target(targetUrl)
                        .path("/pushBatch") //FIXME: Dont use camel case in urls
                        .request()
                        .post(Entity.entity(pushBatchRequest, MediaType.APPLICATION_JSON));

        boolean result = Response.Status.OK.getStatusCode() == response.getStatus();
        if (!result) {
            throw new PublishPayloadException(
                    "Unable to push payload batch to recipient " + targetUrl);
        }

        LOGGER.info("Published to {}", targetUrl);
    }
}
