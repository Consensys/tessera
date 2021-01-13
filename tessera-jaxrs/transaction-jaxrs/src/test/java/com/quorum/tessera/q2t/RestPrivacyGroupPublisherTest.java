package com.quorum.tessera.q2t;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.jaxrs.mock.MockClient;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.privacygroup.exception.PrivacyGroupPublishException;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;
import com.quorum.tessera.privacygroup.exception.PrivacyGroupNotSupportedException;
import com.quorum.tessera.transaction.publish.NodeOfflineException;
import com.quorum.tessera.version.ApiVersion;
import com.quorum.tessera.version.PrivacyGroupVersion;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RestPrivacyGroupPublisherTest {

    private PrivacyGroupPublisher publisher;

    private Discovery discovery;

    private MockClient mockClient;

    @Before
    public void setUp() {
        mockClient = new MockClient();
        discovery = mock(Discovery.class);
        publisher = new RestPrivacyGroupPublisher(discovery, mockClient);
        final NodeInfo node =
                NodeInfo.Builder.create()
                        .withUrl("url1.com")
                        .withRecipients(Collections.emptyList())
                        .withSupportedApiVersions(ApiVersion.versions())
                        .build();

        when(discovery.getRemoteNodeInfo(PublicKey.from("PUBLIC_KEY".getBytes()))).thenReturn(node);
    }

    @Test
    public void testPublishSingleSuccess() {

        Invocation.Builder invocationBuilder = mockClient.getWebTarget().getMockInvocationBuilder();

        List<Entity> postedEntities = new ArrayList<>();
        doAnswer(
                        (invocation) -> {
                            postedEntities.add(invocation.getArgument(0));
                            return Response.ok().build();
                        })
                .when(invocationBuilder)
                .post(any(javax.ws.rs.client.Entity.class));

        final byte[] data = new byte[] {15};
        publisher.publishPrivacyGroup(data, PublicKey.from("PUBLIC_KEY".getBytes()));

        assertThat(postedEntities).hasSize(1);
    }

    @Test
    public void testPublishSingleError() {

        Invocation.Builder invocationBuilder = mockClient.getWebTarget().getMockInvocationBuilder();

        doAnswer((invocation) -> Response.serverError().build())
                .when(invocationBuilder)
                .post(any(javax.ws.rs.client.Entity.class));

        final byte[] data = new byte[5];
        try {
            publisher.publishPrivacyGroup(data, PublicKey.from("PUBLIC_KEY".getBytes()));
            failBecauseExceptionWasNotThrown(Exception.class);
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(PrivacyGroupPublishException.class);
        }
    }

    @Test
    public void testPublishSingleNodeOffline() {

        Invocation.Builder invocationBuilder = mockClient.getWebTarget().getMockInvocationBuilder();
        doThrow(ProcessingException.class).when(invocationBuilder).post(any(javax.ws.rs.client.Entity.class));

        final byte[] data = new byte[5];
        try {
            publisher.publishPrivacyGroup(data, PublicKey.from("PUBLIC_KEY".getBytes()));
            failBecauseExceptionWasNotThrown(Exception.class);
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(NodeOfflineException.class);
        }
    }

    @Test
    public void remoteNodeDoesNotSupportPrivacyGroup() {

        final List<String> versions =
                ApiVersion.versions().stream()
                        .filter(Predicate.not(v -> v.equals(PrivacyGroupVersion.API_VERSION_3)))
                        .collect(Collectors.toList());

        final NodeInfo oldNode =
                NodeInfo.Builder.create()
                        .withUrl("url2.com")
                        .withRecipients(Collections.emptyList())
                        .withSupportedApiVersions(versions)
                        .build();

        when(discovery.getRemoteNodeInfo(PublicKey.from("OLD_KEY".getBytes()))).thenReturn(oldNode);

        final byte[] data = new byte[] {15};

        assertThatThrownBy(
                        () -> {
                            publisher.publishPrivacyGroup(data, PublicKey.from("OLD_KEY".getBytes()));
                        })
                .isInstanceOf(PrivacyGroupNotSupportedException.class)
                .hasMessageContaining(PublicKey.from("OLD_KEY".getBytes()).encodeToBase64());
    }
}
