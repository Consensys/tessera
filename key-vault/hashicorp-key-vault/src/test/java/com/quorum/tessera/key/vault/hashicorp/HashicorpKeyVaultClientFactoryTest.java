package com.quorum.tessera.key.vault.hashicorp;

import com.bettercloud.vault.VaultConfig;
import com.quorum.tessera.config.HashicorpKeyVaultConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class HashicorpKeyVaultClientFactoryTest {

    private HashicorpKeyVaultClientFactory clientFactory;

    private HashicorpKeyVaultConfig keyVaultConfig;

    private VaultConfigFactory vaultConfigFactory;

    private SslConfigFactory sslConfigFactory;

    @Before
    public void setUp() {
        this.clientFactory = new HashicorpKeyVaultClientFactory();

        this.keyVaultConfig = mock(HashicorpKeyVaultConfig.class);
        when(keyVaultConfig.getUrl()).thenReturn("url");

        this.vaultConfigFactory = mock(VaultConfigFactory.class);
        this.sslConfigFactory = mock(SslConfigFactory.class);
    }

    @Test
    public void initCreatesVaultClientWithUrlProvidedInConfig() throws Exception {
        VaultConfig vaultConfig = mock(VaultConfig.class);
        when(vaultConfigFactory.create()).thenReturn(vaultConfig);

        when(vaultConfig.address(anyString())).thenReturn(vaultConfig);

        clientFactory.init(keyVaultConfig, vaultConfigFactory, null);

        verify(vaultConfigFactory).create();
        verify(vaultConfig).address("url");

        verify(vaultConfig).build();
    }

    @Test
    public void initAddsTlsConfigToVaultClientIfProvided() throws Exception {
        Path certPath = mock(Path.class);
        when(keyVaultConfig.getTlsCertificatePath()).thenReturn(certPath);

        File certFile = mock(File.class);
        when(certPath.toFile()).thenReturn(certFile);

        VaultConfig vaultConfig = spy(VaultConfig.class);
        when(vaultConfigFactory.create()).thenReturn(vaultConfig);


        clientFactory.init(keyVaultConfig, vaultConfigFactory, null);

        verify(vaultConfigFactory).create();
        verify(vaultConfig).address("url");

        verify(vaultConfig).sslConfig(any());

        verify(vaultConfig).build();
    }

}
