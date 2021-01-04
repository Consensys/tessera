package com.quorum.tessera.q2t;

import com.quorum.tessera.privacygroup.exception.PrivacyGroupPublishException;
import com.quorum.tessera.transaction.publish.NodeOfflineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

public class RestPrivacyGroupPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestPrivacyGroupPublisher.class);

    private final Client restClient;

    RestPrivacyGroupPublisher(Client restClient) {
        this.restClient = restClient;
    }

    void publish(byte[] data, String targetUrl) {

        try (Response response =
                restClient
                        .target(targetUrl)
                        .path("/pushPrivacyGroup")
                        .request()
                        .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM_TYPE))) {

            if (Response.Status.OK.getStatusCode() != response.getStatus()) {
                throw new PrivacyGroupPublishException("Unable to push privacy group to recipient url " + targetUrl);
            }
            LOGGER.info("Published privacy group to {}", targetUrl);
        } catch (ProcessingException ex) {
            LOGGER.debug("", ex);
            throw new NodeOfflineException(URI.create(targetUrl));
        }
    }
}
