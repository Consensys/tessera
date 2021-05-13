package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.EventHandler;
import net.consensys.orion.enclave.EncryptedPayload;

public class HydrateEncryptedPayload implements EventHandler<OrionDataEvent> {

  private ObjectMapper jacksonObjectMapper = JacksonObjectMapperFactory.create();

  @Override
  public void onEvent(OrionDataEvent event, long sequence, boolean endOfBatch) throws Exception {
    if (event.getPayloadType() != PayloadType.ENCRYPTED_PAYLOAD) {
      return;
    }

    EncryptedPayload encryptedPayload =
        jacksonObjectMapper.readValue(event.getPayloadData(), EncryptedPayload.class);
    event.setEncryptedPayload(encryptedPayload);
  }
}
