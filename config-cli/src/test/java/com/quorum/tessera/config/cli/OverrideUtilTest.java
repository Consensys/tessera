package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.SslAuthenticationMode;
import com.quorum.tessera.config.SslTrustMode;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.nacl.Key;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverrideUtilTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OverrideUtilTest.class);

    @Test
    public void buildOptions() {

        List<String> expected = Arrays.asList(
                "jdbc.username",
                "jdbc.password",
                "jdbc.url",
                "server.hostName",
                "server.port",
                "server.sslConfig.tls",
                "server.sslConfig.generateKeyStoreIfNotExisted",
                "server.sslConfig.serverKeyStore",
                "server.sslConfig.serverTlsKeyPath",
                "server.sslConfig.serverTlsCertificatePath",
                "server.sslConfig.serverKeyStorePassword",
                "server.sslConfig.serverTrustStore",
                "server.sslConfig.serverTrustStorePassword",
                "server.sslConfig.serverTrustMode",
                "server.sslConfig.clientKeyStore",
                "server.sslConfig.clientTlsKeyPath",
                "server.sslConfig.clientTlsCertificatePath",
                "server.sslConfig.clientKeyStorePassword",
                "server.sslConfig.clientTrustStore",
                "server.sslConfig.clientTrustStorePassword",
                "server.sslConfig.clientTrustMode",
                "server.sslConfig.knownClientsFile",
                "server.sslConfig.knownServersFile",
                "server.influxConfig.hostName",
                "server.influxConfig.port",
                "server.influxConfig.dbName",
                "server.influxConfig.pushIntervalInSecs",
                "peer.url",
                "keys.passwordFile",
                "keys.passwords",
                "keys.keyData.config.data.bytes",
                "keys.keyData.config.data.snonce",
                "keys.keyData.config.data.asalt",
                "keys.keyData.config.data.sbox",
                "keys.keyData.config.data.aopts.algorithm",
                "keys.keyData.config.data.aopts.iterations",
                "keys.keyData.config.data.aopts.memory",
                "keys.keyData.config.data.aopts.parallelism",
                "keys.keyData.config.data.password",
                "keys.keyData.config.type",
                "keys.keyData.privateKey",
                "keys.keyData.publicKey",
                "keys.keyData.privateKeyPath",
                "keys.keyData.publicKeyPath",
                "alwaysSendTo.keyBytes",
                "unixSocketFile",
                "useWhiteList",
                "server.sslConfig.clientTrustCertificates",
                "server.sslConfig.serverTrustCertificates"
        );

        Map<String, Class> results = OverrideUtil.buildConfigOptions();

        assertThat(results.keySet())
                .filteredOn(s -> !s.contains("$jacocoData"))
                .containsExactlyInAnyOrderElementsOf(expected);

        assertThat(results.get("server.sslConfig.knownClientsFile")).isEqualTo(Path.class);
        assertThat(results.get("keys.passwords")).isEqualTo(String[].class);

    }

    @Test
    public void initialiseConfigFromNoValues() throws Exception {

        Config config = OverrideUtil.createInstance(Config.class);

        assertThat(config).isNotNull();

        LOGGER.debug(JaxbUtil.marshalToStringNoValidation(config));

        OverrideUtil.setValue(config, "useWhiteList", "true");
        OverrideUtil.setValue(config, "jdbc.username", "someuser");
        OverrideUtil.setValue(config, "jdbc.password", "somepassword");
        OverrideUtil.setValue(config, "jdbc.url", "someurl");
        OverrideUtil.setValue(config, "server.hostName", "somehost");
        OverrideUtil.setValue(config, "server.port", "999");
        OverrideUtil.setValue(config, "keys.passwords", "pw_one", "pw_two");

        OverrideUtil.setValue(config, "server.sslConfig.clientKeyStorePassword", "SomeClientKeyStorePassword");

        OverrideUtil.setValue(config, "server.sslConfig.clientTrustStore", "ClientTrustStore");

        OverrideUtil.setValue(config, "server.sslConfig.clientTrustCertificates",
                "ClientTrustCertificates_1", "ClientTrustCertificates_2");

        OverrideUtil.setValue(config, "server.sslConfig.clientTrustMode", "CA_OR_TOFU");

        OverrideUtil.setValue(config, "server.influxConfig.pushIntervalInSecs", "987");

        OverrideUtil.setValue(config, "peers.url", "PEER1", "PEER2");

        LOGGER.debug(JaxbUtil.marshalToStringNoValidation(config));

        assertThat(config.getJdbcConfig()).isNotNull();
        assertThat(config.getJdbcConfig().getUsername()).isEqualTo("someuser");
        assertThat(config.getJdbcConfig().getPassword()).isEqualTo("somepassword");
        assertThat(config.getJdbcConfig().getUrl()).isEqualTo("someurl");

        assertThat(config.isUseWhiteList()).isTrue();

        assertThat(config.getPeers()).hasSize(2);
        assertThat(config.getKeys().getPasswords())
                .containsExactlyInAnyOrder("pw_one", "pw_two");

        assertThat(config.getServerConfig()).isNotNull();
        assertThat(config.getServerConfig().getHostName()).isEqualTo("somehost");
        assertThat(config.getServerConfig().getPort()).isEqualTo(999);

        assertThat(config.getServerConfig().getSslConfig().getClientKeyStorePassword())
                .isEqualTo("SomeClientKeyStorePassword");

        assertThat(config.getServerConfig().getSslConfig().getClientTrustStore())
                .isEqualTo(Paths.get("ClientTrustStore"));

        assertThat(config.getServerConfig().getSslConfig().getClientTrustMode())
                .isEqualTo(SslTrustMode.CA_OR_TOFU);

        assertThat(config.getServerConfig().getSslConfig().getClientTrustCertificates())
                .containsExactly(Paths.get("ClientTrustCertificates_1"), Paths.get("ClientTrustCertificates_2"));

        assertThat(config.getServerConfig().getInfluxConfig().getPushIntervalInSecs()).isEqualTo(987L);

        assertThat(config.getKeys()).isNotNull();

        KeyConfiguration keyConfig = config.getKeys();

        assertThat(keyConfig.getKeyData()).isEmpty();

    }

    @Test
    public void overrideExistingValueKeyDataWithPublicKey() throws Exception {

        Config config = OverrideUtil.createInstance(Config.class);

        final String publicKeyValue = "PUBLIC_KEY";

        OverrideUtil.setValue(config, "keys.keyData.publicKey", publicKeyValue);

        assertThat(config.getKeys()).isNotNull();

        KeyConfiguration keyConfig = config.getKeys();

        assertThat(keyConfig.getKeyData()).hasSize(1);

        assertThat(keyConfig.getKeyData().get(0).getPublicKey()).isEqualTo(publicKeyValue);
    }

    @Test
    public void overrideExistingValueKeyDataWithPrivateKeyType() throws Exception {

        Config config = OverrideUtil.createInstance(Config.class);

        final PrivateKeyType priavteKeyType = PrivateKeyType.UNLOCKED;

        OverrideUtil.setValue(config, "keys.keyData.config.type", priavteKeyType.name(), priavteKeyType.name());

        assertThat(config.getKeys()).isNotNull();

        KeyConfiguration keyConfig = config.getKeys();

        assertThat(keyConfig.getKeyData()).hasSize(2);

        assertThat(keyConfig.getKeyData().get(0)
                .getConfig().getType()).isEqualTo(priavteKeyType);

        assertThat(keyConfig.getKeyData().get(1)
                .getConfig().getType()).isEqualTo(priavteKeyType);

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

        @XmlElement(name = "some_value")
        private String someValue;

        @XmlElement
        private String otherValue;

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
        assertThat(OverrideUtil.toArrayType(String.class))
                .isEqualTo(String[].class);
        assertThat(OverrideUtil.toArrayType(Path.class))
                .isEqualTo(Path[].class);
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

        assertThat(OverrideUtil.convertTo(Path.class, "SOMEFILE"))
                .isEqualTo(Paths.get("SOMEFILE"));

        assertThat(OverrideUtil.convertTo(String.class, "SOMESTR"))
                .isEqualTo("SOMESTR");

        assertThat(OverrideUtil.convertTo(Integer.class, "999"))
                .isEqualTo(999);

        assertThat(OverrideUtil.convertTo(Long.class, "999"))
                .isEqualTo(999L);

        assertThat(OverrideUtil.convertTo(Boolean.class, "true"))
                .isTrue();

        assertThat(OverrideUtil.convertTo(SslAuthenticationMode.class, "STRICT"))
                .isEqualTo(SslAuthenticationMode.STRICT);

        assertThat(OverrideUtil.convertTo(String.class, null)).isNull();

    }

    @Test
    public void initialiseNestedObjects() {

        Config config = new Config(null, null, null, null, null, null, true);

        OverrideUtil.initialiseNestedObjects(config);

        LOGGER.debug(JaxbUtil.marshalToStringNoValidation(config));

        assertThat(config.getJdbcConfig()).isNotNull();
        assertThat(config.getServerConfig()).isNotNull();
        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getPeers()).isEmpty();
        assertThat(config.getAlwaysSendTo()).isEmpty();

    }

    @Test
    public void createConfigInstance() {
        Config config = OverrideUtil.createInstance(Config.class);
        assertThat(config).isNotNull();

        LOGGER.debug(JaxbUtil.marshalToStringNoValidation(config));

        assertThat(config.getJdbcConfig()).isNotNull();
        assertThat(config.getServerConfig()).isNotNull();
        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getPeers()).isEmpty();
        assertThat(config.getAlwaysSendTo()).isEmpty();

    }

    @Test
    public void overrideExistingValueKeyDataWithPrivateKeyData() throws Exception {

        Config config = OverrideUtil.createInstance(Config.class);

        final String value = "NONCE";

        OverrideUtil.setValue(config, "keys.keyData.config.privateKeyData.snonce", value);

        assertThat(config.getKeys()).isNotNull();

        KeyConfiguration keyConfig = config.getKeys();

        assertThat(keyConfig.getKeyData()).hasSize(1);

        assertThat(keyConfig.getKeyData().get(0)
                .getConfig()
                .getPrivateKeyData()
                .getSnonce()).isEqualTo(value);

    }

    @Test
    public void setValue() {
        Config config = OverrideUtil.createInstance(Config.class);

        OverrideUtil.setValue(config, "jdbc.username", "someuser");
        OverrideUtil.setValue(config, "keys.keyData.config.privateKeyData.snonce", "snonce1", "snonce2");

        assertThat(config.getJdbcConfig().getUsername()).isEqualTo("someuser");

        assertThat(config.getKeys().getKeyData().get(0).getConfig().getSnonce()).isEqualTo("snonce1");
        assertThat(config.getKeys().getKeyData().get(1).getConfig().getSnonce()).isEqualTo("snonce2");
    }

    @Test
    public void setValuePreservePreDefined() throws Exception {
        final Config config;
        try (InputStream data = getClass().getResourceAsStream("/sample-config.json")) {
            config = JaxbUtil.unmarshal(data, Config.class);
        }

        OverrideUtil.setValue(config, "jdbc.username", "someuser");
        OverrideUtil.setValue(config, "keys.keyData.config.privateKeyData.snonce", "snonce1", "snonce2");

        assertThat(config.getJdbcConfig().getUsername()).isEqualTo("someuser");
        assertThat(config.getJdbcConfig().getPassword()).isEqualTo("tiger");

        assertThat(config.getKeys().getKeyData().get(0).getConfig().getSnonce()).isEqualTo("snonce1");
        assertThat(config.getKeys().getKeyData().get(0).getPublicKey()).isEqualTo("PUBLICKEY");

        assertThat(config.getKeys().getKeyData().get(1).getConfig().getSnonce()).isEqualTo("snonce2");
        assertThat(config.getKeys().getKeyData().get(1).getPublicKey()).isNull();
        assertThat(config.getUnixSocketFile()).isEqualTo(Paths.get("${unixSocketPath}"));
    }

    //TODO: Need to support oerrides in config module
    @Ignore
    @Test
    public void definePrivateAndPublicKeyWithOverridesOnly() throws Exception {

        Config config = OverrideUtil.createInstance(Config.class);

        JaxbUtil.marshalWithNoValidation(config, System.out);

        OverrideUtil.setValue(config, "keys.keyData.publicKey", "PUBLICKEY");
        OverrideUtil.setValue(config, "keys.keyData.privateKey", "PRIVATEKEY");
        //UNmarshlling to COnfig to 
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

        JaxbUtil.marshalWithNoValidation(config, System.out);

        OverrideUtil.setValue(config, "alwaysSendTo.keyBytes", "ONE", "TWO");

        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            JaxbUtil.marshalWithNoValidation(config, bout);

            Config result = JaxbUtil.unmarshal(new ByteArrayInputStream(bout.toByteArray()), Config.class);

            assertThat(result.getAlwaysSendTo()).hasSize(2);

            assertThat(result.getAlwaysSendTo()).containsOnly(new Key("ONE".getBytes()), new Key("TWO".getBytes()));
        }
    }

}
