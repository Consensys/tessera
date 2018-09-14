package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.io.FilesDelegate;
import com.quorum.tessera.io.IOCallback;
import com.quorum.tessera.nacl.NaclException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public class KeyDataAdapter extends XmlAdapter<KeyData, KeyData> {
    
    private FilesDelegate filesDelegate = FilesDelegate.create();
    
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyDataAdapter.class);

    public static final String NACL_FAILURE_TOKEN = "NACL_FAILURE";
    
    @Override
    public KeyData unmarshal(final KeyData keyData) {

        //case1: public and private keys provided inline
        if(keyData.hasKeys()) {
            return keyData;
        }

        //case2: public key and private key config provided inline
        if(keyData.getConfig() != null && keyData.getPublicKey() != null) {
            return unmarshalInline(keyData);
        }

        //case3: public and private keys provided in files
        if(keyData.getPrivateKeyPath() != null && keyData.getPublicKeyPath() != null &&
            filesDelegate.exists(keyData.getPublicKeyPath()) && filesDelegate.exists(keyData.getPrivateKeyPath())) {
            return unmarshalFile(keyData);
        }

        //case4: public key and private key vault id provided
        //(plus alkel invalid cases which are picked up later by constraint validation)
        return keyData;
    }

    private KeyData unmarshalFile(final KeyData keyData) {
        final Path publicKeyPath = keyData.getPublicKeyPath();
        final Path privateKeyPath = keyData.getPrivateKeyPath();
        final String password = Optional.ofNullable(keyData.getConfig()).map(KeyDataConfig::getPassword).orElse(null);

        final byte[] publicKey = IOCallback.execute(() -> Files.readAllBytes(publicKeyPath));
        final String publicKeyString = new String(publicKey, UTF_8);

        final byte[] privateKey = IOCallback.execute(() -> Files.readAllBytes(privateKeyPath));
        final String privateKeyString = new String(privateKey, UTF_8);

        final KeyDataConfig unmarshal
                = JaxbUtil.unmarshal(new ByteArrayInputStream(privateKeyString.getBytes(UTF_8)), KeyDataConfig.class);

        return this.unmarshalInline(
                new KeyData(
                        new KeyDataConfig(
                                new PrivateKeyData(
                                        unmarshal.getValue(),
                                        unmarshal.getSnonce(),
                                        unmarshal.getAsalt(),
                                        unmarshal.getSbox(),
                                        unmarshal.getArgonOptions(),
                                        password
                                ),
                                unmarshal.getType()
                        ),
                        null,
                        publicKeyString,
                        privateKeyPath,
                        publicKeyPath,
                        keyData.getAzureKeyVaultId()
                )
        );

    }

    private KeyData unmarshalInline(final KeyData keyData) {
        if (keyData.getConfig().getType() == PrivateKeyType.UNLOCKED) {
            return new KeyData(keyData.getConfig(), keyData.getConfig().getValue(), keyData.getPublicKey(), null, null, null);
        }

        if (keyData.getConfig().getPassword() == null) {
            return keyData;
        }

        final KeyEncryptor kg = KeyEncryptorFactory.create();
        final PrivateKeyData encryptedKey = keyData.getConfig().getPrivateKeyData();

        String decryptedPrivateKey;
        try {
            decryptedPrivateKey = Objects.toString(kg.decryptPrivateKey(encryptedKey));
        } catch (final NaclException ex) {
            LOGGER.debug("Unable to decrypt private key : {}", ex.getMessage());
            decryptedPrivateKey = NACL_FAILURE_TOKEN +": " + ex.getMessage();
        }

        //need to decrypt
        return new KeyData(
                keyData.getConfig(),
                decryptedPrivateKey,
                keyData.getPublicKey(),
                keyData.getPrivateKeyPath(),
                keyData.getPublicKeyPath(),
                keyData.getAzureKeyVaultId()
        );
    }

    @Override
    public KeyData marshal(final KeyData keyData) {

        if (keyData.getConfig() == null) {
            return keyData;
        }

        if(keyData.getPrivateKeyPath()!=null || keyData.getPublicKeyPath()!=null) {
            return new KeyData(
                null, null, null, keyData.getPrivateKeyPath(), keyData.getPublicKeyPath(), keyData.getAzureKeyVaultId()
            );
        }

        if (keyData.getConfig().getType() != PrivateKeyType.UNLOCKED) {
            return new KeyData(
                    new KeyDataConfig(
                            new PrivateKeyData(
                                    null,
                                    keyData.getConfig().getPrivateKeyData().getSnonce(),
                                    keyData.getConfig().getPrivateKeyData().getAsalt(),
                                    keyData.getConfig().getPrivateKeyData().getSbox(),
                                    keyData.getConfig().getPrivateKeyData().getArgonOptions(),
                                    null
                            ),
                            keyData.getConfig().getType()
                    ),
                    null,
                    keyData.getPublicKey(),
                    keyData.getPrivateKeyPath(),
                    keyData.getPublicKeyPath(),
                    keyData.getAzureKeyVaultId()
            );
        }

        return keyData;
    }

    protected void setFilesDelegate(FilesDelegate filesDelegate) {
        this.filesDelegate = filesDelegate;
    }

}
