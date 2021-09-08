package com.quorum.tessera.config;

import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class JdbcConfig extends ConfigItem {

  @XmlElement private String username;

  @XmlElement private String password;

  @NotNull
  @XmlElement(required = true)
  private String url;

  /** Auto create tables if no exists */
  @XmlElement(defaultValue = "false")
  private boolean autoCreateTables;

  @XmlElement private int fetchSize;

  public JdbcConfig(String username, String password, String url) {
    this.username = username;
    this.password = password;
    this.url = url;
  }

  public JdbcConfig() {
    this(null, null, null);
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getUrl() {
    return url;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public boolean isAutoCreateTables() {
    return autoCreateTables;
  }

  public void setAutoCreateTables(boolean autoCreateTables) {
    this.autoCreateTables = autoCreateTables;
  }

  public int getFetchSize() {
    return fetchSize;
  }

  public void setFetchSize(int fetchSize) {
    this.fetchSize = fetchSize;
  }
}
