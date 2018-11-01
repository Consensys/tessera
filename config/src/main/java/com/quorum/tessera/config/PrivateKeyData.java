package com.quorum.tessera.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class PrivateKeyData extends ConfigItem {

    @XmlElement(name = "bytes")
    private String value;

    @XmlElement
    private String snonce;

    @XmlElement
    private String asalt;

    @XmlElement
    private String sbox;

    @XmlElement(name = "aopts")
    private ArgonOptions argonOptions;

    @XmlElement
    private String password;

    public PrivateKeyData(String value, String snonce, String asalt, String sbox, ArgonOptions argonOptions, String password) {
        this.value = value;
        this.snonce = snonce;
        this.asalt = asalt;
        this.sbox = sbox;
        this.argonOptions = argonOptions;
        this.password = password;
    }

    public PrivateKeyData() {
    }



    public String getValue() {
        return value;
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

    public String getPassword() {
        return password;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setSnonce(String snonce) {
        this.snonce = snonce;
    }

    public void setAsalt(String asalt) {
        this.asalt = asalt;
    }

    public void setSbox(String sbox) {
        this.sbox = sbox;
    }

    public void setArgonOptions(ArgonOptions argonOptions) {
        this.argonOptions = argonOptions;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    
}
