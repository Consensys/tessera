package com.quorum.tessera.config;

import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.JaxbUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class JaxbConfigFactoryTest {

    private JaxbConfigFactory factory;

    private KeyEncryptorFactory keyEncryptorFactory;

    @Before
    public void beforeTest() {
        keyEncryptorFactory = mock(KeyEncryptorFactory.class);
        this.factory = new JaxbConfigFactory(keyEncryptorFactory);
    }

    @After
    public void afterTest() {
        verifyNoMoreInteractions(keyEncryptorFactory);
    }

    @Test
    public void createMinimal() {

        final EncryptorConfig encryptorConfig = new EncryptorConfig() {{
            setType(EncryptorType.NACL);
        }};

        Config config = new Config();
        config.setEncryptor(encryptorConfig);

        InputStream in =
                Optional.of(config)
                        .map(JaxbUtil::marshalToStringNoValidation)
                        .map(String::getBytes)
                        .map(ByteArrayInputStream::new)
                        .get();

        JaxbUtil.marshalToStringNoValidation(config);

        Config result = factory.create(in);

        assertThat(result).isNotNull();

        verify(keyEncryptorFactory).create(any(EncryptorConfig.class));
    }
}
