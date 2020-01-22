package com.quorum.tessera.enclave;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EnclaveFactoryTest {

    private EnclaveFactory enclaveFactory;

    @Before
    public void onSetUp() {
        this.enclaveFactory = EnclaveFactory.create();
    }

    @Test
    public void create() {
        assertThat(enclaveFactory).isNotNull();
    }

    @Test
    public void createRemote() {
        final Config config = new Config();
        config.setEncryptor(
                new EncryptorConfig() {
                    {
                        setType(EncryptorType.NACL);
                    }
                });

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(AppType.ENCLAVE);
        serverConfig.setCommunicationType(CommunicationType.REST);
        serverConfig.setServerAddress("http://bogus:9898");

        config.setServerConfigs(singletonList(serverConfig));

        Enclave result = enclaveFactory.create(config);

        assertThat(result).isInstanceOf(EnclaveClient.class);
    }

    @Test
    public void dontCreateRemoteWhenNoEnclaveServer() {

        Stream.of(AppType.values())
                .filter(t -> t != AppType.ENCLAVE)
                .forEach(
                        t -> {
                            final Config config = new Config();
                            config.setEncryptor(
                                    new EncryptorConfig() {
                                        {
                                            setType(EncryptorType.NACL);
                                        }
                                    });

                            ServerConfig serverConfig = new ServerConfig();
                            serverConfig.setApp(t);
                            serverConfig.setCommunicationType(CommunicationType.REST);
                            serverConfig.setServerAddress("http://bogus:9898");

                            config.setServerConfigs(singletonList(serverConfig));

                            KeyConfiguration keyConfiguration = new KeyConfiguration();
                            ConfigKeyPair pair =
                                    new DirectKeyPair(
                                            "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=",
                                            "yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=");
                            keyConfiguration.setKeyData(singletonList(pair));
                            config.setKeys(keyConfiguration);

                            config.setAlwaysSendTo(new ArrayList<>());

                            Enclave result = enclaveFactory.create(config);

                            assertThat(result).isInstanceOf(EnclaveImpl.class);
                        });
    }

    @Test
    public void createLocal() {

        Config config = new Config();
        config.setEncryptor(
                new EncryptorConfig() {
                    {
                        setType(EncryptorType.NACL);
                    }
                });

        KeyConfiguration keyConfiguration = new KeyConfiguration();
        ConfigKeyPair pair =
                new DirectKeyPair(
                        "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=", "yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=");
        keyConfiguration.setKeyData(singletonList(pair));
        config.setKeys(keyConfiguration);

        config.setAlwaysSendTo(new ArrayList<>());

        Enclave result = enclaveFactory.create(config);

        assertThat(result).isInstanceOf(EnclaveImpl.class);
    }

    @Test
    public void createLocalExplicitly() {

        Config config = new Config();
        config.setEncryptor(
                new EncryptorConfig() {
                    {
                        setType(EncryptorType.NACL);
                    }
                });

        KeyConfiguration keyConfiguration = new KeyConfiguration();
        ConfigKeyPair pair =
                new DirectKeyPair(
                        "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=", "yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=");
        keyConfiguration.setKeyData(singletonList(pair));
        config.setKeys(keyConfiguration);

        config.setAlwaysSendTo(new ArrayList<>());

        Enclave result = enclaveFactory.createLocal(config);

        assertThat(result).isInstanceOf(EnclaveImpl.class);
    }

    @Test
    public void handleException() {
        Config config = mock(Config.class);
        EncryptorConfig encryptorConfig = mock(EncryptorConfig.class);
        when(encryptorConfig.getType()).thenThrow(new RuntimeException("OUCH"));
        when(config.getEncryptor()).thenReturn(encryptorConfig);
        try {
            enclaveFactory.create(config);
            failBecauseExceptionWasNotThrown(RuntimeException.class);
        } catch (RuntimeException ex) {
            assertThat(ex).hasMessage("OUCH");
        }
    }
}
