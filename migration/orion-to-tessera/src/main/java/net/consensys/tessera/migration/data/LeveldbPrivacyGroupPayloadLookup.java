package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.consensys.orion.enclave.EncryptedPayload;
import net.consensys.orion.enclave.PrivacyGroupPayload;
import org.iq80.leveldb.DB;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.Optional;

public class LeveldbPrivacyGroupPayloadLookup implements PrivacyGroupPayloadLookup {

    private final DB leveldb;

    private final ObjectMapper objectMapper;

    public LeveldbPrivacyGroupPayloadLookup(DB leveldb, ObjectMapper objectMapper) {
        this.leveldb = leveldb;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<PrivacyGroupPayload> findRecipients(EncryptedPayload encryptedPayload) {
        byte[] privacyGroupId = encryptedPayload.privacyGroupId();
        byte[] privacyGroupPayloadData = leveldb.get(Base64.getEncoder().encode(privacyGroupId));
        return Optional.ofNullable(privacyGroupPayloadData)
                .map(
                        data -> {
                            try {
                                return objectMapper.readValue(data, PrivacyGroupPayload.class);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
    }
}
