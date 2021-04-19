package net.consensys.tessera.migration.data;

import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import net.consensys.orion.enclave.EncryptedKey;
import net.consensys.orion.enclave.EncryptedPayload;
import org.apache.tuweni.crypto.sodium.Box;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class EncryptedKeyMatcherTest {

    private EncryptedKeyMatcher encryptedKeyMatcher;

    private List<Box.KeyPair> keyPairs;

    private EncryptorHelper encryptor;

    @Before
    public void beforeTest() {
        keyPairs = new ArrayList<>();
        encryptor = mock(EncryptorHelper.class);
        encryptedKeyMatcher = new EncryptedKeyMatcher(keyPairs,encryptor);
    }

    @After
    public void afterTest() {
        verifyNoMoreInteractions(encryptor);
    }

    @Test
    public void findRecipientKeyWhenNotSenderAndPrivacyGroupNotFound() {

        Box.KeyPair configuredKeyPair = mock(Box.KeyPair.class);
        Box.PublicKey publicKey = mock(Box.PublicKey.class);
        when(publicKey.bytesArray()).thenReturn("PublicKeyBytes".getBytes());

        Box.SecretKey secretKey = mock(Box.SecretKey.class);
        when(secretKey.bytesArray()).thenReturn("SecretKeyBytes".getBytes());

        when(configuredKeyPair.publicKey()).thenReturn(publicKey);
        when(configuredKeyPair.secretKey()).thenReturn(secretKey);

        keyPairs.addAll(List.of(configuredKeyPair));

        EncryptedPayload encryptedPayload = mock(EncryptedPayload.class);

        Box.PublicKey senderKey = mock(Box.PublicKey.class);
        when(senderKey.bytesArray()).thenReturn("SenderKeyData".getBytes());

        when(encryptedPayload.sender()).thenReturn(senderKey);
        EncryptedKey encryptedKey = mock(EncryptedKey.class);
        when(encryptedKey.getEncoded()).thenReturn("EncryptedKeyBytes".getBytes());
        when(encryptedPayload.encryptedKeys()).thenReturn(new EncryptedKey[] {encryptedKey});

        when(encryptor.canDecrypt(same(encryptedPayload),same(encryptedKey),any(PublicKey.class),any(PrivateKey.class))).thenReturn(true);

        Optional<PublicKey> result = encryptedKeyMatcher.findRecipientKeyWhenNotSenderAndPrivacyGroupNotFound(encryptedPayload);

        assertThat(result).isPresent()
            .contains(PublicKey.from("PublicKeyBytes".getBytes()));

        verify(encryptor).canDecrypt(same(encryptedPayload),same(encryptedKey),any(PublicKey.class),any(PrivateKey.class));
    }



}
