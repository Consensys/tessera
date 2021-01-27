package net.consensys.tessera.migration.data;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class PersistTransactionEventHandlerTest {

    private PersistTransactionEventHandler persistTransactionEventHandler;

    private EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;

    @Before
    public void beforeTest() {
        entityManagerFactory = mock(EntityManagerFactory.class);
        entityManager = mock(EntityManager.class);
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        persistTransactionEventHandler = new PersistTransactionEventHandler(entityManagerFactory);
    }

    @After
    public void afterTest() {
        verifyNoMoreInteractions(entityManagerFactory);
        verifyNoMoreInteractions(entityManager);
    }

    @Test
    public void handle() throws Exception {

        byte[] id = "SomeId".getBytes();

        OrionEvent orionEvent = mock(OrionEvent.class);
        when(orionEvent.getKey()).thenReturn(id);
        when(orionEvent.getPayloadType()).thenReturn(PayloadType.ENCRYPTED_PAYLOAD);

        JsonObject jsonObject = Json.createObjectBuilder()
            .add("sender","HEkOUBXbgGCQ5+WDFUAhucXm/n5zUrfGkgdJY/5lfCs=")
            .add("nonce","NRY4WRnhNWbEm3EZgJPu4hbr+11ABxOE")
            .add("encryptedKeys",Json.createArrayBuilder()
                .add("9sN4Qo5ieGXPhrCJBYdMNl1yv63Z0SIchBqvtOgY1nY2CTFSZoggr82af2W1NBtA"))
            .add("cipherText","OSjQw8USGgPJN+4py3/a9zqLRIrXt7fZmzAv4Ve0NrqbjoISL++S/ucBLH+L1VFPL3RK0SZ6kX1dcgrjVN4Fg2rjRVOSvRWz1A9jkJHQuCDo6DbgM+6aDjFuvxHlkhrO4opOjXQoj6FSFzK0Sz6Aj+E97E+Fa3VstNvkrsBZS2Nc67QtFkVQw5WAJlknm4QKUerWGF8gZ1bBnzxmes/UkTsXhW/+Kf7UpW4zY61qVcNvZJ8GOlvNG37hs4HLD8L32xlRBpBzPzeYPPvMS+XFgQ5e6V9Y8kcTmfyEd4LyshtEM22gT57NgpevT3gwsIfkG0vMUUsurpv9ToJU0SgBpmXZGCs=")
            .add("privacyGroupId","VTTzv+L6f8ULWgJAO8iVX52WWxIygIt8QUszBIt5RT4=")
            .build();

        when(orionEvent.getJsonObject()).thenReturn(jsonObject);

        RecipientBox recipientBox = RecipientBox.from("9sN4Qo5ieGXPhrCJBYdMNl1yv63Z0SIchBqvtOgY1nY2CTFSZoggr82af2W1NBtA".getBytes());

        PublicKey recipientKey = Optional.of("arhIcNa+MuYXZabmzJD5B33F3dZgqb0hEbM3FZsylSg=")
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from).get();

        Map<PublicKey, RecipientBox> recipientBoxMap = Map.of(recipientKey,recipientBox);
        when(orionEvent.getRecipientBoxMap()).thenReturn(Optional.of(recipientBoxMap));

        ArgumentCaptor<EncryptedTransaction> persistCapture = ArgumentCaptor.forClass(EncryptedTransaction.class);

        persistTransactionEventHandler.onEvent(orionEvent);

        verify(entityManagerFactory).createEntityManager();
        verify(entityManager).persist(persistCapture.capture());

        EncryptedTransaction encryptedTransaction = persistCapture.getValue();

        assertThat(encryptedTransaction).isNotNull();

        assertThat(encryptedTransaction.getHash()).isNotNull();
        assertThat(encryptedTransaction.getEncodedPayload()).isNotNull();


        EncodedPayload encodedPayload = PayloadEncoder.create().decode(encryptedTransaction.getEncodedPayload());
        assertThat(encodedPayload.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
        assertThat(encodedPayload.getSenderKey().encodeToBase64())
            .isEqualTo(jsonObject.getString("sender"));
        assertThat(encodedPayload.getCipherText())
            .isEqualTo(Base64.getDecoder().decode(jsonObject.getString("cipherText")));
        assertThat(encodedPayload.getPrivacyGroupId()).isPresent();

        assertThat(encodedPayload.getPrivacyGroupId().map(PublicKey::encodeToBase64).get())
            .isEqualTo(jsonObject.getString("privacyGroupId"));

        assertThat(encodedPayload.getRecipientNonce()).isNotNull();
        assertThat(encodedPayload.getRecipientNonce().getNonceBytes())
            .isEqualTo(Base64.getDecoder().decode(jsonObject.getString("nonce")));

        assertThat(encodedPayload.getCipherTextNonce())
            .isEqualTo(new Nonce(new byte[24]));

        assertThat(encodedPayload.getRecipientKeys()).containsExactly(recipientKey);
        assertThat(encodedPayload.getRecipientBoxes()).containsExactly(recipientBox);

    }

}
