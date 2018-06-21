package com.github.nexus.ssl;

import com.github.nexus.ssl.util.TlsUtils;
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
        TlsUtils tlsUtils = new TlsUtils();

        File certificateFile = new File(tmpDir.getRoot(), "server.key");
        File privateKeyFile = new File(tmpDir.getRoot(), "server.cer");

        tlsUtils.generateKeyStoreWithSelfSignedCertificate(privateKeyFile, certificateFile);

//        File resultPrivateKeyFile = new File(tmpDir.getRoot(), "server.key");
//        assertTrue(resultPrivateKeyFile.exists());

    }
}
