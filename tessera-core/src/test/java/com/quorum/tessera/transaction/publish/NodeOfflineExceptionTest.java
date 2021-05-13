package com.quorum.tessera.transaction.publish;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.Test;

public class NodeOfflineExceptionTest {

  @Test
  public void createWithUri() {
    final URI uri = URI.create("http://dddd.com");
    NodeOfflineException exception = new NodeOfflineException(uri);

    assertThat(exception.getUri()).isEqualTo(uri);
    assertThat(exception).hasMessageContaining(uri.toString());
  }
}
