package com.github.nexus.config;

import java.net.URL;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class SampleTest {

    @Test
    public void loadSampleUnmarshalToXmlUusingJaxbUtil() throws Exception {

        URL sampleXml = getClass().getResource("/samples/sample.xml");

        Configuration result = JAXB.unmarshal(sampleXml, Configuration.class);

        assertThat(result).isNotNull();
        assertThat(result.getKeygenBasePath()).isEqualTo("SOMEPATH");

    }

    @Test
    public void loadSampleUnmarshalToJsonUsingMoxy() throws Exception {

        URL sampleJson = getClass().getResource("/samples/sample.json");

        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setProperty("eclipselink.media-type", "application/json");
        unmarshaller.setProperty("eclipselink.json.include-root", false);
        
        
        JAXBElement<Configuration> element = unmarshaller.unmarshal(new StreamSource(sampleJson.openStream()),Configuration.class);
        Configuration result = element.getValue();
        assertThat(result).isNotNull();
        assertThat(result.getKeygenBasePath()).isEqualTo("SOMEPATH");
    }

}
