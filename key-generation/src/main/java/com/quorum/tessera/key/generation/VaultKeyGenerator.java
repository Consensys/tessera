package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.*;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.nacl.Key;
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

        final StringBuilder publicId = new StringBuilder();
        final StringBuilder privateId = new StringBuilder();

        if(filename != null) {
            final Path path = Paths.get(filename);
            final String keyVaultId = path.getFileName().toString();

            if(!keyVaultId.matches("^[0-9a-zA-Z\\-]*$")) {
                throw new RuntimeException("Generated key ID for Azure Key Vault can contain only 0-9, a-z, A-Z and - characters");
            }

            if(keyVaultId != null) {
                publicId.append(keyVaultId);
                privateId.append(keyVaultId);
            }
        }

        publicId.append("Pub");
        privateId.append("Key");

        saveKeyInVault(publicId.toString(), keys.getPublicKey());
        saveKeyInVault(privateId.toString(), keys.getPrivateKey());

        final KeyData keyData = new KeyData(
            new KeyDataConfig(
                new PrivateKeyData(keys.getPrivateKey().toString(), null, null, null, null, null),
                PrivateKeyType.UNLOCKED
            ),
            keys.getPrivateKey().toString(),
            keys.getPublicKey().toString(),
            null,
            null,
            publicId.toString(),
            privateId.toString()
        );

        return keyData;
    }

    private void saveKeyInVault(String id, Key key) {
        keyVaultService.setSecret(id, key.toString());
        LOGGER.debug("Key {} saved to vault with id {}", key.toString(), id);
        LOGGER.info("Key saved to vault with id {}", id);
    }
}
