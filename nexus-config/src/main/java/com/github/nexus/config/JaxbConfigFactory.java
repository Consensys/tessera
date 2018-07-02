package com.github.nexus.config;

import com.github.nexus.config.util.JaxbUtil;
import java.io.InputStream;

public class JaxbConfigFactory implements ConfigFactory {

    @Override
    public Config create(InputStream inputStream) {
        return JaxbUtil.unmarshal(inputStream, Config.class);
    }

}
