package com.github.nexus.node;

import com.github.nexus.api.model.ApiPath;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PostDelegateTest {

    private PostDelegate delegate;

    private Invocation.Builder builder;

    @Before
    public void onSetup() {

        Client client = mock(Client.class);
        WebTarget webTarget = mock(WebTarget.class);
        builder = mock(Invocation.Builder.class);
        when(webTarget.request()).thenReturn(builder);

        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.path(anyString())).thenReturn(webTarget);

        delegate = new PostDelegate(client);

    }

    @Test
    public void doPost() {
        byte[] responseData = "I LOVE SPARROWS!".getBytes();
        Response response = mock(Response.class);
        when(response.readEntity(byte[].class)).thenReturn(responseData);
        when(builder.post(any(Entity.class))).thenReturn(response);
        byte[] data = "BOGUS".getBytes();
        byte[]  result = delegate.doPost("http://bogus.com",ApiPath.PARTYINFO,data);
        assertThat(result).isSameAs(responseData);
    }
}
