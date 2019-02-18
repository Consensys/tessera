package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.TesseraServerFactory;
import java.util.Collections;


public class Main {
    
    public static void main(String... args) throws Exception {
        
        TesseraServerFactory serverFactory = TesseraServerFactory.create(CommunicationType.WEB_SOCKET);
        
        ServerConfig config = null;
        
        
        serverFactory.createServer(config, Collections.singleton(EnclaveEndpoint.class));
        
    }
}
