package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.InetServerSocket;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import com.quorum.tessera.encryption.Enclave;
import com.quorum.tessera.encryption.EnclaveImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class DefaultEnclaveFactoryTest {

    private DefaultEnclaveFactory enclaveFactory = new DefaultEnclaveFactory();

    @Test
    public void createRemote() {
        final Config config = new Config();

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setEnabled(true);
        serverConfig.setApp(AppType.ENCLAVE);
        serverConfig.setServerSocket(new InetServerSocket("http://bogus", 9898));

        config.setServerConfigs(Arrays.asList(serverConfig));

        Enclave result = enclaveFactory.create(config);

        assertThat(result).isInstanceOf(EnclaveClient.class);

    }

    @Test
    public void dontCreateRemoteWhenNoEnclaveServer() {

        Stream.of(AppType.values()).filter(t -> t != AppType.ENCLAVE).forEach(t -> {

            Config config = new Config();

            ServerConfig serverConfig = new ServerConfig();
            serverConfig.setApp(t);
            serverConfig.setServerSocket(new InetServerSocket("http://bogus", 9898));

            config.setServerConfigs(Arrays.asList(serverConfig));

            KeyConfiguration keyConfiguration = new KeyConfiguration();
            ConfigKeyPair pair = new DirectKeyPair("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=", "yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=");
            keyConfiguration.setKeyData(Arrays.asList(pair));
            config.setKeys(keyConfiguration);

            config.setAlwaysSendTo(new ArrayList<>());

            Enclave result = enclaveFactory.create(config);

            assertThat(result)
                    .isNotInstanceOf(EnclaveClient.class)
                    .isInstanceOf(EnclaveImpl.class);

        });

    }

    @Test
    public void createLocal() {

        Config config = new Config();

        KeyConfiguration keyConfiguration = new KeyConfiguration();
        ConfigKeyPair pair = new DirectKeyPair("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=", "yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=");
        keyConfiguration.setKeyData(Arrays.asList(pair));
        config.setKeys(keyConfiguration);

        config.setAlwaysSendTo(new ArrayList<>());

        Enclave result = enclaveFactory.create(config);

        assertThat(result).isInstanceOf(EnclaveImpl.class);

    }
}
