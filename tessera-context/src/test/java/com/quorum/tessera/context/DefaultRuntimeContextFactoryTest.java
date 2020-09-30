package com.quorum.tessera.context;

import com.quorum.tessera.config.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

public class DefaultRuntimeContextFactoryTest extends ContextTestCase {

    private DefaultRuntimeContextFactory runtimeContextFactory;

    private ContextHolder contextHolder;

    @Before
    public void onSetUp() {
        contextHolder = mock(ContextHolder.class);
        runtimeContextFactory = new DefaultRuntimeContextFactory(contextHolder);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(contextHolder);
        MockKeyVaultConfigValidations.reset();
    }

    @Test
    public void createMinimal() {

        when(contextHolder.getContext()).thenReturn(Optional.empty());

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
        assertThat(result.isEnhancedPrivacy()).isFalse();

        verify(contextHolder).getContext();
        verify(contextHolder).setContext(any(RuntimeContext.class));
    }

    @Test
    public void createMinimalWithKeyData() {

        when(contextHolder.getContext()).thenReturn(Optional.empty());

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
        verify(contextHolder).getContext();
        verify(contextHolder).setContext(any(RuntimeContext.class));
    }

    @Test
    public void validationFailureThrowsException() {

        when(contextHolder.getContext()).thenReturn(Optional.empty());

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
            verify(contextHolder).getContext();
        }
    }

    @Test
    public void unableToCreateServer() {

        when(contextHolder.getContext()).thenReturn(Optional.empty());

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
            verify(contextHolder).getContext();
        }
    }

    @Test
    public void createMinimalRecoveryMode() {

        when(contextHolder.getContext()).thenReturn(Optional.empty());

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
        when(featureToggles.isEnablePrivacyEnhancements()).thenReturn(true);
        when(confg.getFeatures()).thenReturn(featureToggles);
        when(confg.isRecoveryMode()).thenReturn(true);

        RuntimeContext result = runtimeContextFactory.create(confg);

        assertThat(result).isNotNull();

        assertThat(result.isRecoveryMode()).isTrue();
        assertThat(result.isEnhancedPrivacy()).isTrue();
        verify(contextHolder).getContext();
        verify(contextHolder).setContext(any(RuntimeContext.class));
    }

    @Test
    public void createWithExistingContextPopulated() {
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        when(contextHolder.getContext()).thenReturn(Optional.of(runtimeContext));
        RuntimeContext result = runtimeContextFactory.create(mock(Config.class));
        verify(contextHolder).getContext();
        assertThat(result).isSameAs(runtimeContext);
    }

    @Test
    public void createDefaultInstance() {
        assertThat(new DefaultRuntimeContextFactory()).isNotNull();
    }
}
