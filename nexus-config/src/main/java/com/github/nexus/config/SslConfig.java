
package com.github.nexus.config;

import java.nio.file.Path;


public interface SslConfig {

    SslAuthenticationMode getTls();

    Path getServerKeyStore();

    String getServerKeyStorePassword();

    Path getServerTrustStore();

    String getServerTrustStorePassword();

    SslTrustMode getServerTrustMode();

    Path getKnownClientsFile();

    Path getClientKeyStore();

    String getClientKeyStorePassword();

    Path getClientTrustStore();

    String getClientTrustStorePassword();

    SslTrustMode getClientTrustMode();

    Path getKnownServersFile();
}
