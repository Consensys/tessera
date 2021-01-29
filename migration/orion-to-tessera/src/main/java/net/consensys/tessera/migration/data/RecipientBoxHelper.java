package net.consensys.tessera.migration.data;

import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.PublicKey;
import net.consensys.orion.enclave.EncryptedKey;
import net.consensys.orion.enclave.EncryptedPayload;
import net.consensys.orion.enclave.PrivacyGroupPayload;
import net.consensys.tessera.migration.OrionKeyHelper;
import org.apache.tuweni.crypto.sodium.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RecipientBoxHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecipientBoxHelper.class);

    private final OrionKeyHelper orionKeyHelper;

    private final EncryptedPayload encryptedPayload;

    private final PrivacyGroupPayload privacyGroupPayload;

    public RecipientBoxHelper(OrionKeyHelper orionKeyHelper,
                              EncryptedPayload encryptedPayload,
                              PrivacyGroupPayload privacyGroupPayload) {

        this.orionKeyHelper = Objects.requireNonNull(orionKeyHelper);
        this.encryptedPayload = Objects.requireNonNull(encryptedPayload);
        this.privacyGroupPayload = privacyGroupPayload;
    }

    public Map<PublicKey, RecipientBox> getRecipientMapping() {

        final List<String> recipients =
            Optional.ofNullable(privacyGroupPayload)
                .map(PrivacyGroupPayload::addresses)
                .map(List::of)
                .orElse(List.of());

        List<EncryptedKey> recipientBoxes = List.of(encryptedPayload.encryptedKeys());

        String sender = Optional.of(encryptedPayload)
            .map(EncryptedPayload::sender)
            .map(Box.PublicKey::bytesArray)
            .map(Base64.getEncoder()::encodeToString)
            .orElseThrow(() -> new IllegalStateException("Unable to find sender from payload"));

        List<String> ourKeys =
            orionKeyHelper.getKeyPairs()
                .stream()
                .map(Box.KeyPair::publicKey)
                .map(Box.PublicKey::bytesArray)
                .map(Base64.getEncoder()::encodeToString)
                .collect(Collectors.toList());

        boolean issender = ourKeys.contains(sender);

        List<String> recipientList =
            recipients.stream()
                .filter(r -> issender || ourKeys.contains(r))
                .map(Base64.getDecoder()::decode)
                .map(Box.PublicKey::fromBytes)
                .sorted(Comparator.comparing(Box.PublicKey::hashCode))
                .map(Box.PublicKey::bytesArray)
                .map(Base64.getEncoder()::encodeToString)
                .collect(Collectors.toList());

        LOGGER.info("Recipients {}, Recipient boxes {}, Sender? {}",recipientList.size(),recipientBoxes.size(),issender);

        if (recipientList.size() != recipientBoxes.size()) {
            throw new IllegalStateException("Recipient list and recipient box list aren't same size");
        }

        final Map<PublicKey, RecipientBox> map = new LinkedHashMap<>();
        IntStream.range(0, recipientList.size())
            .boxed().forEach(i -> {
            PublicKey recipientKey = PublicKey.from(Base64.getDecoder().decode(recipientList.get(i)));
            RecipientBox recipientBox = RecipientBox.from(recipientBoxes.get(i).getEncoded());
            map.put(recipientKey,recipientBox);
        });
       return map;

    }
}
