package com.github.nexus.config.adapters;

import com.github.nexus.config.PrivateKey;
import com.github.nexus.config.PrivateKeyType;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.nexus.config.PrivateKeyType.UNLOCKED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class PrivateKeyAdapterTest {

    private PrivateKeyAdapter adapter = new PrivateKeyAdapter();

    @Test
    public void marshallCreatesProperObject() {

        final PrivateKey privateKey = new PrivateKey(
            null, null, null, null, null, null, null, Paths.get("/some/random/path")
        );

        final PrivateKeyMutable marshalledObject = adapter.marshal(privateKey);

        assertThat(marshalledObject).isNotNull();
        assertThat(marshalledObject.getPath()).isEqualTo(Paths.get("/some/random/path"));
    }

    @Test
    public void valueIsPassedInDirect() throws IOException {

        final PrivateKeyMutable pkm = new PrivateKeyMutable(null, null, "DATA", null, null);

        final PrivateKey pk = adapter.unmarshal(pkm);

        assertThat(pk.getValue()).isEqualTo("DATA");
        assertThat(pk.getType()).isEqualTo(UNLOCKED);

    }

    @Test
    public void valuePathIsPassed() throws URISyntaxException, IOException {

        final URI uri = ClassLoader.getSystemResource("keyfile.txt").toURI();
        final Path path = Paths.get(uri);

        final PrivateKeyMutable pkm = new PrivateKeyMutable(null, path, null, null, null);

        final PrivateKey pk = adapter.unmarshal(pkm);

        assertThat(pk.getValue()).isEqualTo("SOMEDATA");
        assertThat(pk.getType()).isEqualTo(UNLOCKED);

    }

    @Test
    public void legacyPathUnlockedKeyIsUnmarshalled() throws URISyntaxException, IOException {

        final URI uri = ClassLoader.getSystemResource("unlockedprivatekey.json").toURI();
        final Path path = Paths.get(uri);
        final PrivateKeyMutable pkm = new PrivateKeyMutable(path, null, null, null, null);

        final PrivateKey result = adapter.unmarshal(pkm);

        assertThat(result.getType()).isEqualTo(PrivateKeyType.UNLOCKED);
        assertThat(result.getValue()).isEqualTo("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=");

    }

    @Test
    public void lockedKeyIsUnmarshalled() throws URISyntaxException, IOException {

        final URI uri = ClassLoader.getSystemResource("lockedprivatekey.json").toURI();
        final Path path = Paths.get(uri);
        final PrivateKeyMutable pkm = new PrivateKeyMutable(path, null, null, null, null);

        final PrivateKey result = adapter.unmarshal(pkm);

        assertThat(result.getType()).isEqualTo(PrivateKeyType.LOCKED);
        assertThat(result.getValue()).isNull();
        assertThat(result.getAsalt()).isEqualTo("7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=");
        assertThat(result.getSbox()).isEqualTo("d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc");
        assertThat(result.getSnonce()).isEqualTo("x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC");
        assertThat(result.getArgonOptions().getAlgorithm()).isEqualTo("id");
        assertThat(result.getArgonOptions().getIterations()).isEqualTo(10);
        assertThat(result.getArgonOptions().getMemory()).isEqualTo(1048576);
        assertThat(result.getArgonOptions().getParallelism()).isEqualTo(4);

    }

    @Test
    public void defaultOptionsUsed() throws IOException {
        final PrivateKeyMutable pkm = new PrivateKeyMutable(
            null,
            null,
            null,
            "PASSWORD",
            new PrivateKey("KEY-DATA", null, UNLOCKED, null, null, null, null,null)
        );

        final PrivateKey result = adapter.unmarshal(pkm);

        assertThat(result.getType()).isEqualTo(PrivateKeyType.UNLOCKED);
        assertThat(result.getValue()).isEqualTo("KEY-DATA");
        assertThat(result.getPassword()).isEqualTo("PASSWORD");
    }

}
