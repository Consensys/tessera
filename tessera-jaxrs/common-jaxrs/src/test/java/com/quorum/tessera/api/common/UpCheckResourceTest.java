package com.quorum.tessera.api.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import jakarta.ws.rs.core.Response;
import org.junit.Test;

public class UpCheckResourceTest {

  private UpCheckResource resource = new UpCheckResource();

  @Test
  public void upCheck() {
    final Response response = resource.upCheck();
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getEntity()).isEqualTo("I'm up!");
  }
}
