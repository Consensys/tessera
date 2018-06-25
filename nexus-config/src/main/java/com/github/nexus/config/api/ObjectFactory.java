package com.github.nexus.config.api;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {

    private static final QName QNAME = new QName("http://nexus.github.com/config/api", "configuration");

    public ObjectFactory() {
    }

    public Configuration createConfiguration() {
        
      
        return new Configuration();
    }

    public KeyGenConfig createKeyGenConfig() {
        return new KeyGenConfig();
    }

    public JdbcConfig createJdbcConfig() {
        return new JdbcConfig();
    }


    public ServerConfig createServerConfig() {
        return new ServerConfig();
    }

    /**
     * Create an instance of {@link Peer }
     * 
     * @return 
     */
    public Peer createPeer() {
        return new Peer();
    }


    public PrivateKey createPrivateKey() {
        return new PrivateKey();
    }


    public PublicKey createPublicKey() {
        return new PublicKey();
    }

    @XmlElementDecl(namespace = "http://nexus.github.com/config/api", name = "configuration")
    public JAXBElement<Configuration> createConfiguration(Configuration value) {
        return new JAXBElement<>(QNAME, Configuration.class, null, value);
    }

}
