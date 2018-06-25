package com.github.nexus.config;

import java.nio.file.Path;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class PrivateKey {

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path path;

    @XmlSchemaType(name = "anyURI")
    private final String value;

    @XmlElement(required = true)
    private final String password;

    @XmlAttribute
    private final PrivateKeyType type;

    @XmlElement
    private final String snonce;

    @XmlElement
    private final String asalt;

    @XmlElement
    private final String sbox;

    @XmlElement
    private final ArgonOptions argonOptions;

    public PrivateKey(
            Path path, 
            String value, 
            String password, 
            PrivateKeyType type, 
            String snonce, 
            String asalt, 
            String sbox, 
            ArgonOptions argonOptions) {
        this.path = path;
        this.value = value;
        this.password = password;
        this.type = type;
        this.snonce = snonce;
        this.asalt = asalt;
        this.sbox = sbox;
        this.argonOptions = argonOptions;
    }

    private PrivateKey() {
        this(null,null,null,null,null,null,null,null);
    }

    private static PrivateKey create() {
        return new PrivateKey();
    }
    
    
    public Path getPath() {
        return path;
    }

    public String getValue() {
        return value;
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

}
