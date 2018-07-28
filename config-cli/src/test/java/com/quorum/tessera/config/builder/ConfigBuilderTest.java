package com.quorum.tessera.config.builder;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.config.test.FixtureUtil;
import org.eclipse.persistence.jaxb.BeanValidationMode;
import org.junit.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigBuilderTest {

    private final ConfigBuilder builderWithValidValues = FixtureUtil.builderWithValidValues();

    @Test
    public void buildValid() {
        Config result = builderWithValidValues.build();

        assertThat(result).isNotNull();
        builderWithValidValues.sslClientTrustCertificates(Arrays.asList("sslServerTrustCertificates"));
        assertThat(result.getUnixSocketFile()).isEqualTo(Paths.get("somepath.ipc"));

        assertThat(result.getKeys().getKeyData()).hasSize(1);

        KeyData keyData = result.getKeys().getKeyData().get(0);

        assertThat(keyData).isNotNull();
        assertThat(keyData.getConfig().getType()).isEqualTo(PrivateKeyType.LOCKED);

        ServerConfig serverConfig = result.getServerConfig();
        assertThat(serverConfig).isNotNull();

        SslConfig sslConfig = serverConfig.getSslConfig();
        assertThat(sslConfig).isNotNull();

        assertThat(sslConfig.getClientKeyStorePassword()).isEqualTo("sslClientKeyStorePassword");
        assertThat(sslConfig.getClientKeyStore()).isEqualTo(Paths.get("sslClientKeyStorePath"));
        assertThat(sslConfig.getClientTlsKeyPath()).isEqualTo(Paths.get("sslClientTlsKeyPath"));

        assertThat(sslConfig.getServerTrustCertificates())
                .containsExactly(Paths.get("sslServerTrustCertificates"));

        assertThat(result.getJdbcConfig().getUsername()).isEqualTo("jdbcUsername");
        assertThat(result.getJdbcConfig().getPassword()).isEqualTo("jdbcPassword");
        assertThat(result.getJdbcConfig().getUrl()).isEqualTo("jdbc:bogus");

        assertThat(result.getServerConfig().getPort()).isEqualTo(892);

    }

    @Test
    public void influxHostNameEmptyThenInfluxConfigIsNull() {
        Config result = builderWithValidValues.build();

        assertThat(result.getServerConfig().getInfluxConfig()).isNull();
    }

    /*
    Create config from existing config and ensure all 
    properties are populated using marhsalled values
     */
    @Test
    public void buildFromExisting() throws Exception {
        Config existing = builderWithValidValues.build();

        ConfigBuilder configBuilder = ConfigBuilder.from(existing);

        Config result = configBuilder.build();

        JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty("eclipselink.beanvalidation.mode", BeanValidationMode.NONE);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        // marshaller.marshal(existing, System.out);
        // marshaller.marshal(result, System.out);
        final String expected;
        try (Writer writer = new StringWriter()) {
            marshaller.marshal(existing, writer);
            expected = writer.toString();
        }

        final String actual;
        try (Writer writer = new StringWriter()) {
            marshaller.marshal(result, writer);
            actual = writer.toString();
        }

        Diff diff = DiffBuilder.compare(expected)
                .withTest(actual)
                .checkForSimilar()
                .build();

        assertThat(diff.getDifferences()).isEmpty();

    }

}
