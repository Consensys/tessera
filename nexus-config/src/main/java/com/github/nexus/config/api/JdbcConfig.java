package com.github.nexus.config.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "JdbcConfig", propOrder = {
    "username",
    "password",
    "url"
})
public class JdbcConfig
    implements com.github.nexus.config.JdbcConfig
{

    @XmlElement(required = true)
    private String username;
    
    @XmlElement(required = true)
    private String password;
    
    @XmlElement(required = true)
    private String url;

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String value) {
        this.username = value;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String value) {
        this.url = value;
    }

}
