package com.github.nexus.config;

import com.github.nexus.config.adapters.PathAdapter;
import com.github.nexus.config.constraints.KeyGen;
import com.github.nexus.config.constraints.ValidPath;
import java.nio.file.Path;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class PrivateKey {

    @XmlElement(name = "bytes")
    private final String value;

    private final String password;

    private final PrivateKeyType type;

    private final String snonce;

    private final String asalt;

    private final String sbox;

    private final ArgonOptions argonOptions;

    @NotNull(groups = KeyGen.class)
    @ValidPath(groups = KeyGen.class)
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path path;
    
    private PrivateKey() {
        this(null, null, null, null, null, null, null,null);
    }
    
    
    private static PrivateKey create() {
        return new PrivateKey();
    }
    
    public PrivateKey(final String value,
                      final String password,
                      final PrivateKeyType type,
                      final String snonce,
                      final String asalt,
                      final String sbox,
                      final ArgonOptions argonOptions,
                      final Path path) {
        this.value = value;
        this.password = password;
        this.type = type;
        this.snonce = snonce;
        this.asalt = asalt;
        this.sbox = sbox;
        this.argonOptions = argonOptions;
        this.path = path;
    }

    public String getValue() {
        return this.value;
    }

    public String getPassword() {
        return password;
    }

    public PrivateKeyType getType() {
        return type;
    }

    public String getSnonce() {
        return snonce;
    }

    public String getAsalt() {
        return asalt;
    }

    public String getSbox() {
        return sbox;
    }

    public ArgonOptions getArgonOptions() {
        return argonOptions;
    }

    public Path getPath() {
        return path;
    }

}
