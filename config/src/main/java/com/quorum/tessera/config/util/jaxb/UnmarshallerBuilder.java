package com.quorum.tessera.config.util.jaxb;

import com.quorum.tessera.config.util.JaxbUtil;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

public class UnmarshallerBuilder {

  public static UnmarshallerBuilder create() {
    return new UnmarshallerBuilder();
  }

  private boolean beanvalidation = true;

  private MediaType mediaType = MediaType.JSON;

  public UnmarshallerBuilder withoutBeanValidation() {
    this.beanvalidation = false;
    return this;
  }

  public UnmarshallerBuilder withXmlMediaType() {
    this.mediaType = MediaType.XML;
    return this;
  }

  public Unmarshaller build() {

    return JaxbCallback.execute(
        () -> {
          JAXBContext jAXBContext = JAXBContext.newInstance(JaxbUtil.JAXB_CLASSES);

          Unmarshaller unmarshaller = jAXBContext.createUnmarshaller();
          if (!beanvalidation) {
            Enum enu =
                Enum.valueOf(
                    Class.class.cast(
                        unmarshaller.getProperty("eclipselink.beanvalidation.mode").getClass()),
                    "NONE");

            unmarshaller.setProperty("eclipselink.beanvalidation.mode", enu);
          }
          unmarshaller.setProperty("eclipselink.media-type", mediaType.getValue());

          if (mediaType == MediaType.JSON) {
            unmarshaller.setProperty("eclipselink.json.include-root", false);
          }
          return unmarshaller;
        });
  }
}
