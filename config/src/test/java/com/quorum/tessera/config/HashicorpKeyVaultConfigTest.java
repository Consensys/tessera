package com.quorum.tessera.config;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class HashicorpKeyVaultConfigTest {

    private HashicorpKeyVaultConfig vaultConfig;

    @Before
    public void setUp() {
        vaultConfig = new HashicorpKeyVaultConfig();
    }

    @Test
    public void multiArgConstructor() {
        String url = "url";
        String approle = "approle";
        Path tlsCertPath = Paths.get("tlscertpath");
        Path tlsKeyPath = Paths.get("tlskeypath");
        Path tlsServerCertPath = Paths.get("tlsservercertpath");

        HashicorpKeyVaultConfig conf = new HashicorpKeyVaultConfig(url, approle, tlsCertPath, tlsKeyPath, tlsServerCertPath);

        assertThat(conf.getUrl()).isEqualTo(url);
        assertThat(conf.getApprolePath()).isEqualTo("approle");
        assertThat(conf.getTlsCertificatePath()).isEqualTo(tlsCertPath);
        assertThat(conf.getTlsKeyPath()).isEqualTo(tlsKeyPath);
        assertThat(conf.getTlsServerCertificatePath()).isEqualTo(tlsServerCertPath);
    }

    @Test
    public void gettersAndSetters() {
        assertThat(vaultConfig.getUrl()).isEqualTo(null);
        assertThat(vaultConfig.getTlsCertificatePath()).isEqualTo(null);
        assertThat(vaultConfig.getTlsKeyPath()).isEqualTo(null);
        assertThat(vaultConfig.getTlsServerCertificatePath()).isEqualTo(null);

        String url = "url";
        Path tlsCertPath = Paths.get("tlscertpath");
        Path tlsKeyPath = Paths.get("tlskeypath");
        Path tlsServerCertPath = Paths.get("tlsservercertpath");

        vaultConfig.setUrl(url);
        vaultConfig.setTlsCertificatePath(tlsCertPath);
        vaultConfig.setTlsKeyPath(tlsKeyPath);
        vaultConfig.setTlsServerCertificatePath(tlsServerCertPath);

        assertThat(vaultConfig.getUrl()).isEqualTo(url);
        assertThat(vaultConfig.getTlsCertificatePath()).isEqualTo(tlsCertPath);
        assertThat(vaultConfig.getTlsKeyPath()).isEqualTo(tlsKeyPath);
        assertThat(vaultConfig.getTlsServerCertificatePath()).isEqualTo(tlsServerCertPath);

    }

    @Test
    public void getType() {
        assertThat(vaultConfig.getKeyVaultType()).isEqualTo(KeyVaultType.HASHICORP);
    }

    @Test
    public void getApprolePathReturnsDefaultIfNotSet() {
        assertThat(vaultConfig.getApprolePath()).isEqualTo("approle");
    }

    @Test
    public void getApprolePath() {
        vaultConfig.setApprolePath("notdefault");
        assertThat(vaultConfig.getApprolePath()).isEqualTo("notdefault");
    }

}
