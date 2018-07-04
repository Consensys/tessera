package com.github.nexus.config.util;

import com.github.nexus.config.ArgonOptions;
import com.github.nexus.config.Config;
import com.github.nexus.config.ConfigException;
import com.github.nexus.config.JdbcConfig;
import com.github.nexus.config.KeyData;
import com.github.nexus.config.KeyDataConfig;
import com.github.nexus.config.Peer;
import com.github.nexus.config.PrivateKeyData;
import com.github.nexus.config.PrivateKeyType;
import com.github.nexus.config.ServerConfig;
import com.github.nexus.config.SslAuthenticationMode;
import com.github.nexus.config.SslConfig;
import com.github.nexus.config.SslTrustMode;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

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
}
