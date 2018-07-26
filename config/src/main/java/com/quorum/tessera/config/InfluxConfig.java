package com.quorum.tessera.config;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class InfluxConfig extends ConfigItem {

    @NotNull
    @XmlElement(required = true)
    private final String hostName;

    @NotNull
    @XmlElement(required = true)
    private final Integer port;

    @NotNull
    @XmlElement(required = true)
    private final Long pushIntervalInSecs;

    @NotNull
    @XmlElement(required = true)
    private final String dbName;

    public InfluxConfig(String hostName, Integer port, Long pushIntervalInSecs, String dbName) {
        this.hostName = hostName;
        this.port = port;
        this.dbName = dbName;
        this.pushIntervalInSecs = pushIntervalInSecs;
    }

    private static InfluxConfig create() {
        return new InfluxConfig(null, null, null, null);
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
}
