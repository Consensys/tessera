
package com.github.nexus.config.jaxb;

import com.github.nexus.config.SslAuthenticationMode;
import javax.xml.bind.annotation.adapters.XmlAdapter;


public class SslAuthenticationModeAdapter extends XmlAdapter<String, SslAuthenticationMode>{

    @Override
    public SslAuthenticationMode unmarshal(String v) throws Exception {
        return SslAuthenticationMode.valueOf(v);
    }

    @Override
    public String marshal(SslAuthenticationMode v) throws Exception {
        return v.name();
    }
    
}
