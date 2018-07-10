package com.github.tessera.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ServiceLoader;

public interface UnixSocketFactory {

     ServerSocket createServerSocket(Path socketFile) throws IOException;

     Socket createSocket(Path socketFile) throws IOException;

     static UnixSocketFactory create() {
         return ServiceLoader.load(UnixSocketFactory.class).iterator().next();
     }
     
}
