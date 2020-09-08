package com.quorum.tessera.transaction.publish;

import org.junit.Test;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class NodeOfflineExceptionTest {

    @Test
    public void createWithUri() {
        final URI uri = URI.create("http://dddd.com");
        NodeOfflineException exception = new NodeOfflineException(uri);

        assertThat(exception.getUri()).isEqualTo(uri);
        assertThat(exception).hasMessageContaining(uri.toString());

    }

}
