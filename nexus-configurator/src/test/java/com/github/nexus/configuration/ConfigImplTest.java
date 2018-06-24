package com.github.nexus.configuration;

import com.github.nexus.configuration.model.KeyData;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import java.nio.file.Paths;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ConfigImplTest {

    @Test
    public void gettersManipulateProperties() {

        final Properties configProperties = new Properties();

        configProperties.setProperty("keygenBasePath", "basepath");
        configProperties.setProperty("url", "http://url.com");
        configProperties.setProperty("port", "2000");
        configProperties.setProperty("othernodes", "node1.com,node2.com:10000");
        configProperties.setProperty("generatekeys", "newkey1,newkey2");
        configProperties.setProperty("whitelist", "ip1,,ip2");
        configProperties.setProperty("workdir", "/tmp");
        configProperties.setProperty("socket", "tst.ipc");
        configProperties.setProperty("databaseURL","h2");
        configProperties.setProperty("tls","off");
        configProperties.setProperty("serverKeyStore","serverKeyStore");
        configProperties.setProperty("serverKeyStorePassword","serverKeyStorePassword");
        configProperties.setProperty("serverTrustStore","serverTrustStore");
        configProperties.setProperty("serverTrustStorePassword","serverTrustStorePassword");
        configProperties.setProperty("serverTrustMode","CA");
        configProperties.setProperty("knownClients","knownClients");
        configProperties.setProperty("clientKeyStore","clientKeyStore");
        configProperties.setProperty("clientKeyStorePassword","clientKeyStorePassword");
        configProperties.setProperty("clientTrustStore","clientTrustStore");
        configProperties.setProperty("clientTrustStorePassword","clientTrustStorePassword");
        configProperties.setProperty("clientTrustMode","TOFU");
        configProperties.setProperty("knownServers","knownServers");

        final Configuration configuration = new ConfigurationImpl(configProperties);

        assertThat(configuration.keygenBasePath()).isEqualTo(Paths.get("basepath").toAbsolutePath());
        assertThat(configuration.url()).isEqualTo("http://url.com");
        assertThat(configuration.port()).isEqualTo(2000);
        assertThat(configuration.othernodes()).hasSize(2).containsExactly("node1.com", "node2.com:10000");
        assertThat(configuration.generatekeys()).hasSize(2).containsExactly("newkey1", "newkey2");
        assertThat(configuration.uri().toString()).isEqualTo("http://url.com:2000");
        assertThat(configuration.whitelist()).hasSize(2).containsExactlyInAnyOrder("ip1", "ip2");
        assertThat(configuration.workdir()).isEqualTo("/tmp");
        assertThat(configuration.socket()).isEqualTo("tst.ipc");
        assertThat(configuration.databaseURL()).isEqualTo("h2");
        assertThat(configuration.tls()).isEqualTo("off");
        assertThat(configuration.serverKeyStore()).isEqualTo("serverKeyStore");
        assertThat(configuration.serverKeyStorePassword()).isEqualTo("serverKeyStorePassword");
        assertThat(configuration.serverTrustStore()).isEqualTo("serverTrustStore");
        assertThat(configuration.serverTrustStorePassword()).isEqualTo("serverTrustStorePassword");
        assertThat(configuration.serverTrustMode()).isEqualTo("CA");
        assertThat(configuration.knownClients()).isEqualTo("knownClients");
        assertThat(configuration.clientKeyStore()).isEqualTo("clientKeyStore");
        assertThat(configuration.clientKeyStorePassword()).isEqualTo("clientKeyStorePassword");
        assertThat(configuration.clientTrustStore()).isEqualTo("clientTrustStore");
        assertThat(configuration.clientTrustStorePassword()).isEqualTo("clientTrustStorePassword");
        assertThat(configuration.clientTrustMode()).isEqualTo("TOFU");
        assertThat(configuration.knownServers()).isEqualTo("knownServers");



    }

    @Test
    public void keysProperties() {

        final JsonObject[] privateKeys = new JsonObject[]{
            Json.createObjectBuilder().add("key", "priv1").build(),
            Json.createObjectBuilder().add("key", "priv2").build(),
            Json.createObjectBuilder().add("key", "priv3").build()
        };

        final Properties configProperties = new Properties();

        configProperties.setProperty("publicKeys", "key1,key2,key3");
        configProperties.setProperty("privateKeys", "{\"key\": \"priv1\"},{\"key\": \"priv2\"},{\"key\": \"priv3\"}");
        configProperties.setProperty("passwords", "p1,,p2");

        final Configuration configuration = new ConfigurationImpl(configProperties);

        assertThat(configuration.publicKeys()).hasSize(3).containsExactly("key1", "key2", "key3");
        assertThat(configuration.privateKeys()).hasSize(3).containsExactlyInAnyOrder(privateKeys);
        assertThat(configuration.passwords()).hasSize(3).containsExactly("p1", "", "p2");
        assertThat(configuration.keyData())
            .hasSize(3)
            .containsExactlyInAnyOrder(
                new KeyData("key1", privateKeys[0], "p1"),
                new KeyData("key2", privateKeys[1], ""),
                new KeyData("key3", privateKeys[2], "p2")
            );

    }

    @Test
    public void errorThrownIfSameNumberOfKeysAndPasswordsNotProvided() {

        final Properties configProperties = new Properties();

        configProperties.setProperty("publicKeys", "key1,key2,key3");
        configProperties.setProperty("privateKeys", "{\"key\": \"priv1\"},{\"key\": \"priv2\"},{\"key\": \"priv3\"}");
        configProperties.setProperty("passwords", "p1,");

        final Throwable throwable = catchThrowable(() -> new ConfigurationImpl(configProperties).keyData());

        assertThat(throwable)
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Public, private keys and passwords must match up");

    }

    @Test
    public void emptyKeysAtEndReturnsCorrectAmount() {

        final Properties configProperties = new Properties();
        configProperties.put("passwords", "p1,p2,,");

        final Configuration configuration = new ConfigurationImpl(configProperties);

        assertThat(configuration.passwords()).hasSize(4);

    }

    @Test
    public void emptyKeynamesGetFiltered() {
        final Properties configProperties = new Properties();
        configProperties.put("generatekeys", "p1,,p2");

        final Configuration configuration = new ConfigurationImpl(configProperties);

        assertThat(configuration.generatekeys()).hasSize(2);
    }

}
