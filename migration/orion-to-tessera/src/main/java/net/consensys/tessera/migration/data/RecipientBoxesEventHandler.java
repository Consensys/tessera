package net.consensys.tessera.migration.data;

import net.consensys.orion.enclave.EncryptedKey;
import net.consensys.orion.enclave.EncryptedPayload;
import net.consensys.orion.enclave.PrivacyGroupPayload;
import net.consensys.tessera.migration.OrionKeyHelper;
import org.apache.tuweni.crypto.sodium.Box;

import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RecipientBoxesEventHandler extends AbstractEventHandler {

    private OrionKeyHelper orionKeyHelper;

    public RecipientBoxesEventHandler(OrionKeyHelper orionKeyHelper) {
        this.orionKeyHelper = orionKeyHelper;
    }

    @Override
    public void onEvent(OrionRecordEvent event) throws Exception {

        EncryptedPayload encryptedPayload = event.getEncryptedPayload();

        final List<String> recipients =
                event.getPrivacyGroupPayload().map(PrivacyGroupPayload::addresses).map(List::of).orElse(List.of());

        List<EncryptedKey> recipientBoxes = List.of(encryptedPayload.encryptedKeys());

        String sender = Base64.getEncoder().encodeToString(encryptedPayload.sender().bytesArray());

        List<String> ourKeys =
                orionKeyHelper.getKeyPairs().stream()
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

        System.out.println(recipientList.size() + " = " + recipientBoxes.size() + ", Sender? " + issender);

        if (recipientList.size() != recipientBoxes.size()) {
            System.err.println("WARN: Not great. "); // Add sender?
            // continue;
            //    return;
        }

        Map<String, EncryptedKey> recipientKeyToBoxes =
                IntStream.range(0, recipientList.size())
                        .boxed()
                        .collect(Collectors.toUnmodifiableMap(i -> recipientList.get(i), i -> recipientBoxes.get(i)));

        event.setRecipientKeyToBoxes(recipientKeyToBoxes);
    }
}
