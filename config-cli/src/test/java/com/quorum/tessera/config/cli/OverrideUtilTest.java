package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.*;
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

        final List<String> expected = Arrays.asList(
                "jdbc.username",
                "jdbc.password",
                "jdbc.url",
                "server.hostName",
                "server.port",
                "server.grpcPort",
                "server.communicationType",
                "server.bindingAddress",
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
//                "keys.keyData.config.data.bytes",
//                "keys.keyData.config.data.snonce",
//                "keys.keyData.config.data.asalt",
//                "keys.keyData.config.data.sbox",
                "keys.keyData.config.data.aopts.algorithm",
                "keys.keyData.config.data.aopts.iterations",
                "keys.keyData.config.data.aopts.memory",
                "keys.keyData.config.data.aopts.parallelism",
//                "keys.keyData.config.data.password",
//                "keys.keyData.config.type",
//                "keys.keyData.privateKey",
//                "keys.keyData.publicKey",
                "keys.keyData.privateKeyPath",
//                "keys.keyData.publicKeyPath",
                "alwaysSendTo",
                "unixSocketFile",
                "useWhiteList",
                "server.sslConfig.clientTrustCertificates",
                "server.sslConfig.serverTrustCertificates",
                "disablePeerDiscovery"
        );

        final Map<String, Class> results = OverrideUtil.buildConfigOptions();

        assertThat(results.keySet())
            .filteredOn(s -> !s.contains("$jacocoData"))
            .containsExactlyInAnyOrderElementsOf(expected);

        assertThat(results.get("server.sslConfig.knownClientsFile")).isEqualTo(Path.class);
        assertThat(results.get("keys.passwords")).isEqualTo(String[].class);

    }

    @Test
    public void initialiseConfigFromNoValues() {

        Config config = OverrideUtil.createInstance(Config.class);

        assertThat(config).isNotNull();

        LOGGER.debug(JaxbUtil.marshalToStringNoValidation(config));

        OverrideUtil.setValue(config, "useWhiteList", "true");
        OverrideUtil.setValue(config, "jdbc.username", "someuser");
        OverrideUtil.setValue(config, "jdbc.password", "somepassword");
        OverrideUtil.setValue(config, "jdbc.url", "someurl");
        OverrideUtil.setValue(config, "server.hostName", "somehost");
        OverrideUtil.setValue(config, "server.port", "999");
        OverrideUtil.setValue(config, "server.grpcPort", "50000");
        OverrideUtil.setValue(config, "server.bindingAddress", "http://binding:9999");
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

        assertThat(config.getKeys()).isNotNull();
        assertThat(config.getKeys().getPasswords()).containsExactlyInAnyOrder("pw_one", "pw_two");
        assertThat(config.getKeys().getKeyData()).isEmpty();

        final ServerConfig serverConfig = config.getServerConfig();
        assertThat(serverConfig).isNotNull();
        assertThat(serverConfig.getHostName()).isEqualTo("somehost");
        assertThat(serverConfig.getPort()).isEqualTo(999);
        assertThat(serverConfig.getGrpcPort()).isEqualTo(50000);
        assertThat(serverConfig.getBindingAddress()).isEqualTo("http://binding:9999");

        assertThat(serverConfig.getSslConfig().getClientKeyStorePassword()).isEqualTo("SomeClientKeyStorePassword");

        assertThat(serverConfig.getSslConfig().getClientTrustStore()).isEqualTo(Paths.get("ClientTrustStore"));

        assertThat(serverConfig.getSslConfig().getClientTrustMode()).isEqualTo(SslTrustMode.CA_OR_TOFU);

        assertThat(serverConfig.getSslConfig().getClientTrustCertificates())
            .containsExactly(Paths.get("ClientTrustCertificates_1"), Paths.get("ClientTrustCertificates_2"));

        assertThat(serverConfig.getInfluxConfig().getPushIntervalInSecs()).isEqualTo(987L);

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
    public void overrideExistingValueKeyDataWithPrivateKeyType() {
//
//        Config config = OverrideUtil.createInstance(Config.class);
//
//        final PrivateKeyType privateKeyType = PrivateKeyType.UNLOCKED;
//
//        OverrideUtil.setValue(config, "keys.keyData.config.type", privateKeyType.name(), privateKeyType.name());
//
//        assertThat(config.getKeys()).isNotNull();
//
//        KeyConfiguration keyConfig = config.getKeys();
//
//        assertThat(keyConfig.getKeyData()).hasSize(2);
//
//        assertThat(keyConfig.getKeyData().get(0).getConfig().getType()).isEqualTo(privateKeyType);
//        assertThat(keyConfig.getKeyData().get(1).getConfig().getType()).isEqualTo(privateKeyType);
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

        Config config = new Config(null, null, null, null, null, null, true,true);

        OverrideUtil.initialiseNestedObjects(config);

        LOGGER.debug(JaxbUtil.marshalToStringNoValidation(config));

        assertThat(config.getJdbcConfig()).isNotNull();
        assertThat(config.getServerConfig()).isNotNull();
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
        assertThat(config.getServerConfig()).isNotNull();
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
    public void overrideExistingValueKeyDataWithPrivateKeyData() {
//
//        Config config = OverrideUtil.createInstance(Config.class);
//
//        final String value = "NONCE";
//
//        OverrideUtil.setValue(config, "keys.keyData.config.privateKeyData.snonce", value);
//
//        assertThat(config.getKeys()).isNotNull();
//
//        KeyConfiguration keyConfig = config.getKeys();
//
//        assertThat(keyConfig.getKeyData()).hasSize(1);
//
//        assertThat(keyConfig.getKeyData().get(0)
//            .getConfig()
//            .getPrivateKeyData()
//            .getSnonce()).isEqualTo(value);
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

}
