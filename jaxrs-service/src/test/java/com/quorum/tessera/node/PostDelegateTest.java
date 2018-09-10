package com.quorum.tessera.node;

import com.quorum.tessera.api.model.ApiPath;
import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.client.PostDelegate;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class PostDelegateTest {

    private PostDelegate delegate;

    private Invocation.Builder builder;

    @Before
    public void onSetup() {

        final Client client = mock(Client.class);
        final WebTarget webTarget = mock(WebTarget.class);

        this.builder = mock(Invocation.Builder.class);
        doReturn(builder).when(webTarget).request();

        doReturn(webTarget).when(client).target(anyString());
        doReturn(webTarget).when(webTarget).path(anyString());

        this.delegate = new PostDelegate(client);
    }

    @Test
    public void doPost() {

        final byte[] responseData = "I LOVE SPARROWS!".getBytes();
        final Response response = mock(Response.class);

        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(response.readEntity(byte[].class)).thenReturn(responseData);
        when(builder.post(any(Entity.class))).thenReturn(response);

        final byte[] data = "BOGUS".getBytes();
        final byte[] result = delegate.doPost("http://bogus.com", ApiPath.PARTYINFO, data);
        assertThat(result).isSameAs(responseData);
    }

    @Test
    public void doPostFailure() {

        final Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        when(builder.post(any(Entity.class))).thenReturn(response);

        final byte[] data = "BOGUS".getBytes();
        final byte[] result = delegate.doPost("http://bogus.com", ApiPath.PARTYINFO, data);
        verify(response, never()).readEntity(byte[].class);
        assertThat(result).isNull();
    }

    @Test
    public void makeResendRequestSucceeds() {

        final Response response = mock(Response.class);
        doReturn(Response.Status.OK.getStatusCode()).when(response).getStatus();
        doReturn(response).when(builder).post(any(Entity.class));

        final ResendRequest request = new ResendRequest();

        final boolean success = this.delegate.makeResendRequest("http://example.com", request);

        assertThat(success).isTrue();
    }

    @Test
    public void makeResendRequestFails() {

        final Response response = mock(Response.class);
        doReturn(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).when(response).getStatus();
        doReturn(response).when(builder).post(any(Entity.class));

        final ResendRequest request = new ResendRequest();

        final boolean success = this.delegate.makeResendRequest("http://example.com", request);

        assertThat(success).isFalse();
    }

    @Test
    public void exceptionBubblesUpOnFailure() {

        doThrow(RuntimeException.class).when(builder).post(any(Entity.class));

        final Throwable throwable = catchThrowable(
            () ->delegate.makeResendRequest("randomUrl", new ResendRequest())
        );

        assertThat(throwable).isInstanceOf(RuntimeException.class);

    }

}
