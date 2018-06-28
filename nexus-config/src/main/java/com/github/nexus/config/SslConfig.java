package com.github.nexus.config;

import com.github.nexus.config.constraints.ValidPath;
import java.nio.file.Path;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class SslConfig  {

    @NotNull
    @XmlElement(required = true)
    private final SslAuthenticationMode tls;

    @NotNull
    @ValidPath(checkExists = true)
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path serverKeyStore;

    @XmlElement(required = true)
    private final String serverKeyStorePassword;

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path serverTrustStore;

    @XmlElement(required = true)
    private final String serverTrustStorePassword;

    @XmlElement(required = true)
    private final SslTrustMode serverTrustMode;

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path clientKeyStore;

    @XmlElement(required = true)
    private final String clientKeyStorePassword;

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path clientTrustStore;

    @XmlElement(required = true)
    private final String clientTrustStorePassword;

    @XmlElement(required = true)
    private final SslTrustMode clientTrustMode;

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path knownClientsFile;

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path knownServersFile;

    public SslConfig(
            SslAuthenticationMode tls, 
            Path serverKeyStore, 
            String serverKeyStorePassword, 
            Path serverTrustStore, 
            String serverTrustStorePassword, 
            SslTrustMode serverTrustMode, 
            Path clientKeyStore, 
            String clientKeyStorePassword, 
            Path clientTrustStore, 
            String clientTrustStorePassword, 
            SslTrustMode clientTrustMode, 
            Path knownClientsFile, 
            Path knownServersFile) {
        this.tls = tls;
        this.serverKeyStore = serverKeyStore;
        this.serverKeyStorePassword = serverKeyStorePassword;
        this.serverTrustStore = serverTrustStore;
        this.serverTrustStorePassword = serverTrustStorePassword;
        this.serverTrustMode = serverTrustMode;
        this.clientKeyStore = clientKeyStore;
        this.clientKeyStorePassword = clientKeyStorePassword;
        this.clientTrustStore = clientTrustStore;
        this.clientTrustStorePassword = clientTrustStorePassword;
        this.clientTrustMode = clientTrustMode;
        this.knownClientsFile = knownClientsFile;
        this.knownServersFile = knownServersFile;
    }

    private SslConfig() {
        this(null,null,null,null,null,null,null,null,null,null,null,null,null);
    }
    
    private static SslConfig create() {
        return new SslConfig();
    }

    public SslAuthenticationMode getTls() {
        return tls;
    }

    public Path getServerKeyStore() {
        return serverKeyStore;
    }

    public String getServerKeyStorePassword() {
        return serverKeyStorePassword;
    }

    public Path getServerTrustStore() {
        return serverTrustStore;
    }

    public String getServerTrustStorePassword() {
        return serverTrustStorePassword;
    }

    public SslTrustMode getServerTrustMode() {
        return serverTrustMode;
    }

    public Path getClientKeyStore() {
        return clientKeyStore;
    }

    public String getClientKeyStorePassword() {
        return clientKeyStorePassword;
    }

    public Path getClientTrustStore() {
        return clientTrustStore;
    }

    public String getClientTrustStorePassword() {
        return clientTrustStorePassword;
    }

    public SslTrustMode getClientTrustMode() {
        return clientTrustMode;
    }

    public Path getKnownClientsFile() {
        return knownClientsFile;
    }

    public Path getKnownServersFile() {
        return knownServersFile;
    }

}
