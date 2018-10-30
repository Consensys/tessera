package com.quorum.tessera.config.apps;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerSocket;

import java.util.Set;

public interface TesseraApp {
    Set<CommunicationType> communicationTypes();
    Set<Class<? extends ServerSocket>> serverSocketTypes();
}
