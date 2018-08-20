package com.quorum.tessera.config;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class JdbcConfig extends ConfigItem {

    @XmlElement
    private final String username;

    @XmlElement
    private final String password;

    @NotNull
    @XmlElement(required = true)
    private final String url;

    public JdbcConfig(String username, String password, String url) {
        this.username = username;
        this.password = password;
        this.url = url;
    }

    private static JdbcConfig create() {
        return new JdbcConfig();
    }

    private JdbcConfig() {
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
  
}
