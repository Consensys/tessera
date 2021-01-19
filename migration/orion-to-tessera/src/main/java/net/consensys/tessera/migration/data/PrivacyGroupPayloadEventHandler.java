package net.consensys.tessera.migration.data;

import net.consensys.orion.enclave.PrivacyGroupPayload;

import java.util.Optional;

public class PrivacyGroupPayloadEventHandler extends AbstractEventHandler {

    private PrivacyGroupPayloadLookup privacyGroupPayloadLookup;

    public PrivacyGroupPayloadEventHandler(PrivacyGroupPayloadLookup privacyGroupPayloadLookup) {
        this.privacyGroupPayloadLookup = privacyGroupPayloadLookup;
    }

    @Override
    public void onEvent(OrionRecordEvent event) throws Exception {

        Optional<PrivacyGroupPayload> privacyGroupPayload =
                privacyGroupPayloadLookup.findRecipients(event.getEncryptedPayload());
        if (privacyGroupPayload.isEmpty()) {
            event.error();
            return;
        }

        privacyGroupPayload.ifPresent(event::setPrivacyGroupPayload);
    }
}
