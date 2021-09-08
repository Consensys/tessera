package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.CharArrayAdapter;
import com.quorum.tessera.config.adapters.PathAdapter;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class SslConfig extends ConfigItem {

  @NotNull
  @XmlElement(required = true)
  private SslAuthenticationMode tls;

  @XmlElement(defaultValue = "false")
  private boolean generateKeyStoreIfNotExisted;

  @XmlElement(type = String.class)
  @XmlJavaTypeAdapter(PathAdapter.class)
  private Path serverKeyStore;

  @XmlElement(type = String.class)
  @XmlJavaTypeAdapter(PathAdapter.class)
  private Path serverTlsKeyPath;

  @XmlElement(type = String.class)
  @XmlJavaTypeAdapter(PathAdapter.class)
  private Path serverTlsCertificatePath;

  @XmlElement(type = String.class)
  @XmlJavaTypeAdapter(CharArrayAdapter.class)
  private char[] serverKeyStorePassword;

  @XmlElement(type = String.class)
  @XmlJavaTypeAdapter(PathAdapter.class)
  private Path serverTrustStore;

  @XmlElement
  @XmlJavaTypeAdapter(value = PathAdapter.class)
  private List<Path> serverTrustCertificates;

  @XmlElement(type = String.class)
  @XmlJavaTypeAdapter(CharArrayAdapter.class)
  private char[] serverTrustStorePassword;

  @XmlElement private SslTrustMode serverTrustMode;

  @XmlElement(type = String.class)
  @XmlJavaTypeAdapter(PathAdapter.class)
  private Path clientKeyStore;

  @XmlElement(type = String.class)
  @XmlJavaTypeAdapter(PathAdapter.class)
  private Path clientTlsKeyPath;

  @XmlElement(type = String.class)
  @XmlJavaTypeAdapter(PathAdapter.class)
  private Path clientTlsCertificatePath;

  @XmlElement(type = String.class)
  @XmlJavaTypeAdapter(CharArrayAdapter.class)
  private char[] clientKeyStorePassword;

  @XmlElement(type = String.class)
  @XmlJavaTypeAdapter(PathAdapter.class)
  private Path clientTrustStore;

  @XmlElement
  @XmlJavaTypeAdapter(value = PathAdapter.class)
  private List<Path> clientTrustCertificates;

  @XmlElement(type = String.class)
  @XmlJavaTypeAdapter(CharArrayAdapter.class)
  private char[] clientTrustStorePassword;

  @XmlElement private SslTrustMode clientTrustMode;

  @XmlElement(type = String.class)
  @XmlJavaTypeAdapter(PathAdapter.class)
  private Path knownClientsFile;

  @XmlElement(type = String.class)
  @XmlJavaTypeAdapter(PathAdapter.class)
  private Path knownServersFile;

  @XmlElement private String environmentVariablePrefix;

  @XmlElement private SslConfigType sslConfigType;

  @XmlElement private String[] excludeCipherSuites;

  @XmlElement(defaultValue = "true")
  private boolean clientAuth = true;

  public SslConfig(
      SslAuthenticationMode tls,
      boolean generateKeyStoreIfNotExisted,
      Path serverKeyStore,
      char[] serverKeyStorePassword,
      Path serverTrustStore,
      char[] serverTrustStorePassword,
      SslTrustMode serverTrustMode,
      Path clientKeyStore,
      char[] clientKeyStorePassword,
      Path clientTrustStore,
      char[] clientTrustStorePassword,
      SslTrustMode clientTrustMode,
      Path knownClientsFile,
      Path knownServersFile,
      List<Path> serverTrustCertificates,
      List<Path> clientTrustCertificates,
      Path serverTlsKeyPath,
      Path serverTlsCertificatePath,
      Path clientTlsKeyPath,
      Path clientTlsCertificatePath,
      String environmentVariablePrefix) {

    this.tls = tls;
    this.generateKeyStoreIfNotExisted = generateKeyStoreIfNotExisted;
    this.serverKeyStore = serverKeyStore;
    this.serverTrustCertificates = serverTrustCertificates;
    this.serverKeyStorePassword = serverKeyStorePassword;
    this.serverTrustStore = serverTrustStore;
    this.serverTrustStorePassword = serverTrustStorePassword;
    this.serverTrustMode = serverTrustMode;
    this.clientKeyStore = clientKeyStore;
    this.clientTrustCertificates = clientTrustCertificates;
    this.clientKeyStorePassword = clientKeyStorePassword;
    this.clientTrustStore = clientTrustStore;
    this.clientTrustStorePassword = clientTrustStorePassword;
    this.clientTrustMode = clientTrustMode;
    this.knownClientsFile = knownClientsFile;
    this.knownServersFile = knownServersFile;
    this.serverTlsKeyPath = serverTlsKeyPath;
    this.serverTlsCertificatePath = serverTlsCertificatePath;
    this.clientTlsKeyPath = clientTlsKeyPath;
    this.clientTlsCertificatePath = clientTlsCertificatePath;
    this.environmentVariablePrefix = environmentVariablePrefix;
  }

  public SslConfig() {}

  public SslAuthenticationMode getTls() {
    return tls;
  }

  public boolean isGenerateKeyStoreIfNotExisted() {
    return generateKeyStoreIfNotExisted;
  }

  public Path getServerKeyStore() {
    return serverKeyStore;
  }

  public char[] getServerKeyStorePassword() {
    return serverKeyStorePassword;
  }

  public Path getServerTrustStore() {
    return serverTrustStore;
  }

  public char[] getServerTrustStorePassword() {
    return serverTrustStorePassword;
  }

  public SslTrustMode getServerTrustMode() {
    return serverTrustMode;
  }

  public Path getClientKeyStore() {
    return clientKeyStore;
  }

  public char[] getClientKeyStorePassword() {
    return clientKeyStorePassword;
  }

  public Path getClientTrustStore() {
    return clientTrustStore;
  }

  public char[] getClientTrustStorePassword() {
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

  public List<Path> getServerTrustCertificates() {
    return serverTrustCertificates;
  }

  public List<Path> getClientTrustCertificates() {
    return clientTrustCertificates;
  }

  public Path getServerTlsKeyPath() {
    return serverTlsKeyPath;
  }

  public Path getServerTlsCertificatePath() {
    return serverTlsCertificatePath;
  }

  public Path getClientTlsKeyPath() {
    return clientTlsKeyPath;
  }

  public Path getClientTlsCertificatePath() {
    return clientTlsCertificatePath;
  }

  public String getEnvironmentVariablePrefix() {
    return environmentVariablePrefix;
  }

  public void setTls(SslAuthenticationMode tls) {
    this.tls = tls;
  }

  public void setGenerateKeyStoreIfNotExisted(boolean generateKeyStoreIfNotExisted) {
    this.generateKeyStoreIfNotExisted = generateKeyStoreIfNotExisted;
  }

  public void setServerKeyStore(Path serverKeyStore) {
    this.serverKeyStore = serverKeyStore;
  }

  public void setServerTlsKeyPath(Path serverTlsKeyPath) {
    this.serverTlsKeyPath = serverTlsKeyPath;
  }

  public void setServerTlsCertificatePath(Path serverTlsCertificatePath) {
    this.serverTlsCertificatePath = serverTlsCertificatePath;
  }

  public void setServerKeyStorePassword(char[] serverKeyStorePassword) {
    this.serverKeyStorePassword = serverKeyStorePassword;
  }

  public void setServerTrustStore(Path serverTrustStore) {
    this.serverTrustStore = serverTrustStore;
  }

  public void setServerTrustCertificates(List<Path> serverTrustCertificates) {
    this.serverTrustCertificates = serverTrustCertificates;
  }

  public void setServerTrustStorePassword(char[] serverTrustStorePassword) {
    this.serverTrustStorePassword = serverTrustStorePassword;
  }

  public void setServerTrustMode(SslTrustMode serverTrustMode) {
    this.serverTrustMode = serverTrustMode;
  }

  public void setClientKeyStore(Path clientKeyStore) {
    this.clientKeyStore = clientKeyStore;
  }

  public void setClientTlsKeyPath(Path clientTlsKeyPath) {
    this.clientTlsKeyPath = clientTlsKeyPath;
  }

  public void setClientTlsCertificatePath(Path clientTlsCertificatePath) {
    this.clientTlsCertificatePath = clientTlsCertificatePath;
  }

  public void setClientKeyStorePassword(char[] clientKeyStorePassword) {
    this.clientKeyStorePassword = clientKeyStorePassword;
  }

  public void setClientTrustStore(Path clientTrustStore) {
    this.clientTrustStore = clientTrustStore;
  }

  public void setClientTrustCertificates(List<Path> clientTrustCertificates) {
    this.clientTrustCertificates = clientTrustCertificates;
  }

  public void setClientTrustStorePassword(char[] clientTrustStorePassword) {
    this.clientTrustStorePassword = clientTrustStorePassword;
  }

  public void setClientTrustMode(SslTrustMode clientTrustMode) {
    this.clientTrustMode = clientTrustMode;
  }

  public void setKnownClientsFile(Path knownClientsFile) {
    this.knownClientsFile = knownClientsFile;
  }

  public void setKnownServersFile(Path knownServersFile) {
    this.knownServersFile = knownServersFile;
  }

  public void setEnvironmentVariablePrefix(String environmentVariablePrefix) {
    this.environmentVariablePrefix = environmentVariablePrefix;
  }

  public SslConfigType getSslConfigType() {
    return sslConfigType;
  }

  public void setSslConfigType(SslConfigType sslConfigType) {
    this.sslConfigType = sslConfigType;
  }

  public String[] getExcludeCipherSuites() {
    return excludeCipherSuites;
  }

  public void setExcludeCipherSuites(String[] excludeCipherSuites) {
    this.excludeCipherSuites = excludeCipherSuites;
  }

  public boolean isClientAuth() {
    return clientAuth;
  }

  public void setClientAuth(boolean clientAuth) {
    this.clientAuth = clientAuth;
  }
}
