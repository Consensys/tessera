package com.github.nexus.config;

import com.github.nexus.config.adapters.PathAdapter;
import com.github.nexus.config.constraints.KeyGen;
import com.github.nexus.config.constraints.ValidPath;
import com.github.nexus.config.util.PathUtil;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;
import javax.validation.constraints.NotNull;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class PublicKey {
    
    @NotNull(groups = KeyGen.class)
    @ValidPath(groups = KeyGen.class)
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path path;

    @XmlSchemaType(name = "anyURI")
    private String value;

    public PublicKey(Path path, String value) {
        this.path = path;
        this.value = value;
    }

    private PublicKey() {
        this(null, null);
    }

    private static PublicKey create() {
        return new PublicKey(null, null);
    }

    public Path getPath() {
        return path;
    }

    public String getValue() {
        if(value == null) {
            return PathUtil.readData(path, null);
        }

        return value;
    }

}
