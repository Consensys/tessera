
package com.github.nexus.config.sample;

import com.github.nexus.config.api.Configuration;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXB;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;


public class DoStuff {
    
    public static void main(String[] args) throws Exception {
        
       SchemaFactory schemaFactory =  SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        
       Schema schema = schemaFactory.newSchema(DoStuff.class.getResource("/xsd/config.xsd"));
       try (InputStream inputStream = DoStuff.class.getResourceAsStream("/sample.xml")) {
         schema.newValidator().validate(new StreamSource(inputStream), new StreamResult(System.out));
       }
       
       
       Configuration config = JAXB.unmarshal(DoStuff.class.getResourceAsStream("/sample.xml"), Configuration.class);
       
       System.out.println(config.getPrivateKeys().get(0).getType());
       
        
                 
        
//        ConfigFactory configFactory = ConfigFactory.create();
//        
//        Properties properties = new Properties();
//        properties.setProperty("nexus.jdbc.url", "");
//        properties.setProperty("nexus.jdbc.username", "");
//        properties.setProperty("nexus.jdbc.password", "");
//        
//        properties.setProperty("nexus.server.port", "");
//        
//        properties.setProperty("nexus.peers", "");
//        
//        
//        
//        Config config = configFactory.create(properties);
//        
//        
        
        System.exit(0);
        
        
    }
}
