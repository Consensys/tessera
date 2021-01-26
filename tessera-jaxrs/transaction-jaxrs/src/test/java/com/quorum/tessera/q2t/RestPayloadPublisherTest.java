package com.quorum.tessera.q2t;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyGroupId;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.jaxrs.mock.MockClient;
import com.quorum.tessera.privacygroup.exception.PrivacyGroupNotSupportedException;
import com.quorum.tessera.transaction.exception.EnhancedPrivacyNotSupportedException;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import com.quorum.tessera.transaction.publish.NodeOfflineException;
import com.quorum.tessera.transaction.publish.PublishPayloadException;
import org.junit.*;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RestPayloadPublisherTest {

    private RestPayloadPublisher publisher;

    private MockClient mockClient;

    private PayloadEncoder encoder;

    private Discovery discovery;

    @Before
    public void onSetUp() {
        mockClient = new MockClient();
        encoder = mock(PayloadEncoder.class);
        discovery = mock(Discovery.class);
        publisher = new RestPayloadPublisher(mockClient, encoder, discovery);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(encoder, discovery);
    }

    @Test
    public void publish() {

        Invocation.Builder invocationBuilder = mockClient.getWebTarget().getMockInvocationBuilder();

        List<javax.ws.rs.client.Entity> postedEntities = new ArrayList<>();

        doAnswer(
                        (invocation) -> {
                            postedEntities.add(invocation.getArgument(0));
                            return Response.ok().build();
                        })
                .when(invocationBuilder)
                .post(any(javax.ws.rs.client.Entity.class));

        String targetUrl = "http://someplace.com";

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        byte[] payloadData = "Some Data".getBytes();
        when(encoder.encode(encodedPayload)).thenReturn(payloadData);

        PublicKey recipientKey = mock(PublicKey.class);
        NodeInfo nodeInfo = mock(NodeInfo.class);
        Recipient recipient = mock(Recipient.class);
        when(recipient.getKey()).thenReturn(recipientKey);
        when(recipient.getUrl()).thenReturn(targetUrl);
        when(nodeInfo.getRecipients()).thenReturn(Set.of(recipient));
        when(discovery.getRemoteNodeInfo(recipientKey)).thenReturn(nodeInfo);

        publisher.publishPayload(encodedPayload, recipientKey);

        assertThat(postedEntities).hasSize(1);

        Entity entity = postedEntities.get(0);
        assertThat(entity.getMediaType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        assertThat(entity.getEntity()).isSameAs(payloadData);

        verify(encoder).encode(encodedPayload);
        verify(invocationBuilder).post(any(javax.ws.rs.client.Entity.class));
        verify(discovery).getRemoteNodeInfo(eq(recipientKey));
    }

    @Test
    public void publishReturns201() {

        Invocation.Builder invocationBuilder = mockClient.getWebTarget().getMockInvocationBuilder();

        List<javax.ws.rs.client.Entity> postedEntities = new ArrayList<>();

        doAnswer(
                        (invocation) -> {
                            postedEntities.add(invocation.getArgument(0));
                            return Response.created(URI.create("http://location")).build();
                        })
                .when(invocationBuilder)
                .post(any(javax.ws.rs.client.Entity.class));

        String targetUrl = "http://someplace.com";

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        byte[] payloadData = "Some Data".getBytes();
        when(encoder.encode(encodedPayload)).thenReturn(payloadData);

        PublicKey recipientKey = mock(PublicKey.class);
        NodeInfo nodeInfo = mock(NodeInfo.class);
        Recipient recipient = mock(Recipient.class);
        when(recipient.getKey()).thenReturn(recipientKey);
        when(recipient.getUrl()).thenReturn(targetUrl);
        when(nodeInfo.getRecipients()).thenReturn(Set.of(recipient));
        when(discovery.getRemoteNodeInfo(recipientKey)).thenReturn(nodeInfo);

        publisher.publishPayload(encodedPayload, recipientKey);

        assertThat(postedEntities).hasSize(1);

        Entity entity = postedEntities.get(0);
        assertThat(entity.getMediaType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_TYPE);
        assertThat(entity.getEntity()).isSameAs(payloadData);

        verify(encoder).encode(encodedPayload);
        verify(invocationBuilder).post(any(javax.ws.rs.client.Entity.class));
        verify(discovery).getRemoteNodeInfo(eq(recipientKey));
    }

    @Test
    public void publishReturnsError() {

        Invocation.Builder invocationBuilder = mockClient.getWebTarget().getMockInvocationBuilder();

        doAnswer(
                        (invocation) -> {
                            return Response.serverError().build();
                        })
                .when(invocationBuilder)
                .post(any(javax.ws.rs.client.Entity.class));

        String targetUrl = "http://someplace.com";

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        byte[] payloadData = "Some Data".getBytes();
        when(encoder.encode(encodedPayload)).thenReturn(payloadData);

        PublicKey recipientKey = mock(PublicKey.class);
        NodeInfo nodeInfo = mock(NodeInfo.class);
        Recipient recipient = mock(Recipient.class);
        when(recipient.getKey()).thenReturn(recipientKey);
        when(recipient.getUrl()).thenReturn(targetUrl);
        when(nodeInfo.getRecipients()).thenReturn(Set.of(recipient));
        when(discovery.getRemoteNodeInfo(recipientKey)).thenReturn(nodeInfo);

        try {
            publisher.publishPayload(encodedPayload, recipientKey);
            failBecauseExceptionWasNotThrown(PublishPayloadException.class);
        } catch (PublishPayloadException ex) {
            verify(encoder).encode(encodedPayload);
            verify(invocationBuilder).post(any(javax.ws.rs.client.Entity.class));
            verify(discovery).getRemoteNodeInfo(eq(recipientKey));
        }
    }

    @Test
    public void publishEnhancedTransactionsToNodesThatDoNotSupport() {

        String targetUrl = "http://someplace.com";

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);
        byte[] payloadData = "Some Data".getBytes();
        when(encoder.encode(encodedPayload)).thenReturn(payloadData);

        PublicKey recipientKey = mock(PublicKey.class);
        NodeInfo nodeInfo = mock(NodeInfo.class);
        when(nodeInfo.supportedApiVersions()).thenReturn(Set.of("v1"));
        Recipient recipient = mock(Recipient.class);
        when(recipient.getKey()).thenReturn(recipientKey);
        when(recipient.getUrl()).thenReturn(targetUrl);

        when(nodeInfo.getRecipients()).thenReturn(Set.of(recipient));
        when(discovery.getRemoteNodeInfo(recipientKey)).thenReturn(nodeInfo);

        try {
            publisher.publishPayload(encodedPayload, recipientKey);
            failBecauseExceptionWasNotThrown(EnhancedPrivacyNotSupportedException.class);
        } catch (EnhancedPrivacyNotSupportedException ex) {
            assertThat(ex).isNotNull();
            assertThat(ex).hasMessageContaining("Transactions with enhanced privacy is not currently supported");
        } finally {
            verify(discovery).getRemoteNodeInfo(eq(recipientKey));
        }
    }

    @Test
    public void publishPrivacyGroupToNodesThatDoNotSupport() {

        String targetUrl = "http://someplace.com";

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);
        when(encodedPayload.getPrivacyGroupId()).thenReturn(Optional.of(mock(PrivacyGroupId.class)));
        byte[] payloadData = "Some Data".getBytes();
        when(encoder.encode(encodedPayload)).thenReturn(payloadData);

        PublicKey recipientKey = mock(PublicKey.class);
        NodeInfo nodeInfo = mock(NodeInfo.class);
        when(nodeInfo.supportedApiVersions()).thenReturn(Set.of("v1", "v2", "2.1"));
        Recipient recipient = mock(Recipient.class);
        when(recipient.getKey()).thenReturn(recipientKey);
        when(recipient.getUrl()).thenReturn(targetUrl);

        when(nodeInfo.getRecipients()).thenReturn(Set.of(recipient));
        when(discovery.getRemoteNodeInfo(recipientKey)).thenReturn(nodeInfo);

        try {
            publisher.publishPayload(encodedPayload, recipientKey);
            failBecauseExceptionWasNotThrown(PrivacyGroupNotSupportedException.class);
        } catch (PrivacyGroupNotSupportedException ex) {
            assertThat(ex).isNotNull();
            assertThat(ex).hasMessageContaining("Transactions with privacy group is not currently supported");
        } finally {
            verify(discovery).getRemoteNodeInfo(eq(recipientKey));
        }
    }

    @Test
    public void handleConnectionError() {

        final String targetUri = "http://jimmywhite.com";
        final PublicKey recipientKey = mock(PublicKey.class);

        Recipient recipient = mock(Recipient.class);
        when(recipient.getKey()).thenReturn(recipientKey);
        when(recipient.getUrl()).thenReturn(targetUri);

        NodeInfo nodeInfo = mock(NodeInfo.class);
        when(nodeInfo.getRecipients()).thenReturn(Set.of(recipient));
        when(nodeInfo.getUrl()).thenReturn(targetUri);
        when(discovery.getRemoteNodeInfo(recipientKey)).thenReturn(nodeInfo);

        Client client = mock(Client.class);
        when(client.target(targetUri)).thenThrow(ProcessingException.class);

        final EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        when(encoder.encode(payload)).thenReturn("SomeData".getBytes());

        RestPayloadPublisher restPayloadPublisher = new RestPayloadPublisher(client, encoder, discovery);

        try {
            restPayloadPublisher.publishPayload(payload, recipientKey);
            failBecauseExceptionWasNotThrown(NodeOfflineException.class);
        } catch (NodeOfflineException ex) {
            assertThat(ex).hasMessageContaining(targetUri);
            verify(client).target(targetUri);
            verify(discovery).getRemoteNodeInfo(eq(recipientKey));
            verify(encoder).encode(payload);
            verify(discovery).getRemoteNodeInfo(eq(recipientKey));
        }
    }
}
