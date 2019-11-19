package com.quorum.tessera.config;

import com.quorum.tessera.config.util.JaxbUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;

public class JaxbConfigFactoryTest {

    private JaxbConfigFactory factory;

    @Before
    public void init() {
        this.factory = new JaxbConfigFactory();
    }

    @Test
    public void createMinimal() {

        Config config = new Config();
        config.setEncryptor(
                new EncryptorConfig() {
                    {
                        setType(EncryptorType.NACL);
                    }
                });

        InputStream in =
                Optional.of(config)
                        .map(JaxbUtil::marshalToStringNoValidation)
                        .map(String::getBytes)
                        .map(ByteArrayInputStream::new)
                        .get();

        JaxbUtil.marshalToStringNoValidation(config);

        Config result = factory.create(in);

        assertThat(result).isNotNull();
    }
}
