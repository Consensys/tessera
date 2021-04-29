package net.consensys.tessera.migration.data;

import com.lmax.disruptor.EventHandler;
import com.quorum.tessera.encryption.PublicKey;
import net.consensys.orion.enclave.EncryptedPayload;
import net.consensys.tessera.migration.OrionKeyHelper;

import java.util.List;
import java.util.stream.Collectors;

public class ValidateWhenSender implements EventHandler<OrionDataEvent> {

    private EncryptedKeyMatcher encryptedKeyMatcher;

    private OrionKeyHelper orionKeyHelper;

    public ValidateWhenSender(EncryptedKeyMatcher encryptedKeyMatcher) {
        this.encryptedKeyMatcher = encryptedKeyMatcher;
    }

    @Override
    public void onEvent(OrionDataEvent event, long sequence, boolean endOfBatch) throws Exception {
        if(event.getPayloadType() != PayloadType.ENCRYPTED_PAYLOAD) {
            return;
        }

        final EncryptedPayload encryptedPayload = event.getEncryptedPayload().get();

        final boolean weAreSender = encryptedKeyMatcher.weAreSender(encryptedPayload);
        if(!weAreSender) {
            return;
        }

        List<String> recipients = event.getRecipientBoxMap().get().keySet()
            .stream()
            .map(PublicKey::encodeToBase64)
            .collect(Collectors.toUnmodifiableList());

        try {
            List<PublicKey> resolved = encryptedKeyMatcher.handleWhenSender(encryptedPayload, recipients);
        } catch (Throwable ex) {
            throw ex;
        }
    }
}
