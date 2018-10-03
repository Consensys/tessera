package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.keypairs.*;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.quorum.tessera.config.PrivateKeyType.UNLOCKED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

public class KeyDataAdapterTest {

    private KeyDataAdapter adapter = new KeyDataAdapter();

    @Test
    public void marshallDirectKeys() {
        final ConfigKeyPair keys = new DirectKeyPair("PUB", "PRIV");
        final KeyData expected = new KeyData(null, "PRIV", "PUB", null, null);

        final KeyData marshalledKey = adapter.marshal(keys);

        assertThat(marshalledKey).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void marshallInlineKeys() {
        final PrivateKeyData pkd = new PrivateKeyData("val", null, null, null, null, null);
        final ConfigKeyPair keys = new InlineKeypair("PUB", new KeyDataConfig(pkd, UNLOCKED));
        final KeyData expected = new KeyData(new KeyDataConfig(pkd, UNLOCKED), null, "PUB", null, null);

        final KeyData marshalledKey = adapter.marshal(keys);

        assertThat(marshalledKey).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void marshallFilesystemKeys() {
        final Path path = mock(Path.class);
        final FilesystemKeyPair keyPair = new FilesystemKeyPair(path, path);

        final KeyData expected = new KeyData(null, null, null, path, path);
        final KeyData result = adapter.marshal(keyPair);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void marshallUnsupportedKeys() {
        final KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        final Path path = mock(Path.class);
        final UnsupportedKeyPair keyPair = new UnsupportedKeyPair(keyDataConfig, "priv", null, path, null);

        final KeyData expected = new KeyData(keyDataConfig, "priv", null, path, null);
        final KeyData result = adapter.marshal(keyPair);

        assertThat(result).isEqualTo(expected);
    }

    class UnknownKeyPair implements ConfigKeyPair {

        @Override
        public String getPublicKey() {
            return null;
        }

        @Override
        public String getPrivateKey() {
            return null;
        }

        @Override
        public void withPassword(String password) {
            //do nothing
        }

        @Override
        public String getPassword() {
            return null;
        }
    }

    @Test
    public void marshallUnknownKeyPairType() {
        final ConfigKeyPair keyPair = new UnknownKeyPair();

        Throwable ex = catchThrowable(() -> adapter.marshal(keyPair));

        assertThat(ex).isInstanceOf(UnsupportedOperationException.class);
        assertThat(ex).hasMessage("The keypair type " + keyPair.getClass() + " is not allowed");
    }

    @Test
    public void marshallLockedKeyNullifiesPrivateKey() {
        final PrivateKeyData pkd = new PrivateKeyData("val", null, null, null, null, "password");
        final ConfigKeyPair keys = new InlineKeypair("PUB", new KeyDataConfig(pkd, UNLOCKED));

        final KeyData marshalledKey = adapter.marshal(keys);

        assertThat(marshalledKey.getPrivateKey()).isNull();

    }

    @Test
    public void unmarshallingDirectKeysGivesCorrectKeypair() {
        final KeyData input = new KeyData(null, "private", "public", null, null);

        final ConfigKeyPair result = this.adapter.unmarshal(input);
        assertThat(result).isInstanceOf(DirectKeyPair.class);

    }

    @Test
    public void unmarshallingInlineKeysGivesCorrectKeypair() {
        final KeyData input = new KeyData(new KeyDataConfig(null, null), null, "public", null, null);

        final ConfigKeyPair result = this.adapter.unmarshal(input);
        assertThat(result).isInstanceOf(InlineKeypair.class);

    }

    @Test
    public void unmarshallingFilesystemKeysGivesCorrectKeypair() {
        final KeyData input = new KeyData(null, null, null, Paths.get("private"), Paths.get("public"));

        final ConfigKeyPair result = this.adapter.unmarshal(input);
        assertThat(result).isInstanceOf(FilesystemKeyPair.class);

    }

    @Test
    public void unmarshallingPrivateOnlyGivesUnsupportedKeyPair() {
        final KeyData input = new KeyData(null, "private", null, null, null);

        final ConfigKeyPair result = this.adapter.unmarshal(input);
        assertThat(result).isInstanceOf(UnsupportedKeyPair.class);
    }

    @Test
    public void unmarshallingPrivateConfigOnlyGivesUnsupportedKeyPair() {
        final KeyDataConfig keyDataConfig = mock(KeyDataConfig.class);
        final KeyData input = new KeyData(keyDataConfig, null, null, null, null);

        final ConfigKeyPair result = this.adapter.unmarshal(input);
        assertThat(result).isInstanceOf(UnsupportedKeyPair.class);
    }

    @Test
    public void unmarshallingPublicPathOnlyGivesUnsupportedKeyPair() {
        final Path path = mock(Path.class);
        final KeyData input = new KeyData(null, null, null, null, path);

        final ConfigKeyPair result = this.adapter.unmarshal(input);
        assertThat(result).isInstanceOf(UnsupportedKeyPair.class);
    }

    @Test
    public void unmarshallingPrivatePathOnlyGivesUnsupportedKeyPair() {
        final Path path = mock(Path.class);
        final KeyData input = new KeyData(null, null, null, path, null);

        final ConfigKeyPair result = this.adapter.unmarshal(input);
        assertThat(result).isInstanceOf(UnsupportedKeyPair.class);
    }

}
