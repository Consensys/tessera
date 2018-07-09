package com.github.tessera.config;

import com.github.tessera.config.util.JaxbUtil;
import com.github.tessera.config.keys.KeyGenerator;
import com.github.tessera.config.keys.KeyGeneratorFactory;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JaxbConfigFactory implements ConfigFactory {

    private final KeyGenerator generator = KeyGeneratorFactory.create();

    @Override
    public Config create(final InputStream configData, final InputStream... keyConfigData) {

        final List<KeyData> newKeys = Stream.of(keyConfigData)
            .map(kcd -> JaxbUtil.unmarshal(kcd, KeyDataConfig.class))
            .map(generator::generate)
            .collect(Collectors.toList());

        final Config config = JaxbUtil.unmarshal(configData, Config.class);
        config.getKeys().addAll(newKeys);

        return config;
    }

}
