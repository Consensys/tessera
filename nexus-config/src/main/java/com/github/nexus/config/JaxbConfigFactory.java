package com.github.nexus.config;

import com.github.nexus.config.util.JaxbUtil;
import java.io.InputStream;

public class JaxbConfigFactory implements ConfigFactory {

    @Override
    public Config create(InputStream configData, InputStream... keyConfigData) {

        for (InputStream d : keyConfigData) {
            KeyDataConfig keyDataConfig = JaxbUtil.unmarshal(d, KeyDataConfig.class);
            KeyDataConfigStore.INSTANCE.push(keyDataConfig);

        }
        return JaxbUtil.unmarshal(configData, Config.class);
    }

}
