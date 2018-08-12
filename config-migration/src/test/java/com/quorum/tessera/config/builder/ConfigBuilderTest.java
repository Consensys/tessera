package com.quorum.tessera.config.builder;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.migration.test.FixtureUtil;
import org.junit.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigBuilderTest {

    private final ConfigBuilder builderWithValidValues = FixtureUtil.builderWithValidValues();

    private final ConfigBuilder builderWithNullValues = FixtureUtil.builderWithNullValues();

    @Test
    public void nullIsNullAndNotAStringWithTheValueOfNull() {
        assertThat(ConfigBuilder.toPath(null)).isNull();
        assertThat(ConfigBuilder.toPath("test")).isNotNull();
    }

    @Test
    public void buildValid() {
        Config result = builderWithValidValues.build();

        assertThat(result).isNotNull();
        builderWithValidValues.sslClientTrustCertificates(Arrays.asList(Paths.get("sslServerTrustCertificates")));
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
        Enum enu = Enum.valueOf(Class.class.cast(marshaller
                .getProperty("eclipselink.beanvalidation.mode").getClass()), "NONE");
        marshaller.setProperty("eclipselink.beanvalidation.mode", enu);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        marshaller.marshal(existing, System.out);
        marshaller.marshal(result, System.out);
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

    @Test
    public void buildFromExistingWithNulls() throws Exception {
        Method createMethod = Config.class.getDeclaredMethod("create");
        createMethod.setAccessible(true);
        Config existing = new Config(new JdbcConfig(null,null,null),
            new ServerConfig(null,null,
                new SslConfig(null,true,null,null,
                    null,null,null,
                    null,null,null,
                    null,null,null,null,null,
                    null,null,null,
                    null,null),
                null
            ), null,null, null, null, true);

        ConfigBuilder configBuilder = ConfigBuilder.from(existing);

        Config result = configBuilder.build();

        JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);

        Marshaller marshaller = jaxbContext.createMarshaller();
        Enum enu = Enum.valueOf(Class.class.cast(marshaller
            .getProperty("eclipselink.beanvalidation.mode").getClass()), "NONE");
        marshaller.setProperty("eclipselink.beanvalidation.mode", enu);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        marshaller.marshal(existing, System.out);
        marshaller.marshal(result, System.out);
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

    @Test
    public void alwaysSendToFileNotFoundPrintsErrorMessageToTerminal() {
        List<String> alwaysSendTo = new ArrayList<>();
        alwaysSendTo.add("doesntexist.txt");
        alwaysSendTo.add("alsodoesntexist.txt");

        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final PrintStream originalErr = System.err;

        System.setErr(new PrintStream(errContent));

        final ConfigBuilder builder = builderWithValidValues.alwaysSendTo(alwaysSendTo);
        builder.build();

        assertThat(errContent.toString()).isEqualTo("Error reading alwayssendto file: doesntexist.txt\n" +
                                                    "Error reading alwayssendto file: alsodoesntexist.txt\n");

        System.setErr(originalErr);

    }

    @Test
    public void buildWithNoValuesSetDoesNotThrowException() {
        final ConfigBuilder builder = ConfigBuilder.create();

        Config config = builder.build();

        assertThat(config).isNotNull();
    }

}
