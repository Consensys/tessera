package com.github.nexus.ssl.util;

import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class TlsUtilsTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @Test
    public void testGenerateKeys() throws OperatorCreationException, InvalidKeyException, NoSuchAlgorithmException, IOException, SignatureException, NoSuchProviderException, CertificateException, KeyStoreException {

        File privateKeyFile = new File(tmpDir.getRoot(),"./test-keystore");

        privateKeyFile.createNewFile();

        TlsUtils.create().generateKeyStoreWithSelfSignedCertificate(privateKeyFile,"quorum");

    }
}
