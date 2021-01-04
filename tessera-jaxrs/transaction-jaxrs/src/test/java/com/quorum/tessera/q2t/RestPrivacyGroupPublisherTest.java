package com.quorum.tessera.q2t;

import com.quorum.tessera.jaxrs.mock.MockClient;
import com.quorum.tessera.privacygroup.exception.PrivacyGroupPublishException;
import com.quorum.tessera.transaction.publish.NodeOfflineException;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;

public class RestPrivacyGroupPublisherTest {

    private RestPrivacyGroupPublisher publisher;

    private MockClient mockClient;

    @Before
    public void setUp() {
        mockClient = new MockClient();
        publisher = new RestPrivacyGroupPublisher(mockClient);
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
        publisher.publish(data, "someUrl.com");

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
            publisher.publish(data, "someUrl.com");
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
            publisher.publish(data, "someUrl.com");
            failBecauseExceptionWasNotThrown(Exception.class);
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(NodeOfflineException.class);
        }
    }
}
