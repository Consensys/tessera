package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.enclave.EnclaveClientFactory;


public class WebsocketEnclaveClientFactory implements EnclaveClientFactory<WebsocketEnclaveClient> {

    @Override
    public WebsocketEnclaveClient create(Config config) {
        return config.getServerConfigs().stream()
                .filter(sc -> sc.getApp() == AppType.ENCLAVE)
                .filter(sc -> sc.getCommunicationType() == CommunicationType.WEB_SOCKET)
                .map(ServerConfig::getServerUri)
                .map(UriPathAppender::createFromServerUri)
                .map(WebsocketEnclaveClient::new)
                .findAny().get();
    }
    
}
