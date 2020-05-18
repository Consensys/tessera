package com.quorum.tessera.launcher;

import com.quorum.tessera.config.*;

import java.util.List;

public class TestConfig {

    protected Config serverConfig() {
        final Config config = new Config();

        final ServerConfig p2pConfig = new ServerConfig();
        p2pConfig.setApp(AppType.P2P);
        p2pConfig.setCommunicationType(CommunicationType.REST);
        p2pConfig.setServerAddress("http://localhost:9001/");

        final ServerConfig q2tConfig = new ServerConfig();
        q2tConfig.setApp(AppType.Q2T);
        q2tConfig.setCommunicationType(CommunicationType.REST);
        q2tConfig.setServerAddress("http://localhost:22000/");

        final ServerConfig thirdPartyConfig = new ServerConfig();
        thirdPartyConfig.setApp(AppType.THIRD_PARTY);
        thirdPartyConfig.setCommunicationType(CommunicationType.REST);
        thirdPartyConfig.setServerAddress("http://localhost:22000/");

        config.setServerConfigs(List.of(p2pConfig, q2tConfig, thirdPartyConfig));

        return config;
    }

    protected Config invalidConfig() {
        final Config config = new Config();

        final ServerConfig adminConfig = new ServerConfig();
        adminConfig.setApp(AppType.ADMIN);
        adminConfig.setCommunicationType(CommunicationType.REST);
        adminConfig.setServerAddress("http://localhost:8989/");

        config.setServerConfigs(List.of(adminConfig));

        return config;
    }
}
