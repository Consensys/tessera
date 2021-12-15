package com.quorum.tessera.data.staging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.ClientMode;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import java.util.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.MockedStatic;

@RunWith(Parameterized.class)
public class StagingTransactionUtilsTest {

  private final PublicKey sender = PublicKey.from("sender".getBytes());

  private final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());

  private final PayloadEncoder encoder = PayloadEncoder.create(EncodedPayloadCodec.current());

  private ClientMode clientMode;

  private MockedStatic<ConfigFactory> configFactoryMockedStatic;

  private ConfigFactory configFactory;

  private PayloadDigest payloadDigest;

  private static final Map<ClientMode, Class<? extends PayloadDigest>> DIGEST_LOOKUP =
      Map.of(
          ClientMode.ORION, SHA512256PayloadDigest.class,
          ClientMode.TESSERA, DefaultPayloadDigest.class);

  public StagingTransactionUtilsTest(ClientMode clientMode) {
    this.clientMode = clientMode;
  }

  @Before
  public void beforeTest() {

    Config config = mock(Config.class);
    when(config.getClientMode()).thenReturn(clientMode);
    configFactory = mock(ConfigFactory.class);
    when(configFactory.getConfig()).thenReturn(config);

    configFactoryMockedStatic = mockStatic(ConfigFactory.class);
    configFactoryMockedStatic.when(ConfigFactory::create).thenReturn(configFactory);
    payloadDigest = PayloadDigest.create();

    assertThat(payloadDigest).isExactlyInstanceOf(DIGEST_LOOKUP.get(clientMode));
  }

  @After
  public void afterTest() {
    configFactoryMockedStatic.close();
  }

  @Test
  public void testFromRawPayload() {

    final TxHash affectedHash = new TxHash("TX2");

    final EncodedPayload encodedPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(sender)
            .withCipherText("cipherText".getBytes())
            .withCipherTextNonce(new Nonce("nonce".getBytes()))
            .withRecipientBoxes(List.of("box1".getBytes(), "box2".getBytes()))
            .withRecipientNonce(new Nonce("recipientNonce".getBytes()))
            .withRecipientKeys(List.of(recipient1))
            .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
            .withAffectedContractTransactions(Map.of(affectedHash, "somesecurityHash".getBytes()))
            .build();

    // FIXME: Cross module depenency!!!
    final String messageHash =
        Base64.getEncoder().encodeToString(payloadDigest.digest(encodedPayload.getCipherText()));

    final byte[] raw = encoder.encode(encodedPayload);

    StagingTransaction result =
        StagingTransactionUtils.fromRawPayload(raw, EncodedPayloadCodec.current());
    assertThat(result).isNotNull();
    assertThat(result.getHash()).isEqualTo(messageHash);
    assertThat(result.getPayload()).isEqualTo(raw);
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.PARTY_PROTECTION);
    assertThat(result.getValidationStage()).isNull();
    assertThat(result.getAffectedContractTransactions()).hasSize(1);

    result
        .getAffectedContractTransactions()
        .forEach(
            atx -> {
              assertThat(atx.getHash()).isEqualTo(affectedHash.encodeToBase64());
              assertThat(atx.getSourceTransaction()).isEqualTo(result);
            });
  }

  @Parameterized.Parameters(name = "{0}")
  public static List<ClientMode> configs() {
    return List.of(ClientMode.values());
  }
}
