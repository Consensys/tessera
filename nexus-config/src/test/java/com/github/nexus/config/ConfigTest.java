package com.github.nexus.config;

import com.github.nexus.config.api.Configuration;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXB;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class ConfigTest {

    public ConfigTest() {
    }

    @Test
    public void validateXmlConfig() throws Exception {
        URL url = Objects.requireNonNull(getClass().getResource("/sample.xml"));
       SchemaFactory schemaFactory =  SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

       Schema schema = schemaFactory.newSchema(getClass().getResource("/xsd/config.xsd"));
       try (InputStream inputStream = url.openStream()) {
        Validator validator =  schema.newValidator();
        
        validator.validate(new StreamSource(inputStream));

       }
        
        
    }

    @Test
    public void unmarshalXmlConfig() throws Exception {

        URL url = Objects.requireNonNull(getClass().getResource("/sample.xml"));

        try (InputStream inputStream = url.openStream()) {
            Configuration config = JAXB.unmarshal(inputStream, Configuration.class);

            assertThat(config).isNotNull();

            assertThat(config.getJdbcConfig()).isNotNull();
            assertThat(config.getServerConfig()).isNotNull();
            assertThat(config.getServerConfig().getPort()).isEqualTo(99);
            assertThat(config.getJdbcConfig().getUsername()).isEqualTo("scott");
            assertThat(config.getJdbcConfig().getPassword()).isEqualTo("tiger");
            assertThat(config.getJdbcConfig().getUrl()).isEqualTo("foo:bar");

            assertThat(config.getPeers()).hasSize(2);
            assertThat(config.getPeers().stream().map(Peer::getUrl).collect(Collectors.toList()))
                    .containsExactly("http://bogus1.com", "http://bogus2.com");

            assertThat(config.getPeers().get(0).getPublicKey().getPath()).isEqualTo(Paths.get("/tmp/someppath1"));
            assertThat(config.getPeers().get(1).getPublicKey().getPath()).isEqualTo(Paths.get("/tmp/someppath2"));

            assertThat(config.getPrivateKeys()).hasSize(3);
            assertThat(config.getPrivateKeys().get(0).getType()).isEqualTo(PrivateKeyType.LOCKED);
            assertThat(config.getPrivateKeys().get(0).getPath()).isNull();
            assertThat(config.getPrivateKeys().get(0).getValue()).isEqualTo("PRIVATEKEY");
            assertThat(config.getPrivateKeys().get(0).getPassword()).isEqualTo("TOP_SECRET");

            assertThat(config.getPrivateKeys().get(1).getType()).isEqualTo(PrivateKeyType.UNLOCKED);

            assertThat(config.getPrivateKeys().get(2).getType()).isEqualTo(PrivateKeyType.LOCKED);

            assertThat(config.getPrivateKeys().get(2).getPath()).isEqualTo(Paths.get("/some/bogus/path"));
            assertThat(config.getPrivateKeys().get(2).getValue()).isNull();
        }
    }

}
