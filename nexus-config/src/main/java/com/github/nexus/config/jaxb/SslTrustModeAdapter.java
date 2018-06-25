
package com.github.nexus.config.jaxb;

import com.github.nexus.config.SslTrustMode;
import javax.xml.bind.annotation.adapters.XmlAdapter;


public class SslTrustModeAdapter extends XmlAdapter<String, SslTrustMode>{

    @Override
    public SslTrustMode unmarshal(String v) throws Exception {
        return SslTrustMode.valueOf(v);
    }

    @Override
    public String marshal(SslTrustMode v) throws Exception {
        return v.name();
    }
    
}
