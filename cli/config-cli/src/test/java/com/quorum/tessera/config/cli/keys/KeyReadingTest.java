package com.quorum.tessera.config.cli.keys;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.cli.DefaultCliAdapter;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.util.JaxbUtil;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyReadingTest {

    private DefaultCliAdapter adapter = new DefaultCliAdapter();

    @Test
    public void publicPrivateInlineUnlocked() {
        final Config config
            = JaxbUtil.unmarshal(getClass().getResourceAsStream("/keytests/pubPrivInlineUnlocked.json"), Config.class);
        adapter.updateKeyPasswords(config);

        assertThat(config).isNotNull();
        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).isNotNull().hasSize(1);
        assertThat(config.getKeys().getKeyData().get(0).getPublicKey()).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        assertThat(config.getKeys().getKeyData().get(0).getPrivateKey()).isEqualTo("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=");

    }

    @Test
    public void publicPrivateInlineLocked() {
        final Config config
            = JaxbUtil.unmarshal(getClass().getResourceAsStream("/keytests/pubPrivInlineLocked.json"), Config.class);
        adapter.updateKeyPasswords(config);

        assertThat(config).isNotNull();
        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).isNotNull().hasSize(1);
        assertThat(config.getKeys().getKeyData().get(0).getPublicKey()).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        assertThat(config.getKeys().getKeyData().get(0).getPrivateKey()).isEqualTo("6ccai0+GXRRVbNckE+JubN+UQ9+8pMCx86dZI683X7w=");

    }

    @Test
    public void passwordsInFile() {
        final Config config
            = JaxbUtil.unmarshal(getClass().getResourceAsStream("/keytests/pubPrivPasswordsFile.json"), Config.class);
        adapter.updateKeyPasswords(config);

        assertThat(config).isNotNull();
        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).isNotNull().hasSize(1);
        ConfigKeyPair keyPair = config.getKeys().getKeyData().get(0);
        assertThat(keyPair.getPublicKey()).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        assertThat(keyPair.getPrivateKey()).isEqualTo("6ccai0+GXRRVbNckE+JubN+UQ9+8pMCx86dZI683X7w=");

    }

    @Test
    public void pubPrivUsingPassLocked() {
        final Config config = JaxbUtil.unmarshal(
            getClass().getResourceAsStream("/keytests/pubPrivUsingPathsLocked.json"), Config.class
        );
        adapter.updateKeyPasswords(config);

        assertThat(config).isNotNull();
        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).isNotNull().hasSize(1);
        assertThat(config.getKeys().getKeyData().get(0).getPublicKey()).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        assertThat(config.getKeys().getKeyData().get(0).getPrivateKey()).isEqualTo("6ccai0+GXRRVbNckE+JubN+UQ9+8pMCx86dZI683X7w=");

    }

    @Test
    public void pubPrivUsingPassUnlocked() {

        final Config config = JaxbUtil.unmarshal(
            getClass().getResourceAsStream("/keytests/pubPrivUsingPathsUnlocked.json"), Config.class
        );
        adapter.updateKeyPasswords(config);

        assertThat(config).isNotNull();
        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).isNotNull().hasSize(1);
        assertThat(config.getKeys().getKeyData().get(0).getPublicKey()).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        assertThat(config.getKeys().getKeyData().get(0).getPrivateKey()).isEqualTo("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=");

    }

    @Test
    public void wrongPasswordsProvided() {
        final Config config
            = JaxbUtil.unmarshal(getClass().getResourceAsStream("/keytests/passwordsWrong.json"), Config.class);
        adapter.updateKeyPasswords(config);

        //a null response indicates an error occurred
        assertThat(config.getKeys().getKeyData()).hasSize(1);
        assertThat(config.getKeys().getKeyData().get(0).getPrivateKey()).startsWith("NACL_FAILURE");
    }

}
