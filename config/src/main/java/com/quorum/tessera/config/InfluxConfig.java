package com.quorum.tessera.config;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class InfluxConfig extends ConfigItem {

    @NotNull
    @XmlElement(required = true)
    private String hostName;

    @NotNull
    @XmlElement(required = true)
    private Integer port;

    @NotNull
    @XmlElement(required = true)
    private Long pushIntervalInSecs;

    @NotNull
    @XmlElement(required = true)
    private String dbName;

    public InfluxConfig(String hostName, Integer port, Long pushIntervalInSecs, String dbName) {
        this.hostName = hostName;
        this.port = port;
        this.dbName = dbName;
        this.pushIntervalInSecs = pushIntervalInSecs;
    }

    public InfluxConfig() {
        this(null, null, null, null);
    }

    public String getHostName() {
        return hostName;
    }

    public Integer getPort() {
        return port;
    }

    public Long getPushIntervalInSecs() {
        return pushIntervalInSecs;
    }

    public String getDbName() {
        return dbName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setPushIntervalInSecs(Long pushIntervalInSecs) {
        this.pushIntervalInSecs = pushIntervalInSecs;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

}
