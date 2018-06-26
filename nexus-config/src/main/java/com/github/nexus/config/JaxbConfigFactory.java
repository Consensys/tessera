package com.github.nexus.config;

import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

public class JaxbConfigFactory implements ConfigFactory {


    @Override
    public Config create(InputStream inputStream) {

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setProperty("eclipselink.media-type", "application/json");
            unmarshaller.setProperty("eclipselink.json.include-root", false);
            return unmarshaller.unmarshal(new StreamSource(inputStream),Config.class).getValue();
        } catch (JAXBException ex) {
            throw new ConfigException(ex);
        }
    }

}
