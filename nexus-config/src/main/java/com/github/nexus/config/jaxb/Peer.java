package com.github.nexus.config.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Peer", propOrder = {
    "url"
})
public class Peer
    implements com.github.nexus.config.Peer
{

    @XmlElement(required = true)
    private String url;

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String value) {
        this.url = value;
    }

}
