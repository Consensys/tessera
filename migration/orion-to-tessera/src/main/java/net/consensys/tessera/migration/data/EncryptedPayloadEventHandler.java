package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.consensys.orion.enclave.EncryptedPayload;

public class EncryptedPayloadEventHandler extends AbstractEventHandler {

    private ObjectMapper objectMapper;

    public EncryptedPayloadEventHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void onEvent(OrionRecordEvent orionRecordEvent) throws Exception {
        EncryptedPayload encryptedPayload = objectMapper.readValue(orionRecordEvent.getValue(), EncryptedPayload.class);
        orionRecordEvent.setEncryptedPayload(encryptedPayload);
    }
}
