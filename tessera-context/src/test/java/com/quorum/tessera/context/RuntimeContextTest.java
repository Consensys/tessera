package com.quorum.tessera.context;

import com.quorum.tessera.config.keys.KeyEncryptor;
import org.junit.Test;
import javax.ws.rs.client.Client;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RuntimeContextTest extends ContextTestCase {

    @Test
    public void createMinimal() {

        final KeyEncryptor keyEncryptor = mock(KeyEncryptor.class);
        final Client client = mock(Client.class);
        final URI uri = URI.create("http://bogus");

        final RuntimeContext runtimeContext =
                RuntimeContextBuilder.create()
                        .withKeyEncryptor(keyEncryptor)
                        .withP2pClient(client)
                        .withP2pServerUri(uri)
                        .build();

        assertThat(runtimeContext).isNotNull();
        assertThat(runtimeContext.getP2pServerUri()).isSameAs(uri);
        assertThat(runtimeContext.getKeyEncryptor()).isSameAs(keyEncryptor);
        assertThat(runtimeContext.getP2pClient()).isSameAs(client);
        assertThat(runtimeContext.getAlwaysSendTo()).isEmpty();
        assertThat(runtimeContext.getKeys()).isEmpty();
        assertThat(runtimeContext.getPublicKeys()).isEmpty();
        assertThat(runtimeContext.getPeers()).isEmpty();
        assertThat(runtimeContext.isRemoteKeyValidation()).isFalse();
        assertThat(runtimeContext.isUseWhiteList()).isFalse();
        assertThat(runtimeContext.isRecoveryMode()).isFalse();

        assertThat(runtimeContext.isDisablePeerDiscovery()).isFalse();

        assertThat(runtimeContext.toString()).isNotEmpty();
    }

    @Test
    public void getInstance() {
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        DefaultContextHolder.INSTANCE.setContext(runtimeContext);
        assertThat(runtimeContext).isSameAs(RuntimeContext.getInstance());
    }
}
