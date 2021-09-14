package com.quorum.tessera.api.common;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.core.Response;
import org.junit.Test;

public class BaseResourceTest {

  @Test
  public void get() {
    BaseResource baseResource = new BaseResource();
    Response response = baseResource.get();

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    assertThat(response.getEntity()).isNull();
  }
}
