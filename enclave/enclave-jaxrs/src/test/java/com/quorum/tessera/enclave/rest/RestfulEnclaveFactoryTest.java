package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveImpl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class RestfulEnclaveFactoryTest {

    private RestfulEnclaveFactory enclaveFactory = new RestfulEnclaveFactory();

    @Test
    public void createRemote() {
        final Config config = new Config();

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setEnabled(true);
        serverConfig.setApp(AppType.ENCLAVE);
        serverConfig.setCommunicationType(CommunicationType.REST);
        serverConfig.setServerSocket(new InetServerSocket("http://bogus", 9898));

        config.setServerConfigs(Arrays.asList(serverConfig));

        Enclave result = enclaveFactory.create(config);

        assertThat(result).isInstanceOf(EnclaveClient.class);

    }

    @Test
    public void dontCreateRemoteWhenNoEnclaveServer() {

        Stream.of(AppType.values()).filter(t -> t != AppType.ENCLAVE).forEach(t -> {

            final Config config = new Config();

            ServerConfig serverConfig = new ServerConfig();
            serverConfig.setApp(t);
            serverConfig.setCommunicationType(CommunicationType.REST);
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
    
    
    @Test
    public void dontCreateRemoteWhenNoCommType() {

            Config config = new Config();

            ServerConfig serverConfig = new ServerConfig();
            serverConfig.setApp(AppType.ENCLAVE);
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



    }
}
