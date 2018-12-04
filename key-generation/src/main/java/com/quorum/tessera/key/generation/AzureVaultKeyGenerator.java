package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.config.keypairs.AzureVaultKeyPair;
import com.quorum.tessera.encryption.Key;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.SetSecretData;
import com.quorum.tessera.key.vault.SetSecretDataFactory;
import com.quorum.tessera.nacl.NaclFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class AzureVaultKeyGenerator implements KeyGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureVaultKeyGenerator.class);

    private final NaclFacade nacl;
    private final KeyVaultService keyVaultService;

    public AzureVaultKeyGenerator(final NaclFacade nacl, KeyVaultService keyVaultService) {
        this.nacl = nacl;
        this.keyVaultService = keyVaultService;
    }

    @Override
    public AzureVaultKeyPair generate(String filename, ArgonOptions encryptionOptions) {
        final KeyPair keys = this.nacl.generateNewKeys();

        final StringBuilder publicId = new StringBuilder();
        final StringBuilder privateId = new StringBuilder();

        if(filename != null) {
            final Path path = Paths.get(filename);
            final String keyVaultId = path.getFileName().toString();

            if(!keyVaultId.matches("^[0-9a-zA-Z\\-]*$")) {
                throw new UnsupportedCharsetException("Generated key ID for Azure Key Vault can contain only 0-9, a-z, A-Z and - characters");
            }

            publicId.append(keyVaultId);
            privateId.append(keyVaultId);

        }

        publicId.append("Pub");
        privateId.append("Key");

        saveKeyInVault(publicId.toString(), keys.getPublicKey());
        saveKeyInVault(privateId.toString(), keys.getPrivateKey());

        return new AzureVaultKeyPair(publicId.toString(), privateId.toString());
    }

    private void saveKeyInVault(String id, Key key) {
        SetSecretDataFactory setSecretDataFactory = SetSecretDataFactory.getInstance(KeyVaultType.AZURE);

        Map<String, Object> data = new HashMap<>();
        data.put("secretName", id);
        data.put("secret", key.encodeToBase64());

        SetSecretData setSecretData = setSecretDataFactory.create(data);

        keyVaultService.setSecret(setSecretData);
        LOGGER.debug("Key {} saved to vault with id {}", key.encodeToBase64(), id);
        LOGGER.info("Key saved to vault with id {}", id);
    }
}
