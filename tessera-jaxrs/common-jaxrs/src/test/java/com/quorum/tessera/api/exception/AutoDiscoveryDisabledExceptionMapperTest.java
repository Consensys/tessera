package com.quorum.tessera.api.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.partyinfo.AutoDiscoveryDisabledException;
import jakarta.ws.rs.core.Response;
import org.junit.Test;

public class AutoDiscoveryDisabledExceptionMapperTest {

  private AutoDiscoveryDisabledExceptionMapper mapper = new AutoDiscoveryDisabledExceptionMapper();

  @Test
  public void handleAutoDiscoveryDisabledException() {
    final String message = ".. all outta gum";
    final AutoDiscoveryDisabledException exception = new AutoDiscoveryDisabledException(message);

    final Response result = mapper.toResponse(exception);

    assertThat(result.getStatus()).isEqualTo(403);
    assertThat(result.getEntity()).isEqualTo(message);
  }
}
