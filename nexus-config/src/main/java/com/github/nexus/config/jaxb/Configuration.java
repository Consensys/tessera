package com.github.nexus.config.jaxb;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.github.nexus.config.Config;

import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Configuration", propOrder = {
    "jdbcConfig",
    "serverConfig",
    "peers",
    "keys",
    "unixSocketFile",
    "useWhiteList"
})
public class Configuration implements Config {

    @XmlElement(name = "jdbc", required = true)
    private JdbcConfig jdbcConfig;

    @XmlElement(name = "server", required = true)
    private ServerConfig serverConfig;


    @XmlElement(name = "peer", required = true)
    private final List<Peer> peers = new ArrayList<>();

    
    @XmlElement(name="keys",required = true)
    private List<KeyData> keys = new ArrayList<>();

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path unixSocketFile;
    
    @XmlAttribute(name = "useWhiteList")
    private boolean useWhiteList;
    
    @Override
    public com.github.nexus.config.JdbcConfig getJdbcConfig() {
        return jdbcConfig;
    }

    @Override
    public com.github.nexus.config.ServerConfig getServerConfig() {
        return serverConfig;
    }

    @Override
    public Path getUnixSocketFile() {
        return unixSocketFile;
    }

    @Override
    public List<com.github.nexus.config.Peer> getPeers() {
        return  peers.stream()
                .map(com.github.nexus.config.Peer.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<com.github.nexus.config.KeyData> getKeys() {
        return keys.stream()
                .map(KeyData.class::cast)
                .collect(Collectors.toList());
    }

    public void setKeys(List<com.github.nexus.config.KeyData> keys) {
        this.keys = keys.stream()
                .map(KeyData.class::cast)
                .collect(Collectors.toList());
    }

    public boolean isUseWhiteList() {
        return useWhiteList;
    }

    public void setUseWhiteList(boolean useWhiteList) {
        this.useWhiteList = useWhiteList;
    }

   

}
