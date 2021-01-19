package net.consensys.tessera.migration.data;

import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.PublicKey;
import net.consensys.orion.enclave.EncryptedKey;
import net.consensys.tessera.migration.OrionKeyHelper;
import org.apache.tuweni.crypto.sodium.Box;

import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValidateEventHandler extends AbstractEventHandler {

    private OrionKeyHelper orionKeyHelper;

    private final EncryptedKeyMatcher encryptedKeyMatcher;

    public ValidateEventHandler(OrionKeyHelper orionKeyHelper, Encryptor tesseraEncryptor) {
        this.orionKeyHelper = orionKeyHelper;
        this.encryptedKeyMatcher = new EncryptedKeyMatcher(orionKeyHelper, tesseraEncryptor);
    }

    @Override
    public void onEvent(OrionRecordEvent event) throws Exception {

        List<PublicKey> groupIds =
                event.getRecipientKeyToBoxes().keySet().stream()
                        .map(Base64.getDecoder()::decode)
                        .map(PublicKey::from)
                        .sorted(Comparator.comparing(PublicKey::hashCode))
                        .collect(Collectors.toList());

        List<PublicKey> result =
                encryptedKeyMatcher.match(
                        event.getEncryptedPayload(),
                        groupIds.stream()
                                .sorted(Comparator.comparing(PublicKey::hashCode))
                                .map(PublicKey::encodeToBase64)
                                .collect(Collectors.toList()));

        assert groupIds.equals(result) : "The provided keys and the matched keys are not equal";

        String senderKeyData = Base64.getEncoder().encodeToString(event.getEncryptedPayload().sender().bytesArray());
        Optional<Box.SecretKey> pk = orionKeyHelper.findPrivateKey(event.getEncryptedPayload().sender());
        pk.ifPresent(k -> System.out.println("Sender key is " + Base64.getEncoder().encodeToString(k.bytesArray())));
        if (pk.isEmpty()) {
            System.out.println("No key pair found for sender " + senderKeyData);
        } else {
            System.out.println("Key pair found for sender " + senderKeyData);
        }

        for (Map.Entry<String, EncryptedKey> pair : event.getRecipientKeyToBoxes().entrySet()) {

            Box.PublicKey publicKey =
                    Stream.of(pair)
                            .map(p -> p.getKey())
                            .map(Base64.getDecoder()::decode)
                            .map(Box.PublicKey::fromBytes)
                            .findFirst()
                            .get();

            Box.SecretKey privateKey = orionKeyHelper.findPrivateKey(publicKey).orElseGet(() -> pk.get());
            System.out.println("Found private key: " + privateKey);
        }
    }
}
