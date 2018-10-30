package com.quorum.tessera.config;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;
import java.net.URISyntaxException;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="UNIX")
public class UnixServerSocket extends ServerSocket {

    @NotNull
    @XmlElement(required = true)
    private String path;

    public UnixServerSocket(@NotNull String path) {
        this.path = path;
    }

    public UnixServerSocket(){
        this(null);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public URI getServerUri() {
        try {
            return new URI(path);
        } catch (URISyntaxException ex) {
            throw new ConfigException(ex);
        }
    }
}
