package com.github.nexus.ssl.util;

import org.bouncycastle.operator.OperatorCreationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class TlsUtilsTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    public static TemporaryFolder dirDelegate;

    private static final String FILE = "./test-keystore";
    private static final String PASSWORD ="quorum";
    private static final String ALIAS = "nexus";

    @After
    public void after(){
        dirDelegate = tmpDir;
        assertThat(dirDelegate.getRoot().exists()).isTrue();
    }

    @AfterClass
    public static void tearDown(){
        assertThat(dirDelegate.getRoot().exists()).isFalse();
    }

    @Test
    public void testGenerateKeys() throws OperatorCreationException, InvalidKeyException, NoSuchAlgorithmException, IOException, SignatureException, NoSuchProviderException, CertificateException, KeyStoreException {

        File privateKeyFile = new File(tmpDir.getRoot(),FILE);

        assertThat(privateKeyFile.exists()).isFalse();

        privateKeyFile.createNewFile();

        TlsUtils.create().generateKeyStoreWithSelfSignedCertificate(privateKeyFile,PASSWORD);

        assertThat(privateKeyFile.exists()).isTrue();

        //Read keystore from created file
        final KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(privateKeyFile), PASSWORD.toCharArray());
        Certificate certificate = keyStore.getCertificate(ALIAS);

        assertThat(certificate).isNotNull();
        assertThat(certificate.getPublicKey()).isNotNull();

    }
}
