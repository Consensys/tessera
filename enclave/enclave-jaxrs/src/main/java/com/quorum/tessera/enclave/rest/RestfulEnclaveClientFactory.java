package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.enclave.EnclaveClientFactory;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import java.util.Optional;
import javax.ws.rs.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestfulEnclaveClientFactory implements EnclaveClientFactory<RestfulEnclaveClient> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RestfulEnclaveClientFactory.class);
    
    @Override
    public RestfulEnclaveClient create(Config config) {
        Optional<ServerConfig> enclaveServerConfig = config.getServerConfigs().stream()
                .filter(sc -> sc.getApp() == AppType.ENCLAVE)
                .filter(sc -> sc.getCommunicationType() == CommunicationType.REST)
                .findAny();

        final ClientFactory clientFactory = new ClientFactory();

        ServerConfig serverConfig = enclaveServerConfig.get();

        Client client = clientFactory.buildFrom(serverConfig);
        LOGGER.info("Creating remoted enclave for {}", serverConfig.getServerUri());
        return new RestfulEnclaveClient(client, serverConfig.getServerUri());
    }
    
    
    
}
