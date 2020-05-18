package com.quorum.tessera.context;

import com.quorum.tessera.config.*;
import org.junit.After;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultRuntimeContextFactoryTest extends ContextTestCase {

    private DefaultRuntimeContextFactory runtimeContextFactory = new DefaultRuntimeContextFactory();

    @After
    public void onTearDown() {
        MockKeyVaultConfigValidations.reset();
    }

    @Test
    public void createMinimal() {

        Config confg = mock(Config.class);
        EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
        when(encryptorConfig.getType()).thenReturn(EncryptorType.NACL);

        when(confg.getEncryptor()).thenReturn(encryptorConfig);

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);

        when(confg.getKeys()).thenReturn(keyConfiguration);

        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getApp()).thenReturn(AppType.P2P);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        when(confg.getP2PServerConfig()).thenReturn(serverConfig);
        when(serverConfig.getServerUri()).thenReturn(URI.create("http://bogus"));
        when(serverConfig.getBindingUri()).thenReturn(URI.create("http://bogus"));
        when(serverConfig.getProperties()).thenReturn(Collections.emptyMap());

        when(confg.getServerConfigs()).thenReturn(List.of(serverConfig));

        FeatureToggles featureToggles = mock(FeatureToggles.class);
        when(confg.getFeatures()).thenReturn(featureToggles);

        RuntimeContext result = runtimeContextFactory.create(confg);

        assertThat(result).isNotNull();

        assertThat(result.isRecoveryMode()).isFalse();
    }

    @Test
    public void createMinimalWithKeyData() {

        Config confg = mock(Config.class);
        EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
        when(encryptorConfig.getType()).thenReturn(EncryptorType.NACL);

        when(confg.getEncryptor()).thenReturn(encryptorConfig);

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);

        when(confg.getKeys()).thenReturn(keyConfiguration);

        KeyData keyData = new KeyData();

        keyData.setPublicKey(Base64.getEncoder().encodeToString("PUBLICKEY".getBytes()));
        keyData.setPrivateKey(Base64.getEncoder().encodeToString("PRIVATEKEY".getBytes()));

        when(keyConfiguration.getKeyData()).thenReturn(Arrays.asList(keyData));

        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getApp()).thenReturn(AppType.P2P);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        when(confg.getP2PServerConfig()).thenReturn(serverConfig);
        when(serverConfig.getServerUri()).thenReturn(URI.create("http://bogus"));
        when(serverConfig.getBindingUri()).thenReturn(URI.create("http://bogus"));
        when(serverConfig.getProperties()).thenReturn(Collections.emptyMap());

        when(confg.getServerConfigs()).thenReturn(List.of(serverConfig));

        FeatureToggles featureToggles = mock(FeatureToggles.class);
        when(confg.getFeatures()).thenReturn(featureToggles);

        RuntimeContext result = runtimeContextFactory.create(confg);

        assertThat(result).isNotNull();
    }

    @Test
    public void validationFailureThrowsException() {

        Config confg = mock(Config.class);
        EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
        when(encryptorConfig.getType()).thenReturn(EncryptorType.NACL);

        when(confg.getEncryptor()).thenReturn(encryptorConfig);

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);

        when(confg.getKeys()).thenReturn(keyConfiguration);

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        MockKeyVaultConfigValidations.addConstraintViolation(violation);

        try {
            runtimeContextFactory.create(confg);
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException ex) {
            assertThat(ex.getConstraintViolations()).containsExactly(violation);
        }
    }

    @Test
    public void unableToCreateServer() {

        Config confg = mock(Config.class);
        EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
        when(encryptorConfig.getType()).thenReturn(EncryptorType.NACL);

        when(confg.getEncryptor()).thenReturn(encryptorConfig);

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);

        when(confg.getKeys()).thenReturn(keyConfiguration);

        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.WEB_SOCKET);
        when(serverConfig.getApp()).thenReturn(AppType.THIRD_PARTY);
        when(serverConfig.getServerUri()).thenReturn(URI.create("http://bogus"));
        when(serverConfig.getBindingUri()).thenReturn(URI.create("http://bogus"));
        when(confg.getServerConfigs()).thenReturn(Arrays.asList(serverConfig));

        try {
            runtimeContextFactory.create(confg);
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException ex) {
            assertThat(ex).hasMessage("No P2P server configured");
        }
    }

    @Test
    public void createMinimalRecoveryMode() {

        Config confg = mock(Config.class);
        EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
        when(encryptorConfig.getType()).thenReturn(EncryptorType.NACL);

        when(confg.getEncryptor()).thenReturn(encryptorConfig);

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);

        when(confg.getKeys()).thenReturn(keyConfiguration);

        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getApp()).thenReturn(AppType.P2P);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        when(confg.getP2PServerConfig()).thenReturn(serverConfig);
        when(serverConfig.getServerUri()).thenReturn(URI.create("http://bogus"));
        when(serverConfig.getBindingUri()).thenReturn(URI.create("http://bogus"));
        when(serverConfig.getProperties()).thenReturn(Collections.emptyMap());

        when(confg.getServerConfigs()).thenReturn(List.of(serverConfig));

        FeatureToggles featureToggles = mock(FeatureToggles.class);
        when(confg.getFeatures()).thenReturn(featureToggles);
        when(confg.isRecoveryMode()).thenReturn(true);

        RuntimeContext result = runtimeContextFactory.create(confg);

        assertThat(result).isNotNull();

        assertThat(result.isRecoveryMode()).isTrue();
    }
}
