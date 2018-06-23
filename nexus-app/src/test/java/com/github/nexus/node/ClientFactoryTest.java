package com.github.nexus.node;

import org.junit.Test;

import javax.ws.rs.client.Client;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class ClientFactoryTest {

    ClientFactory factory = new ClientFactory();

    @Test
    public void testBuildInsecureClient() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        Client client = factory.buildClient("off","","","","","","");
        assertThat(client).isNotNull();
    }

    @Test
    public void testBuildSecureClientCAMode() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        Client client = factory.buildClient("strict","","","","","CA","");
        assertThat(client).isNotNull();
        assertThat(client.getSslContext()).isNotNull();
        assertThat(client.getSslContext().getProtocol().equals("TLS"));
    }

    @Test
    public void testBuildSecureClientDefaultMode() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        Client client = factory.buildClient("strict","","","","","something invalid","");
        assertThat(client).isNotNull();
        assertThat(client.getSslContext()).isNotNull();
        assertThat(client.getSslContext().getProtocol().equals("TLS"));
    }

    @Test
    public void testBuildSecureClientTOFUMode() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        Client client = factory.buildClient("strict","","","","","TOFU","");
        assertThat(client).isNotNull();
        assertThat(client.getSslContext()).isNotNull();
        assertThat(client.getSslContext().getProtocol().equals("TLS"));
    }

    @Test
    public void testBuildSecureClientWhiteListMode() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        Client client = factory.buildClient("strict","","","","","WHITELIST","");
        assertThat(client).isNotNull();
        assertThat(client.getSslContext()).isNotNull();
        assertThat(client.getSslContext().getProtocol().equals("TLS"));
    }

    @Test
    public void testBuildSecureTrustAllMode() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        Client client = factory.buildClient("strict","","","","","NONE","");
        assertThat(client).isNotNull();
        assertThat(client.getSslContext()).isNotNull();
        assertThat(client.getSslContext().getProtocol().equals("TLS"));
    }
}
