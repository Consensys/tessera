package com.quorum.tessera.api.common;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseResourceTest {

    @Test
    public void get() {
        BaseResource baseResource = new BaseResource();
        Response response = baseResource.get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isNull();
    }

}
