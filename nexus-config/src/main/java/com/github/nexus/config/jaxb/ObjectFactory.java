package com.github.nexus.config.jaxb;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {

    private static final QName QNAME = new QName("http://nexus.github.com/config", "configuration");

    public ObjectFactory() {
    }

    public Configuration createConfiguration() {
        
      
        return new Configuration();
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

    @XmlElementDecl(namespace = "http://nexus.github.com/config", name = "configuration")
    public JAXBElement<Configuration> createConfiguration(Configuration value) {
        return new JAXBElement<>(QNAME, Configuration.class, null, value);
    }

}
