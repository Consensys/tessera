
package com.github.nexus.config.jaxb;


import com.github.nexus.config.PrivateKeyType;
import javax.xml.bind.annotation.adapters.XmlAdapter;


public class PrivateKeyTypeAdapter extends XmlAdapter<String,PrivateKeyType>{

    @Override
    public PrivateKeyType unmarshal(String v) throws Exception {
        return PrivateKeyType.valueOf(v.toUpperCase());
    }

    @Override
    public String marshal(PrivateKeyType v) throws Exception {
        return v.name().toLowerCase();
    }
 
}
