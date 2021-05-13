package com.quorum.tessera.config.keypairs;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.PrivateKeyType;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.PrivateKey;
import org.junit.Before;
import org.junit.Test;

public class InlineKeypairTest {

  private KeyEncryptor keyEncryptor;

  @Before
  public void onSetup() {
    keyEncryptor = mock(KeyEncryptor.class);
  }

  public void onTearDown() {
    verifyNoMoreInteractions(keyEncryptor);
  }

  @Test
  public void unlockedKeyGetsValue() {

    PrivateKeyData privateKeyData = mock(PrivateKeyData.class);
    final KeyDataConfig privKeyDataConfig = mock(KeyDataConfig.class);
    when(privKeyDataConfig.getPrivateKeyData()).thenReturn(privateKeyData);
    when(privKeyDataConfig.getType()).thenReturn(PrivateKeyType.UNLOCKED);

    String value = "I love sparrows";
    when(privKeyDataConfig.getValue()).thenReturn(value);

    final InlineKeypair result = new InlineKeypair("public", privKeyDataConfig, keyEncryptor);

    assertThat(result.getPrivateKey()).isEqualTo(value);

    verifyZeroInteractions(keyEncryptor);
  }

  @Test
  public void nullPasswordGivesNullKey() {
    PrivateKeyData privateKeyData = mock(PrivateKeyData.class);
    final KeyDataConfig privKeyDataConfig = mock(KeyDataConfig.class);
    when(privKeyDataConfig.getPrivateKeyData()).thenReturn(privateKeyData);
    when(privKeyDataConfig.getType()).thenReturn(PrivateKeyType.LOCKED);

    final InlineKeypair result = new InlineKeypair("public", privKeyDataConfig, keyEncryptor);
    result.withPassword(null);

    assertThat(result.getPrivateKey()).isNull();
    verifyZeroInteractions(keyEncryptor);
  }

  @Test
  public void updatingPasswordsAttemptsToDecryptAgain() {

    PrivateKeyData privateKeyData = mock(PrivateKeyData.class);
    final KeyDataConfig privKeyDataConfig = mock(KeyDataConfig.class);
    when(privKeyDataConfig.getPrivateKeyData()).thenReturn(privateKeyData);
    when(privKeyDataConfig.getType()).thenReturn(PrivateKeyType.LOCKED);

    when(keyEncryptor.decryptPrivateKey(privateKeyData, "wrong-password".toCharArray()))
        .thenThrow(new EncryptorException("WHAT YOU TALKING ABOUT WILLIS"));

    final InlineKeypair inlineKeypair =
        new InlineKeypair("public", privKeyDataConfig, keyEncryptor);
    inlineKeypair.withPassword("wrong-password".toCharArray());

    String result = inlineKeypair.getPrivateKey();

    assertThat(result).isEqualTo("NACL_FAILURE");

    // change password and attempt again
    inlineKeypair.withPassword("testpassword".toCharArray());

    PrivateKey privateKey = mock(PrivateKey.class);
    when(privateKey.encodeToBase64()).thenReturn("SUCCESS");
    when(keyEncryptor.decryptPrivateKey(privateKeyData, "testpassword".toCharArray()))
        .thenReturn(privateKey);

    assertThat(inlineKeypair.getPrivateKey()).isEqualTo("SUCCESS");

    verify(keyEncryptor).decryptPrivateKey(privateKeyData, "wrong-password".toCharArray());
    verify(keyEncryptor).decryptPrivateKey(privateKeyData, "testpassword".toCharArray());
  }

  @Test
  public void incorrectPasswordGetsCorrectFailureToken() {
    PrivateKeyData privateKeyData = mock(PrivateKeyData.class);
    final KeyDataConfig privKeyDataConfig = mock(KeyDataConfig.class);
    when(privKeyDataConfig.getPrivateKeyData()).thenReturn(privateKeyData);
    when(privKeyDataConfig.getType()).thenReturn(PrivateKeyType.LOCKED);

    when(keyEncryptor.decryptPrivateKey(privateKeyData, "wrong-password".toCharArray()))
        .thenThrow(new EncryptorException("WHAT YOU TALKING ABOUT WILLIS"));

    final InlineKeypair inlineKeypair =
        new InlineKeypair("public", privKeyDataConfig, keyEncryptor);
    inlineKeypair.withPassword("wrong-password".toCharArray());

    String result = inlineKeypair.getPrivateKey();
    assertThat(String.valueOf(inlineKeypair.getPassword())).isEqualTo("wrong-password");
    assertThat(result).isEqualTo("NACL_FAILURE");
  }

  @Test
  public void correctPasswordGetsCorrectKey() {

    PrivateKeyData privateKeyData = mock(PrivateKeyData.class);

    final KeyDataConfig privKeyDataConfig = mock(KeyDataConfig.class);
    when(privKeyDataConfig.getPrivateKeyData()).thenReturn(privateKeyData);
    when(privKeyDataConfig.getType()).thenReturn(PrivateKeyType.LOCKED);

    char[] validPassword = "testpassword".toCharArray();

    PrivateKey privateKey = mock(PrivateKey.class);
    when(privateKey.encodeToBase64()).thenReturn("SUCCESS");

    when(keyEncryptor.decryptPrivateKey(privateKeyData, validPassword)).thenReturn(privateKey);

    final InlineKeypair result = new InlineKeypair("public", privKeyDataConfig, keyEncryptor);
    result.withPassword(validPassword);

    assertThat(result.getPrivateKey()).isEqualTo("SUCCESS");
  }
}
