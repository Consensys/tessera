package com.quorum.tessera.enclave;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.util.JaxbUtil;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class EnclaveProviderTest {

    @Test
    public void provider() {

        try(var staticConfigFactory = mockStatic(ConfigFactory.class)) {

            ConfigFactory configFactory = mock(ConfigFactory.class);
            Config config = JaxbUtil.unmarshal(getClass().getResourceAsStream("/sample.json"),Config.class);
            when(configFactory.getConfig()).thenReturn(config);
            staticConfigFactory.when(ConfigFactory::create).thenReturn(configFactory);


            Enclave enclave = Enclave.create();

            assertThat(enclave).isNotNull();
        }
    }

}
