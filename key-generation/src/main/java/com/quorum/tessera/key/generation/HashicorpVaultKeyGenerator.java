package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.keypairs.HashicorpVaultKeyPair;
import com.quorum.tessera.config.vault.data.HashicorpSetSecretData;
import com.quorum.tessera.config.vault.data.SetSecretData;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.nacl.NaclFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HashicorpVaultKeyGenerator implements KeyGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(HashicorpVaultKeyGenerator.class);

    private final NaclFacade nacl;
    private final KeyVaultService keyVaultService;

    public HashicorpVaultKeyGenerator(final NaclFacade nacl, KeyVaultService keyVaultService) {
        this.nacl = nacl;
        this.keyVaultService = keyVaultService;
    }

    @Override
    public HashicorpVaultKeyPair generate(String filename, ArgonOptions encryptionOptions) {
        Objects.requireNonNull(filename);

        final KeyPair keys = this.nacl.generateNewKeys();

        String pubId = "publicKey";
        String privId = "privateKey";
        Map<String, Object> keyPairData = new HashMap<>();
        keyPairData.put(pubId, keys.getPublicKey().encodeToBase64());
        keyPairData.put(privId, keys.getPrivateKey().encodeToBase64());

        SetSecretData setSecretData = new HashicorpSetSecretData(filename, keyPairData);

        keyVaultService.setSecret(setSecretData);
        LOGGER.debug("Key {} saved to vault with path {} and id {}", keyPairData.get(pubId), filename, pubId);
        LOGGER.info("Key saved to vault with path {} and id {}", filename, pubId);
        LOGGER.debug("Key {} saved to vault with path {} and id {}", keyPairData.get(privId), filename, privId);
        LOGGER.info("Key saved to vault with path {} and id {}", filename, privId);

        return new HashicorpVaultKeyPair(pubId, privId, filename);
    }
}
