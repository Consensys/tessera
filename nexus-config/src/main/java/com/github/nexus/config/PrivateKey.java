package com.github.nexus.config;

import com.github.nexus.config.adapters.PathAdapter;
import java.nio.file.Path;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class PrivateKey {

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path path;

    @XmlElement
    private final String value;

    @XmlElement
    private final PrivateKeyConfig config;

    public PrivateKey(Path path, String value, PrivateKeyConfig config) {
        this.path = path;
        this.value = value;
        this.config = config;
    }

    private static PrivateKey create() {
        return new PrivateKey(null, null, null);
    }

    public Path getPath() {
        return path;
    }

    public String getValue() {
        return value;
    }

    public PrivateKeyConfig getConfig() {
        return config;
    }

}
