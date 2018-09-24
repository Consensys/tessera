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
        final Path path = Paths.get(filename);
        final String keyVaultId = path.getFileName().toString();

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
        String publicId = keyVaultId + "pub";
        String privateId = keyVaultId + "key";

        keyVaultService.setSecret(publicId, keys.getPublicKey().toString());
        LOGGER.debug("Public key {} saved to vault with id {}", keys.getPublicKey().toString(), publicId);
        LOGGER.info("Public key saved to vault with id {}", publicId);

        keyVaultService.setSecret(privateId, keys.getPrivateKey().toString());
        LOGGER.debug("Private key {} saved to vault with id {}", keys.getPrivateKey().toString(), privateId);
        LOGGER.info("Private key saved to vault with id {}", privateId);

    }
}
