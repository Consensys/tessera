package com.quorum.tessera.config;

import com.quorum.tessera.config.util.JaxbUtil;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyReadingTest {

    @Test
    public void publicPrivateInlineUnlocked() throws IOException {

        final Config config = JaxbUtil.unmarshal(
            getClass().getResource("/keytests/pubPrivInlineUnlocked.json").openStream(), Config.class
        );

        assertThat(config).isNotNull();
        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).isNotNull().hasSize(1);
        assertThat(config.getKeys().getKeyData().get(0).getPublicKey()).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        assertThat(config.getKeys().getKeyData().get(0).getPrivateKey()).isEqualTo("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=");

    }

    @Test
    public void publicPrivateInlineLocked() throws IOException {

        final Config config = JaxbUtil.unmarshal(
            getClass().getResource("/keytests/pubPrivInlineLocked.json").openStream(), Config.class
        );

        assertThat(config).isNotNull();
        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).isNotNull().hasSize(1);
        assertThat(config.getKeys().getKeyData().get(0).getPublicKey()).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        assertThat(config.getKeys().getKeyData().get(0).getPrivateKey()).isEqualTo("6ccai0+GXRRVbNckE+JubN+UQ9+8pMCx86dZI683X7w=");

    }

    @Test
    public void passwordsInFile() throws IOException {

        final Config config = JaxbUtil.unmarshal(
            getClass().getResource("/keytests/pubPrivPasswordsFile.json").openStream(), Config.class
        );

        assertThat(config).isNotNull();
        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).isNotNull().hasSize(1);
        assertThat(config.getKeys().getKeyData().get(0).getPublicKey()).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        assertThat(config.getKeys().getKeyData().get(0).getPrivateKey()).isEqualTo("6ccai0+GXRRVbNckE+JubN+UQ9+8pMCx86dZI683X7w=");

    }

    @Test
    public void pubPrivUsingPassLocked() throws IOException {

        final Config config = JaxbUtil.unmarshal(
            getClass().getResource("/keytests/pubPrivUsingPathsLocked.json").openStream(), Config.class
        );

        assertThat(config).isNotNull();
        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).isNotNull().hasSize(1);
        assertThat(config.getKeys().getKeyData().get(0).getPublicKey()).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        assertThat(config.getKeys().getKeyData().get(0).getPrivateKey()).isEqualTo("6ccai0+GXRRVbNckE+JubN+UQ9+8pMCx86dZI683X7w=");

    }

    @Test
    public void pubPrivUsingPassUnlocked() throws IOException {

        final Config config = JaxbUtil.unmarshal(
            getClass().getResource("/keytests/pubPrivUsingPathsUnlocked.json").openStream(), Config.class
        );

        assertThat(config).isNotNull();
        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getKeyData()).isNotNull().hasSize(1);
        assertThat(config.getKeys().getKeyData().get(0).getPublicKey()).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");
        assertThat(config.getKeys().getKeyData().get(0).getPrivateKey()).isEqualTo("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=");

    }


    @Test
    public void wrongPasswordsProvided() throws IOException {

        final Config config = JaxbUtil.unmarshal(
            getClass().getResource("/keytests/passwordsWrong.json").openStream(), Config.class
        );

        //a null response indicates an error occured
        assertThat(config.getKeys().getKeyData()).hasSize(1);
        assertThat(config.getKeys().getKeyData().get(0).getPrivateKey()).startsWith("NACL_FAILURE");
    }



}
