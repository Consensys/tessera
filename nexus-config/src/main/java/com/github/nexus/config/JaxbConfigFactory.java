package com.github.nexus.config;

import com.github.nexus.config.api.Configuration;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;

public class JaxbConfigFactory implements ConfigFactory {

    private final Schema schema;

    public JaxbConfigFactory() throws SAXException {
        schema
                = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                        .newSchema(JaxbConfigFactory.class.getResource("/xsd/config.xsd"));

    }

    @Override
    public Config create(InputStream inputStream) {

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(schema);
            JAXBElement<Configuration> element = unmarshaller.unmarshal(new StreamSource(inputStream), Configuration.class);

            return element.getValue();
        } catch (JAXBException ex) {
            throw new ConfigException(ex);
        }
    }

}
