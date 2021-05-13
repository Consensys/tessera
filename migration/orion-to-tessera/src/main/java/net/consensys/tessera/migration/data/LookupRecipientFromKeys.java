package net.consensys.tessera.migration.data;

import com.lmax.disruptor.EventHandler;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import net.consensys.orion.enclave.EncryptedKey;
import net.consensys.orion.enclave.EncryptedPayload;

public class LookupRecipientFromKeys implements EventHandler<OrionDataEvent> {

  private EncryptedKeyMatcher encryptedKeyMatcher;

  public LookupRecipientFromKeys(EncryptedKeyMatcher encryptedKeyMatcher) {
    this.encryptedKeyMatcher = encryptedKeyMatcher;
  }

  @Override
  public void onEvent(OrionDataEvent orionDataEvent, long sequence, boolean endOfBatch)
      throws Exception {

    if (orionDataEvent.getPayloadType() != PayloadType.ENCRYPTED_PAYLOAD) {
      return;
    }

    final byte[] privacyGroupData = orionDataEvent.getPrivacyGroupData();
    if (Objects.nonNull(privacyGroupData)) {
      return;
    }

    final EncryptedPayload orionEncryptedPayload = orionDataEvent.getEncryptedPayload().get();
    if (orionEncryptedPayload.encryptedKeys().length != 1) {
      return;
    }

    final PublicKey recipientKey =
        encryptedKeyMatcher
            .findRecipientKeyWhenNotSenderAndPrivacyGroupNotFound(orionEncryptedPayload)
            .get();

    final RecipientBox recipientBox =
        Arrays.stream(orionEncryptedPayload.encryptedKeys())
            .findFirst()
            .map(EncryptedKey::getEncoded)
            .map(RecipientBox::from)
            .get();

    orionDataEvent.setRecipientBoxMap(Map.of(recipientKey, recipientBox));
  }
}
