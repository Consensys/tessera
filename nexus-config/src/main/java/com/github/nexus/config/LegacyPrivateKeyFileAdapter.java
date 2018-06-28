
package com.github.nexus.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.bind.annotation.adapters.XmlAdapter;


public class LegacyPrivateKeyFileAdapter extends XmlAdapter<String, LegacyPrivateKeyFile> {

    @Override
    public LegacyPrivateKeyFile unmarshal(String v) throws Exception {
        
        Path path = Paths.get(v);
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String marshal(LegacyPrivateKeyFile v) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
