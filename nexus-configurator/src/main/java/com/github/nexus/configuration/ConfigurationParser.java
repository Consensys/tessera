package com.github.nexus.configuration;

import java.util.List;
import java.util.ServiceLoader;

public interface ConfigurationParser {

    String CONFIG_FILE_PROPERTY = "configfile";

    String[] KNOWN_PROPERTIES = new String[]{
        "publicKeys", "privateKeys", "port", "url", "othernodes", "keygenBasePath", "passwords", "generatekeys"
    };

    Configuration config(PropertyLoader propertyLoader, List<String> cliParameters);

    static ConfigurationParser create() {
        final ServiceLoader<ConfigurationParser> slParser = ServiceLoader.load(ConfigurationParser.class);
        return slParser.iterator().next();
    }

}
