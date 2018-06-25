package com.github.nexus.config.jaxb;

import java.nio.file.Path;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.github.nexus.config.PrivateKeyType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PrivateKey", propOrder = {
    "path",
    "value",
    "password",
    "snonce", 
    "asalt", 
    "sbox",
    "argonOptions"
})
public class PrivateKey
        implements com.github.nexus.config.PrivateKey {

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path path;

    @XmlSchemaType(name = "anyURI")
    private String value;

    @XmlElement(required = true)
    private String password;

    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(PrivateKeyTypeAdapter.class)
    private PrivateKeyType type;

    @XmlElement
    private String snonce;

    @XmlElement
    private String asalt;

    @XmlElement
    private String sbox;

    @XmlElement
    private ArgonOptions argonOptions;

    @Override
    public Path getPath() {
        return path;
    }

    public void setPath(Path value) {
        this.path = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String value) {
        this.password = value;
    }

    @Override
    public PrivateKeyType getType() {
        return type;
    }

    public void setType(PrivateKeyType value) {
        this.type = value;
    }

    @Override
    public String getSnonce() {
        return snonce;
    }

    public void setSnonce(String snonce) {
        this.snonce = snonce;
    }

    @Override
    public String getAsalt() {
        return asalt;
    }

    public void setAsalt(String asalt) {
        this.asalt = asalt;
    }

    @Override
    public String getSbox() {
        return sbox;
    }

    public void setSbox(String sbox) {
        this.sbox = sbox;
    }

    @Override
    public ArgonOptions getArgonOptions() {
        return argonOptions;
    }

    public void setArgonOptions(ArgonOptions argonOptions) {
        this.argonOptions = argonOptions;
    }

}
