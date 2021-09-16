package com.quorum.tessera.test.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.shared.Constants;
import jakarta.ws.rs.core.Response;

public interface AssertApiHeaders {

  static void doAsserts(Response response) {

    assertThat(response.getHeaders()).containsKey(Constants.API_VERSION_HEADER);

    assertThat(response.getHeaders().get(Constants.API_VERSION_HEADER)).isNotEmpty();
  }
}
