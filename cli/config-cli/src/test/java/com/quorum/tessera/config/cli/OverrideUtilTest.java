package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.SslAuthenticationMode;
import com.quorum.tessera.config.util.JaxbUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlElement;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class OverrideUtilTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OverrideUtilTest.class);

    @Test
    public void buildOptions() {

        final List<String> expected
                = Arrays.asList(
                        "version",
                        "jdbc.username",
                        "jdbc.password",
                        "jdbc.url",
                        "jdbc.autoCreateTables",
                        "jdbc.fetchSize",
                        "peer.url",
                        "keys.passwordFile",
                        "keys.passwords",
                        "keys.keyData.config.data.aopts.algorithm",
                        "keys.keyData.config.data.aopts.iterations",
                        "keys.keyData.config.data.aopts.memory",
                        "keys.keyData.config.data.aopts.parallelism",
                        "keys.keyData.privateKeyPath",
                        "keys.azureKeyVaultConfig.url",
                        "keys.hashicorpKeyVaultConfig.approlePath",
                        "keys.hashicorpKeyVaultConfig.tlsKeyStorePath",
                        "keys.hashicorpKeyVaultConfig.tlsTrustStorePath",
                        "keys.hashicorpKeyVaultConfig.url",
                        "alwaysSendTo",
                        "unixSocketFile",
                        "useWhiteList",
                        "disablePeerDiscovery",
                        "serverConfigs.serverAddress",
                        "serverConfigs.cors.allowedOrigins",
                        "serverConfigs.cors.allowCredentials",
                        "serverConfigs.cors.allowedHeaders",
                        "serverConfigs.cors.allowedMethods",
                        "serverConfigs.influxConfig.dbName",
                        "serverConfigs.influxConfig.serverAddress",
                        "serverConfigs.influxConfig.pushIntervalInSecs",
                        "serverConfigs.influxConfig.sslConfig.serverTrustStore",
                        "serverConfigs.influxConfig.sslConfig.clientTrustCertificates",
                        "serverConfigs.influxConfig.sslConfig.serverKeyStorePassword",
                        "serverConfigs.influxConfig.sslConfig.clientKeyStorePassword",
                        "serverConfigs.influxConfig.sslConfig.serverTrustCertificates",
                        "serverConfigs.influxConfig.sslConfig.clientTlsKeyPath",
                        "serverConfigs.influxConfig.sslConfig.clientTrustStorePassword",
                        "serverConfigs.influxConfig.sslConfig.serverKeyStore",
                        "serverConfigs.influxConfig.sslConfig.serverTrustStorePassword",
                        "serverConfigs.influxConfig.sslConfig.clientKeyStore",
                        "serverConfigs.influxConfig.sslConfig.generateKeyStoreIfNotExisted",
                        "serverConfigs.influxConfig.sslConfig.serverTlsKeyPath",
                        "serverConfigs.influxConfig.sslConfig.clientTlsCertificatePath",
                        "serverConfigs.influxConfig.sslConfig.serverTlsCertificatePath",
                        "serverConfigs.influxConfig.sslConfig.clientTrustMode",
                        "serverConfigs.influxConfig.sslConfig.knownClientsFile",
                        "serverConfigs.influxConfig.sslConfig.serverTrustMode",
                        "serverConfigs.influxConfig.sslConfig.tls",
                        "serverConfigs.influxConfig.sslConfig.clientTrustStore",
                        "serverConfigs.influxConfig.sslConfig.environmentVariablePrefix",
                        "serverConfigs.influxConfig.sslConfig.sslConfigType",
                        "serverConfigs.influxConfig.sslConfig.knownServersFile",
                        "serverConfigs.sslConfig.serverTrustStore",
                        "serverConfigs.sslConfig.knownClientsFile",
                        "serverConfigs.sslConfig.serverTrustCertificates",
                        "serverConfigs.sslConfig.clientTrustCertificates",
                        "serverConfigs.sslConfig.clientTrustStorePassword",
                        "serverConfigs.sslConfig.generateKeyStoreIfNotExisted",
                        "serverConfigs.bindingAddress",
                        "serverConfigs.sslConfig.serverKeyStore",
                        "serverConfigs.sslConfig.serverTrustStorePassword",
                        "serverConfigs.sslConfig.serverKeyStorePassword",
                        "serverConfigs.sslConfig.clientTrustMode",
                        "serverConfigs.sslConfig.clientKeyStorePassword",
                        "serverConfigs.communicationType",
                        "serverConfigs.sslConfig.clientTlsCertificatePath",
                        "serverConfigs.sslConfig.serverTlsKeyPath",
                        "serverConfigs.sslConfig.clientKeyStore",
                        "serverConfigs.sslConfig.serverTrustMode",
                        "serverConfigs.sslConfig.clientTlsKeyPath",
                        "serverConfigs.app",
                        "serverConfigs.sslConfig.clientTrustStore",
                        "serverConfigs.enabled",
                        "serverConfigs.sslConfig.serverTlsCertificatePath",
                        "serverConfigs.sslConfig.tls",
                        "serverConfigs.sslConfig.knownServersFile",
                        "serverConfigs.sslConfig.environmentVariablePrefix",
                        "serverConfigs.sslConfig.sslConfigType",
                        "server.hostName",
                        "server.sslConfig.knownServersFile",
                        "server.sslConfig.clientTrustStorePassword",
                        "server.sslConfig.clientKeyStorePassword",
                        "server.sslConfig.clientTlsKeyPath",
                        "server.sslConfig.clientTrustCertificates",
                        "server.sslConfig.knownClientsFile",
                        "server.communicationType",
                        "server.sslConfig.serverTrustStorePassword",
                        "server.sslConfig.serverTrustCertificates",
                        "server.sslConfig.clientTrustStore",
                        "server.sslConfig.tls",
                        "server.sslConfig.serverTlsCertificatePath",
                        "server.grpcPort",
                        "server.sslConfig.serverKeyStore",
                        "server.port",
                        "server.sslConfig.generateKeyStoreIfNotExisted",
                        "server.sslConfig.clientTlsCertificatePath",
                        "server.sslConfig.serverTlsKeyPath",
                        "server.sslConfig.serverTrustStore",
                        "server.bindingAddress",
                        "server.sslConfig.serverTrustMode",
                        "server.sslConfig.clientKeyStore",
                        "server.sslConfig.clientTrustMode",
                        "server.sslConfig.serverKeyStorePassword",
                        "server.sslConfig.environmentVariablePrefix",
                        "server.sslConfig.sslConfigType",
                        "server.influxConfig.serverAddress",
                        "server.influxConfig.dbName",
                        "server.influxConfig.pushIntervalInSecs",
                        "server.influxConfig.sslConfig.serverTrustMode",
                        "server.influxConfig.sslConfig.clientTrustStore",
                        "server.influxConfig.sslConfig.environmentVariablePrefix",
                        "server.influxConfig.sslConfig.clientTlsKeyPath",
                        "server.influxConfig.sslConfig.clientTrustMode",
                        "server.influxConfig.sslConfig.serverKeyStore",
                        "server.influxConfig.sslConfig.serverTlsKeyPath",
                        "server.influxConfig.sslConfig.serverTrustCertificates",
                        "server.influxConfig.sslConfig.knownClientsFile",
                        "server.influxConfig.sslConfig.serverTrustStorePassword",
                        "server.influxConfig.sslConfig.serverTrustStore",
                        "server.influxConfig.sslConfig.clientTrustStorePassword",
                        "server.influxConfig.sslConfig.clientTlsCertificatePath",
                        "server.influxConfig.sslConfig.serverTlsCertificatePath",
                        "server.influxConfig.sslConfig.clientKeyStorePassword",
                        "server.influxConfig.sslConfig.knownServersFile",
                        "server.influxConfig.sslConfig.tls",
                        "server.influxConfig.sslConfig.clientTrustCertificates",
                        "server.influxConfig.sslConfig.clientKeyStore",
                        "server.influxConfig.sslConfig.generateKeyStoreIfNotExisted",
                        "server.influxConfig.sslConfig.serverKeyStorePassword",
                        "server.influxConfig.sslConfig.sslConfigType",
                        "features.enableRemoteKeyValidation",
                        "encryptor.type",
                        "keys.awsKeyVaultConfig.endpoint");

        final Map<String, Class> results = OverrideUtil.buildConfigOptions();

        assertThat(results.keySet())
                .filteredOn(s -> !s.contains("$jacocoData"))
                .containsExactlyInAnyOrderElementsOf(expected);

        assertThat(results.get("serverConfigs.sslConfig.knownClientsFile")).isEqualTo(Path.class);
        assertThat(results.get("keys.passwords")).isEqualTo(String[].class);
    }

    @Test
    @Ignore
    public void overrideExistingValueKeyDataWithPublicKey() {

        Config config = OverrideUtil.createInstance(Config.class);

        final String publicKeyValue = "PUBLIC_KEY";

        OverrideUtil.setValue(config, "keys.keyData.publicKey", publicKeyValue);

        assertThat(config.getKeys()).isNotNull();

        KeyConfiguration keyConfig = config.getKeys();

        assertThat(keyConfig.getKeyData()).hasSize(1);

        assertThat(keyConfig.getKeyData().get(0).getPublicKey()).isEqualTo(publicKeyValue);
    }

    @Test
    public void resolveFieldXmlElementName() {

        Field result = OverrideUtil.resolveField(SomeClass.class, "some_value");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("someValue");
    }

    @Test
    public void resolveField() {

        Field result = OverrideUtil.resolveField(SomeClass.class, "otherValue");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("otherValue");
    }

    static class SomeClass {

        private static SomeClass create() {
            return new SomeClass();
        }

        @XmlElement(name = "some_value")
        String someValue;

        @XmlElement
        String otherValue;
    }

    static class OtherClass {

        List<SomeClass> someList;
    }

    enum Foo {
        INSTANCE
    }

    @Test
    public void isSimple() {

        assertThat(OverrideUtil.isSimple(int.class)).isTrue();
        assertThat(OverrideUtil.isSimple(boolean.class)).isTrue();
        assertThat(OverrideUtil.isSimple(long.class)).isTrue();
        assertThat(OverrideUtil.isSimple(Foo.class)).isTrue();
        assertThat(OverrideUtil.isSimple(String.class)).isTrue();
        assertThat(OverrideUtil.isSimple(Integer.class)).isTrue();
        assertThat(OverrideUtil.isSimple(Long.class)).isTrue();
        assertThat(OverrideUtil.isSimple(Boolean.class)).isTrue();
        assertThat(OverrideUtil.isSimple(List.class)).isFalse();
    }

    @Test
    public void toArrayType() {
        assertThat(OverrideUtil.toArrayType(String.class)).isEqualTo(String[].class);
        assertThat(OverrideUtil.toArrayType(Path.class)).isEqualTo(Path[].class);
    }

    @Test
    public void createInstance() {
        Peer result = OverrideUtil.createInstance(Peer.class);
        assertThat(result).isNotNull();
    }

    @Test
    public void classForName() {
        Class type = OverrideUtil.classForName(getClass().getName());
        assertThat(type).isEqualTo(getClass());
    }

    @Test
    public void convertTo() {

        assertThat(OverrideUtil.convertTo(Path.class, "SOMEFILE")).isEqualTo(Paths.get("SOMEFILE"));

        assertThat(OverrideUtil.convertTo(String.class, "SOMESTR")).isEqualTo("SOMESTR");

        assertThat(OverrideUtil.convertTo(Integer.class, "999")).isEqualTo(999);

        assertThat(OverrideUtil.convertTo(Long.class, "999")).isEqualTo(999L);

        assertThat(OverrideUtil.convertTo(Boolean.class, "true")).isTrue();

        assertThat(OverrideUtil.convertTo(SslAuthenticationMode.class, "STRICT"))
                .isEqualTo(SslAuthenticationMode.STRICT);

        assertThat(OverrideUtil.convertTo(String.class, null)).isNull();
    }

    @Test
    public void initialiseNestedObjects() {

        Config config = new Config(null, null, null, null, null, null, true, true);

        OverrideUtil.initialiseNestedObjects(config);

        LOGGER.debug(JaxbUtil.marshalToStringNoValidation(config));

        assertThat(config.getJdbcConfig()).isNotNull();
        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getPeers()).isEmpty();
        assertThat(config.getAlwaysSendTo()).isEmpty();
        assertThat(config.isDisablePeerDiscovery()).isTrue();
    }

    @Test
    public void initialiseNestedObjectsWithNullValueDoesNothing() {
        final Throwable throwable = catchThrowable(() -> OverrideUtil.initialiseNestedObjects(null));
        assertThat(throwable).isNull();
    }

    @Test
    public void createConfigInstance() {
        Config config = OverrideUtil.createInstance(Config.class);
        assertThat(config).isNotNull();

        LOGGER.debug(JaxbUtil.marshalToStringNoValidation(config));

        assertThat(config.getJdbcConfig()).isNotNull();
        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getPeers()).isEmpty();
        assertThat(config.getAlwaysSendTo()).isEmpty();
    }

    @Test
    public void createConfigInstanceWithInterfaceReturnsNull() {
        final OverrideUtil interfaceObject = OverrideUtil.createInstance(OverrideUtil.class);
        assertThat(interfaceObject).isNull();
    }

    @Test
    public void setValue() {
        Config config = OverrideUtil.createInstance(Config.class);

        OverrideUtil.setValue(config, "jdbc.username", "someuser");
        OverrideUtil.setValue(config, "peers.url", "snonce1", "snonce2");

        assertThat(config.getJdbcConfig().getUsername()).isEqualTo("someuser");

        assertThat(config.getPeers().get(0).getUrl()).isEqualTo("snonce1");
        assertThat(config.getPeers().get(1).getUrl()).isEqualTo("snonce2");
    }

    @Test
    public void setValueWithoutAdditions() {
        final OtherClass someList = new OtherClass();
        OverrideUtil.setValue(someList, "someList.someValue", "password1", "password2");
        assertThat(someList.someList.get(0).someValue).isEqualTo("password1");
        assertThat(someList.someList.get(1).someValue).isEqualTo("password2");
    }

    @Test
    public void setValueOnNullDoesNothing() {
        final Throwable throwable = catchThrowable(() -> OverrideUtil.setValue(null, "jdbc.username", "someuser"));
        assertThat(throwable).isNull();
    }

    @Test
    public void setValuePreservePreDefined() throws Exception {
        final Config config;
        try (InputStream data = getClass().getResourceAsStream("/sample-config.json")) {
            config = JaxbUtil.unmarshal(data, Config.class);
        }

        OverrideUtil.setValue(config, "jdbc.username", "someuser");

        assertThat(config.getJdbcConfig().getUsername()).isEqualTo("someuser");
        assertThat(config.getJdbcConfig().getPassword()).isEqualTo("tiger");

        assertThat(config.getUnixSocketFile()).isEqualTo(Paths.get("${unixSocketPath}"));
    }

    // TODO: Need to support oerrides in config module
    @Ignore
    @Test
    public void definePrivateAndPublicKeyWithOverridesOnly() throws Exception {

        Config config = OverrideUtil.createInstance(Config.class);

        OverrideUtil.setValue(config, "keys.keyData.publicKey", "PUBLICKEY");
        OverrideUtil.setValue(config, "keys.keyData.privateKey", "PRIVATEKEY");
        // UNmarshlling to COnfig to
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            JaxbUtil.marshalWithNoValidation(config, bout);
            Config result = JaxbUtil.unmarshal(new ByteArrayInputStream(bout.toByteArray()), Config.class);
            assertThat(result.getKeys()).isNotNull();

            KeyConfiguration keyConfig = result.getKeys();

            assertThat(keyConfig.getKeyData()).hasSize(1);

            assertThat(keyConfig.getKeyData().get(0).getPrivateKey()).isEqualTo("PRIVATEKEY");

            assertThat(keyConfig.getKeyData().get(0).getPublicKey()).isEqualTo("PUBLICKEY");
        }
    }

    @Test
    public void defineAlwaysSendToWithOverridesOnly() throws Exception {

        Config config = OverrideUtil.createInstance(Config.class);

        OverrideUtil.setValue(config, "alwaysSendTo", "ONE", "TWO");

        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            JaxbUtil.marshalWithNoValidation(config, bout);

            Config result = JaxbUtil.unmarshal(new ByteArrayInputStream(bout.toByteArray()), Config.class);

            assertThat(result.getAlwaysSendTo()).hasSize(2);

            assertThat(result.getAlwaysSendTo()).containsOnly("ONE", "TWO");
        }
    }

    @Test
    public void convertToByteArray() {
        final byte[] result = OverrideUtil.convertTo(byte[].class, "HELLOW");
        assertThat(result).isEqualTo("HELLOW".getBytes());
    }

    @Test
    public void setValueWithAnnoClass() throws Exception {

        SomeIFace annon = new SomeIFace() {
            private String value = "HEllow";

            @Override
            public String getValue() {
                return value;
            }
        };
        
        OverrideUtil.setValue(annon, "value", "SOMETHING","SOMETHINGELSE");

    }

    interface SomeIFace {

        String getValue();
    }
}
