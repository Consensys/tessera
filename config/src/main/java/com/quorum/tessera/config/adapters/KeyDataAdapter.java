package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.io.FilesDelegate;
import com.quorum.tessera.io.IOCallback;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.nacl.NaclException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Objects;

public class KeyDataAdapter extends XmlAdapter<KeyData, KeyData> {
    
    private FilesDelegate filesDelegate = FilesDelegate.create();
    
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyDataAdapter.class);

    public static final String NACL_FAILURE_TOKEN = "NACL_FAILURE";
    
    @Override
    public KeyData unmarshal(final KeyData keyData) {

        //case 1, the keys are provided inline
        if (keyData.hasKeys()) {
            return keyData;
        }

        //case 2, the config is provided inline
        if (keyData.getPublicKeyPath() == null && keyData.getPrivateKeyPath() == null) {
            return unmarshalInline(keyData);
        }


        if (keyData.getPublicKeyPath() == null || keyData.getPrivateKeyPath() == null 
                || filesDelegate.notExists(keyData.getPublicKeyPath()) || filesDelegate.notExists(keyData.getPrivateKeyPath())) {
            return keyData;
        }

        //case 3, the keys are provided inside a file
        return unmarshalFile(
                keyData.getPublicKeyPath(),
                keyData.getPrivateKeyPath(),
                Optional.ofNullable(keyData.getConfig()).map(KeyDataConfig::getPassword).orElse(null)
        );
    }

    private KeyData unmarshalFile(final Path publicKeyPath, final Path privateKeyPath, final String password) {
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
                        publicKeyPath
                )
        );

    }

    private KeyData unmarshalInline(final KeyData keyData) {
        if (keyData.getConfig().getType() == PrivateKeyType.UNLOCKED) {
            return new KeyData(keyData.getConfig(), keyData.getConfig().getValue(), keyData.getPublicKey(), null, null);
        }

        if (keyData.getConfig().getPassword() == null) {
            return keyData;
        }

        final KeyEncryptor kg = KeyEncryptorFactory.create();
        final PrivateKeyData encryptedKey = keyData.getConfig().getPrivateKeyData();

        String decyptedPrivateKey;
        try {
            decyptedPrivateKey = Objects.toString(kg.decryptPrivateKey(encryptedKey));
        } catch (final NaclException ex) {
            LOGGER.debug("Unable to decypt private key : {}", ex.getMessage());
            decyptedPrivateKey = NACL_FAILURE_TOKEN +": " + ex.getMessage();
        }

        //need to decrypt
        return new KeyData(
                keyData.getConfig(),
                decyptedPrivateKey,
                keyData.getPublicKey(),
                keyData.getPrivateKeyPath(),
                keyData.getPublicKeyPath()
        );

    }

    @Override
    public KeyData marshal(final KeyData keyData) {

        if (keyData.getConfig() == null) {
            return keyData;
        }

        if(keyData.getPrivateKeyPath()!=null || keyData.getPublicKeyPath()!=null) {
            return new KeyData(
                null, null, null, keyData.getPrivateKeyPath(), keyData.getPublicKeyPath()
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
                    null,//TODO: 
                    keyData.getPublicKey(),
                    keyData.getPrivateKeyPath(),
                    keyData.getPublicKeyPath()
            );
        }

        return keyData;
    }

    protected void setFilesDelegate(FilesDelegate filesDelegate) {
        this.filesDelegate = filesDelegate;
    }

}
