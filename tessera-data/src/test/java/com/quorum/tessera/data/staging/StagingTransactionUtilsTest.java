package com.quorum.tessera.data.staging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.ClientMode;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.PublicKey;
import java.nio.charset.StandardCharsets;
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

  private final MockedStatic<PayloadEncoder> payloadEncoderMockedStatic =
      mockStatic(PayloadEncoder.class);

  private PayloadEncoder payloadEncoder;

  private ClientMode clientMode;

  private final MockedStatic<ConfigFactory> configFactoryMockedStatic =
      mockStatic(ConfigFactory.class);;

  private final MockedStatic<PayloadDigest> payloadDigestMockedStatic =
      mockStatic(PayloadDigest.class);;

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

    payloadEncoder = mock(PayloadEncoder.class);

    payloadEncoderMockedStatic
        .when(() -> PayloadEncoder.create(any(EncodedPayloadCodec.class)))
        .thenReturn(Optional.of(payloadEncoder));

    Config config = mock(Config.class);
    when(config.getClientMode()).thenReturn(clientMode);
    configFactory = mock(ConfigFactory.class);
    when(configFactory.getConfig()).thenReturn(config);

    configFactoryMockedStatic.when(ConfigFactory::create).thenReturn(configFactory);
    payloadDigest = mock(PayloadDigest.class);

    payloadDigestMockedStatic.when(PayloadDigest::create).thenReturn(payloadDigest);
  }

  @After
  public void afterTest() {
    List.of(payloadEncoderMockedStatic, payloadDigestMockedStatic, configFactoryMockedStatic)
        .forEach(
            m -> {
              try (m) {
              } catch (Throwable ex) {
              }
            });

    verifyNoMoreInteractions(payloadEncoder, payloadDigest);
  }

  @Test
  public void testFromRawPayload() {

    final byte[] payloadData = "SomePayloadData".getBytes(StandardCharsets.UTF_8);
    final EncodedPayload encodedPayload = mock(EncodedPayload.class);

    TxHash txHash = mock(TxHash.class);
    when(txHash.getBytes()).thenReturn(TxHash.class.getSimpleName().getBytes());
    SecurityHash securityHash = mock(SecurityHash.class);
    when(securityHash.getData()).thenReturn(SecurityHash.class.getSimpleName().getBytes());

    when(encodedPayload.getAffectedContractTransactions()).thenReturn(Map.of(txHash, securityHash));

    when(payloadEncoder.decode(payloadData)).thenReturn(encodedPayload);
    when(payloadEncoder.encodedPayloadCodec()).thenReturn(EncodedPayloadCodec.UNSUPPORTED);
    byte[] cipherText = "CipherText".getBytes(StandardCharsets.UTF_8);
    when(encodedPayload.getCipherText()).thenReturn(cipherText);
    when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(payloadDigest.digest(cipherText)).thenReturn(cipherText);

    StagingTransaction result =
        StagingTransactionUtils.fromRawPayload(payloadData, EncodedPayloadCodec.UNSUPPORTED);

    assertThat(result).isNotNull();
    assertThat(result.getHash()).isEqualTo("Q2lwaGVyVGV4dA==");
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    assertThat(result.getAffectedContractTransactions()).hasSize(1);
    assertThat(result.getPayload()).isSameAs(payloadData);
    assertThat(result.getEncodedPayloadCodec()).isEqualTo(EncodedPayloadCodec.UNSUPPORTED);

    StagingAffectedTransaction stagingAffectedTransaction =
        result.getAffectedContractTransactions().iterator().next();
    assertThat(stagingAffectedTransaction.getSourceTransaction()).isSameAs(result);
    assertThat(stagingAffectedTransaction.getHash())
        .isEqualTo(Base64.getEncoder().encodeToString(TxHash.class.getSimpleName().getBytes()));

    verify(payloadEncoder).encodedPayloadCodec();
    verify(payloadDigest).digest(cipherText);
    verify(payloadEncoder).decode(payloadData);
  }

  @Parameterized.Parameters(name = "{0}")
  public static List<ClientMode> configs() {
    return List.of(ClientMode.values());
  }
}
