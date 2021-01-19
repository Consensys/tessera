package net.consensys.tessera.migration.data;

import net.consensys.orion.enclave.EncryptedPayload;
import net.consensys.orion.enclave.PrivacyGroupPayload;

import java.util.Optional;

public interface PrivacyGroupPayloadLookup {

    Optional<PrivacyGroupPayload> findRecipients(EncryptedPayload encryptedPayload);
}
