package com.github.nexus.config.jaxb;

import com.github.nexus.config.SslTrustMode;
import java.nio.file.Path;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SslConfig", propOrder = {
    "tls",
    "serverKeyStore",
    "serverKeyStorePassword",
    "serverTrustStore",
    "serverTrustStorePassword",
    "serverTrustMode",
    "clientKeyStore",
    "clientKeyStorePassword",
    "clientTrustStore",
    "clientTrustStorePassword",
    "clientTrustMode",
    "knownClientsFile",
    "knownServersFile"
})
public class SslConfig implements com.github.nexus.config.SslConfig {

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(SslAuthenticationModeAdapter.class)
    private com.github.nexus.config.SslAuthenticationMode tls;

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path serverKeyStore;

    @XmlElement(required = true)
    private String serverKeyStorePassword;

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path serverTrustStore;

    @XmlElement(required = true)
    private String serverTrustStorePassword;

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(SslTrustModeAdapter.class)
    private com.github.nexus.config.SslTrustMode serverTrustMode;

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path clientKeyStore;

    @XmlElement(required = true)
    private String clientKeyStorePassword;

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path clientTrustStore;

    @XmlElement(required = true)
    private String clientTrustStorePassword;

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(SslTrustModeAdapter.class)
    private com.github.nexus.config.SslTrustMode clientTrustMode;

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path knownClientsFile;

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path knownServersFile;

    @Override
    public com.github.nexus.config.SslAuthenticationMode getTls() {
        return tls;
    }

    public void setTls(com.github.nexus.config.SslAuthenticationMode value) {
        this.tls = value;
    }

    @Override
    public Path getServerKeyStore() {
        return serverKeyStore;
    }

    public void setServerKeyStore(Path value) {
        this.serverKeyStore = value;
    }

    @Override
    public String getServerKeyStorePassword() {
        return serverKeyStorePassword;
    }

    public void setServerKeyStorePassword(String value) {
        this.serverKeyStorePassword = value;
    }

    @Override
    public Path getServerTrustStore() {
        return serverTrustStore;
    }

    public void setServerTrustStore(Path value) {
        this.serverTrustStore = value;
    }

    @Override
    public String getServerTrustStorePassword() {
        return serverTrustStorePassword;
    }

    public void setServerTrustStorePassword(String value) {
        this.serverTrustStorePassword = value;
    }

    @Override
    public com.github.nexus.config.SslTrustMode getServerTrustMode() {
        return serverTrustMode;
    }

    /**
     * Sets the value of the serverTrustMode property.
     *
     * @param value allowed object is {@link SslTrustMode }
     *
     */
    public void setServerTrustMode(com.github.nexus.config.SslTrustMode value) {
        this.serverTrustMode = value;
    }

    @Override
    public Path getClientKeyStore() {
        return clientKeyStore;
    }

    public void setClientKeyStore(Path value) {
        this.clientKeyStore = value;
    }

    @Override
    public String getClientKeyStorePassword() {
        return clientKeyStorePassword;
    }

    public void setClientKeyStorePassword(String value) {
        this.clientKeyStorePassword = value;
    }

    @Override
    public Path getClientTrustStore() {
        return clientTrustStore;
    }

    public void setClientTrustStore(Path value) {
        this.clientTrustStore = value;
    }

    @Override
    public String getClientTrustStorePassword() {
        return clientTrustStorePassword;
    }

    public void setClientTrustStorePassword(String value) {
        this.clientTrustStorePassword = value;
    }

    @Override
    public com.github.nexus.config.SslTrustMode getClientTrustMode() {
        return clientTrustMode;
    }


    public void setClientTrustMode(com.github.nexus.config.SslTrustMode value) {
        this.clientTrustMode = value;
    }

    @Override
    public Path getKnownClientsFile() {
        return knownClientsFile;
    }

    public void setKnownClientsFile(Path knownClientsFile) {
        this.knownClientsFile = knownClientsFile;
    }

    @Override
    public Path getKnownServersFile() {
        return knownServersFile;
    }

    public void setKnownServersFile(Path knownServersFile) {
        this.knownServersFile = knownServersFile;
    }



}
