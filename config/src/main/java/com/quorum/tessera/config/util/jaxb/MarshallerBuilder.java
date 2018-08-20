package com.quorum.tessera.config.util.jaxb;

import com.quorum.tessera.jaxb.JaxbCallback;
import com.quorum.tessera.config.util.JaxbUtil;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

public class MarshallerBuilder {

    private MarshallerBuilder() {
    }

    public static MarshallerBuilder create() {
        return new MarshallerBuilder();
    }

    private boolean beanvalidation = true;

    private MediaType mediaType = MediaType.JSON;

    public MarshallerBuilder withoutBeanValidation() {
        this.beanvalidation = false;
        return this;
    }

    public MarshallerBuilder withXmlMediaType() {
        this.mediaType = MediaType.XML;
        return this;
    }

    public Marshaller build() {
        
       return  JaxbCallback.execute(() -> {
           
            JAXBContext jAXBContext = JAXBContext.newInstance(JaxbUtil.JAXB_CLASSES);

            Marshaller marshaller = jAXBContext.createMarshaller();
            if (!beanvalidation) {
                Enum enu = Enum.valueOf(Class.class.cast(marshaller
                        .getProperty("eclipselink.beanvalidation.mode")
                        .getClass()), "NONE");

                marshaller.setProperty("eclipselink.beanvalidation.mode", enu);
            }
            marshaller.setProperty("eclipselink.media-type", mediaType.getValue());
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
            if (mediaType == MediaType.JSON) {
                marshaller.setProperty("eclipselink.json.include-root", false);
            }
            return marshaller;
        });

    }

}
