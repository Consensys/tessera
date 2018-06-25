package com.github.nexus.node;

import org.bouncycastle.operator.OperatorCreationException;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import javax.ws.rs.client.Client;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class ClientFactoryTest {

    ClientFactory factory = new ClientFactory();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    static TemporaryFolder delegate;

    File tmpKeyFile;

    @Before
    public void setUp() {
        tmpKeyFile = new File(temporaryFolder.getRoot(), "test-keystore");
    }

    @After
    public void after(){
        delegate = temporaryFolder;
        assertThat(delegate.getRoot().exists()).isTrue();
    }

    @AfterClass
    public static void tearDown(){
        assertThat(delegate.getRoot().exists()).isFalse();
    }

    @Test
    public void testBuildInsecureClient() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {
        Client client = factory.buildClient("somethingInvalid","","","","","","");
        assertThat(client).isNotNull();
    }

    @Test
    public void testBuildSecureClientCAMode() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {
        Client client = factory.buildClient(
            "strict",
            tmpKeyFile.getPath(),
            "quorum",
            "",
            "",
            "CA",
            "");
        assertThat(client).isNotNull();
        assertThat(client.getSslContext()).isNotNull();
        assertThat(client.getSslContext().getProtocol().equals("TLS"));
    }

    @Test
    public void testBuildSecureClientDefaultMode() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {
        Client client = factory.buildClient("strict",tmpKeyFile.getPath(),"","","","something invalid","");
        assertThat(client).isNotNull();
        assertThat(client.getSslContext()).isNotNull();
        assertThat(client.getSslContext().getProtocol().equals("TLS"));
    }

    @Test
    public void testBuildSecureClientTOFUMode() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {
        Client client = factory.buildClient("strict",tmpKeyFile.getPath(),"quorum","","","TOFU","");
        assertThat(client).isNotNull();
        assertThat(client.getSslContext()).isNotNull();
        assertThat(client.getSslContext().getProtocol().equals("TLS"));
    }

    @Test
    public void testBuildSecureClientWhiteListMode() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {
        Client client = factory.buildClient("strict",tmpKeyFile.getPath(),"quorum","","","WHITELIST","");
        assertThat(client).isNotNull();
        assertThat(client.getSslContext()).isNotNull();
        assertThat(client.getSslContext().getProtocol().equals("TLS"));
    }

    @Test
    public void testBuildSecureTrustAllMode() throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {
        Client client = factory.buildClient("strict",tmpKeyFile.getPath(),"quorum","","","NONE","");
        assertThat(client).isNotNull();
        assertThat(client.getSslContext()).isNotNull();
        assertThat(client.getSslContext().getProtocol().equals("TLS"));
    }
}
