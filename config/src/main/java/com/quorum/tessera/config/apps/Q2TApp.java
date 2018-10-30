package com.quorum.tessera.config.apps;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.InetServerSocket;
import com.quorum.tessera.config.ServerSocket;
import com.quorum.tessera.config.UnixServerSocket;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface Q2TApp extends TesseraApp {
    @Override
    default Set<CommunicationType> communicationTypes(){
        return new HashSet<CommunicationType>(Arrays.asList(CommunicationType.REST));
    }

    @Override
    default Set<Class<? extends ServerSocket>> serverSocketTypes(){
        return new HashSet<>(Arrays.asList(InetServerSocket.class, UnixServerSocket.class));
    }
}
