package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.keypairs.*;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import java.util.*;
import org.junit.Before;
import org.junit.Test;

public class KeyPairConverterTest {

  private KeyPairConverter converter;
  private Config config;
  private EnvironmentVariableProvider envProvider;

  @Before
  public void setUp() {
    this.config = mock(Config.class);
    this.envProvider = mock(EnvironmentVariableProvider.class);
    this.converter = new KeyPairConverter(config, envProvider);
  }

  private byte[] decodeBase64(String input) {
    return Base64.getDecoder().decode(input);
  }

  @Test
  public void convertSingleDirectKeyPair() {
    final DirectKeyPair keyPair = new DirectKeyPair("public", "private");
    Collection<KeyPair> result = converter.convert(Collections.singletonList(keyPair));

    assertThat(result).hasSize(1);

    KeyPair expected =
        new KeyPair(
            PublicKey.from(decodeBase64("public")), PrivateKey.from(decodeBase64("private")));
    KeyPair resultKeyPair = result.iterator().next();

    assertThat(resultKeyPair).isEqualToComparingFieldByField(expected);
  }

  @Test
  public void convertSingleFilesystemKeyPair() {
    final FilesystemKeyPair keyPair = mock(FilesystemKeyPair.class);
    when(keyPair.getPublicKey()).thenReturn("public");
    when(keyPair.getPrivateKey()).thenReturn("private");

    Collection<KeyPair> result = converter.convert(Collections.singletonList(keyPair));

    assertThat(result).hasSize(1);

    KeyPair expected =
        new KeyPair(
            PublicKey.from(decodeBase64("public")), PrivateKey.from(decodeBase64("private")));
    KeyPair resultKeyPair = result.iterator().next();

    assertThat(resultKeyPair).isEqualToComparingFieldByField(expected);
  }

  @Test
  public void convertSingleInlineKeyPair() {
    final InlineKeypair keyPair = mock(InlineKeypair.class);
    when(keyPair.getPublicKey()).thenReturn("public");
    when(keyPair.getPrivateKey()).thenReturn("private");

    Collection<KeyPair> result = converter.convert(Collections.singletonList(keyPair));

    assertThat(result).hasSize(1);

    KeyPair expected =
        new KeyPair(
            PublicKey.from(decodeBase64("public")), PrivateKey.from(decodeBase64("private")));
    KeyPair resultKeyPair = result.iterator().next();

    assertThat(resultKeyPair).isEqualToComparingFieldByField(expected);
  }

  @Test
  // Uses com.quorum.tessera.keypairconverter.MockAzureKeyVaultServiceFactory
  public void convertSingleAzureVaultKeyPair() {
    final AzureVaultKeyPair keyPair = new AzureVaultKeyPair("pub", "priv", null, null);

    Collection<KeyPair> result = converter.convert(Collections.singletonList(keyPair));

    assertThat(result).hasSize(1);

    KeyPair resultKeyPair = result.iterator().next();
    KeyPair expected =
        new KeyPair(
            PublicKey.from(decodeBase64("publicSecret")),
            PrivateKey.from(decodeBase64("privSecret")));

    assertThat(resultKeyPair).isEqualToComparingFieldByField(expected);
  }

  @Test
  // Uses com.quorum.tessera.keypairconverter.MockAwsKeyVaultServiceFactory
  public void convertSingleAwsVaultKeyPair() {
    final AWSKeyPair keyPair = new AWSKeyPair("pub", "priv");

    Collection<KeyPair> result = converter.convert(Collections.singletonList(keyPair));

    assertThat(result).hasSize(1);

    KeyPair resultKeyPair = result.iterator().next();
    KeyPair expected =
        new KeyPair(
            PublicKey.from(decodeBase64("publicSecret")),
            PrivateKey.from(decodeBase64("privSecret")));

    assertThat(resultKeyPair).isEqualToComparingFieldByField(expected);
  }

  @Test
  public void convertSingleHashicorpVaultKeyPair() {
    final HashicorpVaultKeyPair keyPair =
        new HashicorpVaultKeyPair("pub", "priv", "engine", "secretName", 10);

    Collection<KeyPair> result = converter.convert(Collections.singletonList(keyPair));

    assertThat(result).hasSize(1);

    KeyPair resultKeyPair = result.iterator().next();
    KeyPair expected =
        new KeyPair(
            PublicKey.from(decodeBase64("publicSecret")),
            PrivateKey.from(decodeBase64("privSecret")));

    assertThat(resultKeyPair).isEqualToComparingFieldByField(expected);
  }

  @Test
  public void convertMultipleKeyPairs() {
    final String pubA = "publicA";
    final String privA = "privateA";
    final String pubB = "publicB";
    final String privB = "privateB";

    final DirectKeyPair keyPairA = new DirectKeyPair(pubA, privA);
    final DirectKeyPair keyPairB = new DirectKeyPair(pubB, privB);

    final Collection<KeyPair> result = converter.convert(Arrays.asList(keyPairA, keyPairB));

    assertThat(result).hasSize(2);

    final KeyPair expectedA =
        new KeyPair(PublicKey.from(decodeBase64(pubA)), PrivateKey.from(decodeBase64(privA)));
    final KeyPair expectedB =
        new KeyPair(PublicKey.from(decodeBase64(pubB)), PrivateKey.from(decodeBase64(privB)));

    final Iterator<KeyPair> it = result.iterator();

    final KeyPair resultA = it.next();
    assertThat(resultA).isEqualToComparingFieldByField(expectedA);

    final KeyPair resultB = it.next();
    assertThat(resultB).isEqualToComparingFieldByField(expectedB);
  }

  @Test
  public void convertKeyPairWithNewlineOrSpace() {
    final DirectKeyPair keyPair =
        new DirectKeyPair(
            "gybY1t9GOYiuN6QgwcrvM2+pQzIu4UrHwiMG7yKQnTg=\n",
            "   yTjqANGAvPCy8AfhcIZ+e4O8CHZPbHkIeOmae5W3srY=\n\n\n");

    Collection<KeyPair> result = converter.convert(Collections.singletonList(keyPair));

    KeyPair expected =
        new KeyPair(
            PublicKey.from(decodeBase64("gybY1t9GOYiuN6QgwcrvM2+pQzIu4UrHwiMG7yKQnTg=")),
            PrivateKey.from(decodeBase64("yTjqANGAvPCy8AfhcIZ+e4O8CHZPbHkIeOmae5W3srY=")));

    KeyPair resultKeyPair = result.iterator().next();
    assertThat(resultKeyPair).isEqualToComparingFieldByField(expected);
  }

  @Test
  public void createFromStrings() {
    final List<String> inputs = Arrays.asList("KEYONE", "KEYTWO");

    final PublicKey[] expectedOutputs =
        new PublicKey[] {
          PublicKey.from(new byte[] {40, 70, 14, 52}), PublicKey.from(new byte[] {40, 70, 19, 88})
        };

    final List<PublicKey> keys = this.converter.convert(inputs);

    assertThat(keys).hasSize(2).containsExactlyInAnyOrder(expectedOutputs);
  }
}
