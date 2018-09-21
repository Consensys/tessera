package com.quorum.tessera.config.keys;

import com.quorum.tessera.config.*;
import com.quorum.tessera.nacl.KeyPair;
import com.quorum.tessera.nacl.NaclFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.quorum.tessera.config.PrivateKeyType.UNLOCKED;

public class VaultKeyGenerator implements KeyGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(VaultKeyGenerator.class);

    private final NaclFacade nacl;

    public VaultKeyGenerator(final NaclFacade nacl) {
        this.nacl = nacl;
    }

    @Override
    public KeyData generate(String filename, ArgonOptions encryptionOptions, KeyVaultConfig keyVaultConfig) {
        final KeyPair generated = this.nacl.generateNewKeys();
        final Path path = Paths.get(filename);
        final String keyVaultId = path.getFileName().toString();

        saveKeysInVault(generated, keyVaultId, keyVaultConfig);

        final KeyData keyData = new KeyData(
            new KeyDataConfig(
                new PrivateKeyData(generated.getPrivateKey().toString(), null, null, null, null, null),
                UNLOCKED
            ),
            generated.getPrivateKey().toString(),
            generated.getPublicKey().toString(),
            null,
            null,
            keyVaultId
        );

        return keyData;
    }

    private void saveKeysInVault(KeyPair keys, String keyVaultId, KeyVaultConfig keyVaultConfig) {
        String publicId = keyVaultId + "pub";
        String privateId = keyVaultId + "key";
        LOGGER.info("Public key {} saved to vault with id {}", keys.getPublicKey().toString(), publicId);
        LOGGER.info("Private key {} saved to vault with id {}", keys.getPrivateKey().toString(), privateId);
    }
}

