package com.quorum.tessera.config.util;

import com.quorum.tessera.config.*;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Optional;
import javax.validation.ConstraintViolationException;

public interface JaxbUtil {

    Class[] JAXB_CLASSES = new Class[]{
        Config.class,
        KeyDataConfig.class,
        PrivateKeyData.class,
        ArgonOptions.class,
        JdbcConfig.class,
        KeyData.class,
        Peer.class,
        PrivateKeyType.class,
        ServerConfig.class,
        SslAuthenticationMode.class,
        SslConfig.class,
        SslTrustMode.class
    };

    static <T> T unmarshal(InputStream inputStream, Class<T> type) {

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(JAXB_CLASSES);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setProperty("eclipselink.media-type", "application/json");
            unmarshaller.setProperty("eclipselink.json.include-root", false);

            return unmarshaller.unmarshal(new StreamSource(inputStream), type).getValue();
        } catch (JAXBException ex) {
            throw new ConfigException(ex);
        }
    }

    static void marshal(Object object, OutputStream outputStream) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(JAXB_CLASSES);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty("eclipselink.media-type", "application/json");
            marshaller.setProperty("eclipselink.json.include-root", false);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            marshaller.marshal(object, outputStream);
        }  catch(Throwable ex) {
          Optional<ConstraintViolationException> validationException =   unwrapConstraintViolationException(ex);
          if(validationException.isPresent()) {
              throw validationException.get();
          }
          throw new ConfigException(ex);
        }

    }

    static void marshalWithNoValidation(Object object, OutputStream outputStream) {
        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(JAXB_CLASSES);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty("eclipselink.media-type", "application/json");
            //Workaround so not to require eclipselink compile time dependency.
            //Resolve BeanValidationMode from default value and set to NONE
            //org.eclipse.persistence.jaxb.BeanValidationMode
            Enum enu = Enum.valueOf(Class.class.cast(marshaller
                    .getProperty("eclipselink.beanvalidation.mode")
                    .getClass()), "NONE");

            marshaller.setProperty("eclipselink.beanvalidation.mode", enu);
            marshaller.setProperty("eclipselink.json.include-root", false);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(object, outputStream);
        } catch (JAXBException ex) {
            throw new ConfigException(ex);
        }
    }

    static String marshalToString(Object object) {
        return IOCallback.execute(() -> {
            try (OutputStream out = new ByteArrayOutputStream()) {
                marshal(object, out);
                return out.toString();
            }
        });
    }

    static String marshalToStringNoValidation(Object object) {
        return IOCallback.execute(() -> {
            try (OutputStream out = new ByteArrayOutputStream()) {
                marshalWithNoValidation(object, out);
                return out.toString();
            }
        });
    }
    
    static Optional<ConstraintViolationException> unwrapConstraintViolationException(Throwable ex) {
        return Optional.of(ex)
                .map(Throwable::getCause)
                .filter(Objects::nonNull)
                .filter(ConstraintViolationException.class::isInstance)
                .map(ConstraintViolationException.class::cast);
    }
    
}
