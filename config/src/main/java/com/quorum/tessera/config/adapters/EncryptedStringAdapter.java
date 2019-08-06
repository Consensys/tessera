package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.util.ConfigSecretReader;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class EncryptedStringAdapter extends XmlAdapter<String, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedStringAdapter.class);

    private final PBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

    @Override
    public String unmarshal(String textToDecrypt) {

        if (PropertyValueEncryptionUtils.isEncryptedValue(textToDecrypt)) {

            if (!((StandardPBEStringEncryptor) encryptor).isInitialized()) {
                encryptor.setPassword(
                        ConfigSecretReader.readSecretFromFile()
                            .orElseGet(ConfigSecretReader::readSecretFromConsole));
            }

            return PropertyValueEncryptionUtils.decrypt(textToDecrypt, encryptor);
        }

        LOGGER.warn(
                "Some sensitive values are being given as unencrypted plain text in config. "
                        + "Please note this is NOT recommended for production environment.");

        return textToDecrypt;
    }

    @Override
    public String marshal(String text) {
        return text;
    }
}
