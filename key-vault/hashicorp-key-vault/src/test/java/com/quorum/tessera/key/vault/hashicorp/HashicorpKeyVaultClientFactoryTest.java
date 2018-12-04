package com.quorum.tessera.key.vault.hashicorp;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.VaultConfig;
import com.quorum.tessera.config.HashicorpKeyVaultConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class HashicorpKeyVaultClientFactoryTest {

    private HashicorpKeyVaultClientFactory keyVaultClientFactory;

    private HashicorpKeyVaultConfig keyVaultConfig;

    private VaultConfigFactory vaultConfigFactory;

    private SslConfigFactory sslConfigFactory;

    @Before
    public void setUp() {
        this.keyVaultClientFactory = new HashicorpKeyVaultClientFactory();
        this.keyVaultConfig = mock(HashicorpKeyVaultConfig.class);
        when(keyVaultConfig.getUrl()).thenReturn("url");
        this.vaultConfigFactory = mock(VaultConfigFactory.class, RETURNS_DEEP_STUBS);
        this.sslConfigFactory = mock(SslConfigFactory.class);
    }

    @Test
    public void tlsConfigAddedToUnauthenticatedVaultClientIfProvided() throws Exception {
        Path certPath = mock(Path.class);
        when(keyVaultConfig.getTlsCertificatePath()).thenReturn(certPath);
        File certFile = mock(File.class);
        when(certPath.toFile()).thenReturn(certFile);

        VaultConfig vaultConfig = mock(VaultConfig.class);
        when(vaultConfigFactory.create().address(anyString())).thenReturn(vaultConfig);
        when(vaultConfig.build()).thenReturn(vaultConfig);

        SslConfig sslConfig = mock(SslConfig.class, RETURNS_DEEP_STUBS);
        when(sslConfigFactory.create()).thenReturn(sslConfig);

        when(sslConfig.pemFile(any(File.class)).build()).thenReturn(sslConfig);

        keyVaultClientFactory.createUnauthenticatedClient(keyVaultConfig, vaultConfigFactory, sslConfigFactory);

        verify(keyVaultConfig, times(2)).getTlsCertificatePath();
        verify(vaultConfig).sslConfig(sslConfig);
    }

    @Test
    public void tlsConfigNotAddedToUnauthenticatedVaultClientIfNotProvided() throws Exception {
        when(keyVaultConfig.getTlsCertificatePath()).thenReturn(null);

        VaultConfig vaultConfig = mock(VaultConfig.class);
        when(vaultConfigFactory.create().address(anyString())).thenReturn(vaultConfig);
        when(vaultConfig.build()).thenReturn(vaultConfig);

        keyVaultClientFactory.createUnauthenticatedClient(keyVaultConfig, vaultConfigFactory, sslConfigFactory);

        verify(vaultConfig, never()).sslConfig(any(SslConfig.class));
    }

    @Test
    public void tlsConfigAddedToAuthenticatedVaultClientIfProvided() throws Exception {
        Path certPath = mock(Path.class);
        when(keyVaultConfig.getTlsCertificatePath()).thenReturn(certPath);
        File certFile = mock(File.class);
        when(certPath.toFile()).thenReturn(certFile);

        VaultConfig vaultConfig = mock(VaultConfig.class);
        when(vaultConfigFactory.create().address(anyString())).thenReturn(vaultConfig);
        when(vaultConfig.build()).thenReturn(vaultConfig);

        SslConfig sslConfig = mock(SslConfig.class, RETURNS_DEEP_STUBS);
        when(sslConfigFactory.create()).thenReturn(sslConfig);

        when(sslConfig.pemFile(any(File.class)).build()).thenReturn(sslConfig);

        keyVaultClientFactory.createAuthenticatedClient(keyVaultConfig, vaultConfigFactory, sslConfigFactory, "sometoken");

        verify(keyVaultConfig, times(2)).getTlsCertificatePath();
        verify(vaultConfig).sslConfig(sslConfig);
    }

    @Test
    public void tlsConfigNotAddedToAuthenticatedVaultClientIfNotProvided() throws Exception {
        when(keyVaultConfig.getTlsCertificatePath()).thenReturn(null);

        VaultConfig vaultConfig = mock(VaultConfig.class);
        when(vaultConfigFactory.create().address(anyString())).thenReturn(vaultConfig);
        when(vaultConfig.build()).thenReturn(vaultConfig);

        keyVaultClientFactory.createAuthenticatedClient(keyVaultConfig, vaultConfigFactory, sslConfigFactory, "sometoken");

        verify(vaultConfig, never()).sslConfig(any(SslConfig.class));
    }

    @Test
    public void tokenGetsAddedToAuthenticateVaultClientConfig() throws Exception {
        VaultConfig vaultConfig = mock(VaultConfig.class);
        when(vaultConfigFactory.create().address(anyString())).thenReturn(vaultConfig);
        when(vaultConfig.build()).thenReturn(vaultConfig);

        keyVaultClientFactory.createAuthenticatedClient(keyVaultConfig, vaultConfigFactory, sslConfigFactory, "sometoken");

        verify(vaultConfig).token("sometoken");
    }

}
