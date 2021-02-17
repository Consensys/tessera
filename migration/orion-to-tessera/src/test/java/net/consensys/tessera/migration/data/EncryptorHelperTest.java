package net.consensys.tessera.migration.data;

import com.quorum.tessera.encryption.*;
import net.consensys.orion.enclave.EncryptedKey;
import net.consensys.orion.enclave.EncryptedPayload;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Ignore
public class EncryptorHelperTest {

    private EncryptorHelper encryptorHelper;

    private Encryptor encryptor;

    @Before
    public void beforeTest() {
        encryptor = mock(Encryptor.class);
        encryptorHelper = new EncryptorHelper(encryptor);
    }

    @After
    public void afterTest() {
        verifyNoMoreInteractions(encryptor);
    }

    @Test
    public void canDecrypt() {

        byte[] cipherText = "CipherText".getBytes();
        byte[] nonce = "Nonce".getBytes();

        EncryptedPayload encryptedPayload = mock(EncryptedPayload.class);
        when(encryptedPayload.cipherText()).thenReturn(cipherText);
        when(encryptedPayload.nonce()).thenReturn(nonce);

        EncryptedKey encryptedKey = mock(EncryptedKey.class);
        byte[] encryptedKeyData = "encryptedKeyData".getBytes();
        when(encryptedKey.getEncoded()).thenReturn(encryptedKeyData);

        PublicKey publicKey = PublicKey.from("PublicKeyData".getBytes());
        PrivateKey privateKey = PrivateKey.from("PrivateKeyData".getBytes());

        SharedKey sharedKey = mock(SharedKey.class);
        when(encryptor.computeSharedKey(publicKey,privateKey)).thenReturn(sharedKey);
        when(encryptor.openAfterPrecomputation(encryptedKeyData,new Nonce(nonce),sharedKey))
            .thenReturn("decryptedKeyData".getBytes());

        boolean result = encryptorHelper.canDecrypt(encryptedPayload,encryptedKey,publicKey,privateKey);
        assertThat(result).isTrue();

        verify(encryptor).computeSharedKey(publicKey,privateKey);
        verify(encryptor).openAfterPrecomputation(encryptedKeyData,new Nonce(nonce),sharedKey);
        verify(encryptor).openAfterPrecomputation(cipherText,new Nonce(new byte[24]),MasterKey.from("decryptedKeyData".getBytes()));
    }

    @Test
    public void canDecryptJson() {

        byte[] cipherText = "CipherText".getBytes();
        byte[] nonce = "Nonce".getBytes();

        JsonObject encryptedPayload = Json.createObjectBuilder()
            .add("cipherText", Base64.getEncoder().encodeToString(cipherText))
            .add("nonce",new String(nonce)).build();

        byte[] encryptedKeyData = "encryptedKeyData".getBytes();
        JsonObject encryptedKey = Json.createObjectBuilder()
            .add("encoded",Base64.getEncoder().encodeToString(encryptedKeyData))
            .build();

        PublicKey publicKey = PublicKey.from("PublicKeyData".getBytes());
        PrivateKey privateKey = PrivateKey.from("PrivateKeyData".getBytes());

        SharedKey sharedKey = mock(SharedKey.class);
        when(encryptor.computeSharedKey(publicKey,privateKey)).thenReturn(sharedKey);
        when(encryptor.openAfterPrecomputation(encryptedKeyData,new Nonce(nonce),sharedKey))
            .thenReturn("decryptedKeyData".getBytes());

        boolean result = encryptorHelper.canDecrypt(encryptedPayload,encryptedKey,publicKey,privateKey);
        assertThat(result).isTrue();

        verify(encryptor).computeSharedKey(publicKey,privateKey);
        verify(encryptor).openAfterPrecomputation(encryptedKeyData,new Nonce(nonce),sharedKey);
        verify(encryptor).openAfterPrecomputation(cipherText,new Nonce(new byte[24]),MasterKey.from("decryptedKeyData".getBytes()));
    }

}
