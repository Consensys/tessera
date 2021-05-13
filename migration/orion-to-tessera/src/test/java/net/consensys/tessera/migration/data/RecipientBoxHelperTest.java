package net.consensys.tessera.migration.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.PublicKey;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.consensys.orion.enclave.EncryptedKey;
import net.consensys.orion.enclave.EncryptedPayload;
import net.consensys.orion.enclave.PrivacyGroupPayload;
import net.consensys.tessera.migration.OrionKeyHelper;
import org.apache.tuweni.crypto.sodium.Box;
import org.junit.Test;

public class RecipientBoxHelperTest {

  @Test
  public void resolveRecipientBoxes() {

    OrionKeyHelper orionKeyHelper = mock(OrionKeyHelper.class);
    EncryptedPayload encryptedPayload = mock(EncryptedPayload.class);

    Box.PublicKey sender = mock(Box.PublicKey.class);
    when(sender.bytesArray()).thenReturn("SENDER".getBytes());
    when(encryptedPayload.sender()).thenReturn(sender);

    EncryptedKey[] encryptedKeys =
        Stream.of("ONE", "TWO", "THREE")
            .map(String::getBytes)
            .map(EncryptedKey::new)
            .toArray(EncryptedKey[]::new);

    when(encryptedPayload.encryptedKeys()).thenReturn(encryptedKeys);

    PrivacyGroupPayload privacyGroupPayload = mock(PrivacyGroupPayload.class);

    SecureRandom random = new SecureRandom();

    List<String> keys =
        IntStream.range(0, 3)
            .mapToObj(
                i -> {
                  byte[] keyData = new byte[32];
                  random.nextBytes(keyData);
                  return keyData;
                })
            .map(Box.PublicKey::fromBytes)
            .sorted(Comparator.comparing(Box.PublicKey::hashCode))
            .map(Box.PublicKey::bytesArray)
            .map(Base64.getEncoder()::encodeToString)
            .collect(Collectors.toList());

    String[] addresses = keys.toArray(String[]::new);

    when(privacyGroupPayload.addresses()).thenReturn(addresses);

    Box.SecretKey privateKey = mock(Box.SecretKey.class);
    List<Box.KeyPair> pairs =
        Arrays.stream(addresses)
            .map(Base64.getDecoder()::decode)
            .map(Box.PublicKey::fromBytes)
            .map(p -> new Box.KeyPair(p, privateKey))
            .collect(Collectors.toList());

    when(orionKeyHelper.getKeyPairs()).thenReturn(pairs);

    RecipientBoxHelper recipientBoxHelper = new RecipientBoxHelper(orionKeyHelper);

    Map<PublicKey, RecipientBox> results =
        recipientBoxHelper.getRecipientMapping(encryptedPayload, privacyGroupPayload);

    assertThat(results).hasSize(3);

    List<String> encodedKeys =
        results.keySet().stream().map(PublicKey::encodeToBase64).collect(Collectors.toList());

    assertThat(encodedKeys).isEqualTo(keys);

    List<String> values =
        results.values().stream()
            .map(RecipientBox::getData)
            .map(String::new)
            .collect(Collectors.toList());

    assertThat(values).containsExactly("ONE", "TWO", "THREE");
  }
}
