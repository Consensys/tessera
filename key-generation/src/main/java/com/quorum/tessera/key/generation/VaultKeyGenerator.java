package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.*;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.nacl.KeyPair;
import com.quorum.tessera.nacl.NaclFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class VaultKeyGenerator implements KeyGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(VaultKeyGenerator.class);

    private final NaclFacade nacl;
    private final KeyVaultService keyVaultService;

    public VaultKeyGenerator(final NaclFacade nacl, KeyVaultService keyVaultService) {
        this.nacl = nacl;
        this.keyVaultService = keyVaultService;
    }

    @Override
    public KeyData generate(String filename, ArgonOptions encryptionOptions) {
        final KeyPair keys = this.nacl.generateNewKeys();

        String keyVaultId = null;

        if(filename != null) {
            final Path path = Paths.get(filename);
            keyVaultId = path.getFileName().toString();

            if(!keyVaultId.matches("^[0-9a-zA-Z\\-]*$")) {
                throw new RuntimeException("Generated key ID for Azure Key Vault can contain only 0-9, a-z, A-Z and - characters");
            }
        }

        saveKeysInVault(keys, keyVaultId);

        final KeyData keyData = new KeyData(
            new KeyDataConfig(
                new PrivateKeyData(keys.getPrivateKey().toString(), null, null, null, null, null),
                PrivateKeyType.UNLOCKED
            ),
            keys.getPrivateKey().toString(),
            keys.getPublicKey().toString(),
            null,
            null,
            keyVaultId
        );

        return keyData;
    }

    private void saveKeysInVault(KeyPair keys, String keyVaultId) {
        StringBuilder publicId = new StringBuilder();
        StringBuilder privateId = new StringBuilder();

        if(keyVaultId != null) {
            publicId.append(keyVaultId);
            privateId.append(keyVaultId);
        }

        publicId.append("Pub");
        privateId.append("Key");

        keyVaultService.setSecret(publicId.toString(), keys.getPublicKey().toString());
        LOGGER.debug("Public key {} saved to vault with id {}", keys.getPublicKey().toString(), publicId);
        LOGGER.info("Public key saved to vault with id {}", publicId);

        keyVaultService.setSecret(privateId.toString(), keys.getPrivateKey().toString());
        LOGGER.debug("Private key {} saved to vault with id {}", keys.getPrivateKey().toString(), privateId);
        LOGGER.info("Private key saved to vault with id {}", privateId);

    }
}
