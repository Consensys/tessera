package net.consensys.tessera.migration.data;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.lmax.disruptor.EventHandler;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.consensys.orion.enclave.EncryptedPayload;

public class LookupRecipientsFromPrivacyGroup implements EventHandler<OrionDataEvent> {

  private JsonFactory jacksonJsonFactory = JacksonObjectMapperFactory.createFactory();

  private RecipientBoxHelper recipientBoxHelper;

  public LookupRecipientsFromPrivacyGroup(RecipientBoxHelper recipientBoxHelper) {
    this.recipientBoxHelper = recipientBoxHelper;
  }

  @Override
  public void onEvent(OrionDataEvent orionDataEvent, long sequence, boolean endOfBatch)
      throws Exception {

    if (orionDataEvent.getPayloadType() != PayloadType.ENCRYPTED_PAYLOAD) {
      return;
    }

    final byte[] privacyGroupData = orionDataEvent.getPrivacyGroupData();

    if (Objects.isNull(privacyGroupData)) {
      return;
    }

    try (JsonParser jParser = jacksonJsonFactory.createParser(privacyGroupData)) {

      List<String> recipients = new ArrayList<>();
      while (!jParser.isClosed()) {

        JsonToken jsonToken = jParser.nextToken();
        if (JsonToken.FIELD_NAME.equals(jsonToken)) {
          String fieldname = jParser.getCurrentName();
          if (Objects.equals(fieldname, "addresses")) {
            jParser.nextToken();
            while (jParser.nextToken() != JsonToken.END_ARRAY) {
              String address = jParser.getValueAsString();
              recipients.add(address);
            }
          }
        }
      }

      final EncryptedPayload orionEncryptedPayload = orionDataEvent.getEncryptedPayload().get();

      Map<PublicKey, RecipientBox> recipientBoxMap =
          recipientBoxHelper.getRecipientMapping(orionEncryptedPayload, recipients);
      orionDataEvent.setRecipientBoxMap(recipientBoxMap);
    }
  }
}
