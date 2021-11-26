package com.quorum.tessera.enclave.rest;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.*;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.service.Service.Status;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class RestfulEnclaveClientTest {

  private Enclave enclave;

  private JerseyTest jersey;

  private RestfulEnclaveClient enclaveClient;

  private EncodedPayloadCodec encodedPayloadCodec = EncodedPayloadCodec.LEGACY;

  private PayloadEncoder payloadEncoder;

  @BeforeClass
  public static void beforeClass() {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  @Before
  public void setUp() throws Exception {

    payloadEncoder = PayloadEncoder.create(encodedPayloadCodec);

    Config config = new Config();
    config.setEncryptor(new EncryptorConfig());
    config.getEncryptor().setType(EncryptorType.NACL);
    config.setKeys(new KeyConfiguration());
    config
        .getKeys()
        .setKeyData(
            List.of(
                new KeyData() {
                  @Override
                  public String getPrivateKey() {
                    return "yAWAJjwPqUtNVlqGjSrBmr1/iIkghuOh1803Yzx9jLM=";
                  }

                  @Override
                  public String getPublicKey() {
                    return "/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=";
                  }

                  {
                  }
                }));
    ConfigFactory.create().store(config);

    enclave = mock(Enclave.class);
    jersey = Util.create(enclave);

    jersey.setUp();

    enclaveClient = new RestfulEnclaveClient(jersey.client(), jersey.target().getUri());
  }

  @After
  public void tearDown() throws Exception {
    try {
      verifyNoMoreInteractions(enclave);
      jersey.tearDown();
    } finally {
      //  mockedStaticEnclave.close();
    }
  }

  @Test
  public void defaultPublicKey() {

    PublicKey key = PublicKey.from("HELLOW".getBytes());

    when(enclave.defaultPublicKey()).thenReturn(key);

    PublicKey result = enclaveClient.defaultPublicKey();

    assertThat(result).isEqualTo(key);

    verify(enclave).defaultPublicKey();
  }

  @Test
  public void getPublicKeys() {

    PublicKey key = PublicKey.from("HELLOW".getBytes());

    when(enclave.getPublicKeys()).thenReturn(Collections.singleton(key));

    Set<PublicKey> result = enclaveClient.getPublicKeys();

    assertThat(result).containsExactly(key);

    verify(enclave).getPublicKeys();
  }

  @Test
  public void getForwardingKeys() {

    PublicKey key = PublicKey.from("HELLOW".getBytes());

    when(enclave.getForwardingKeys()).thenReturn(Collections.singleton(key));

    Set<PublicKey> result = enclaveClient.getForwardingKeys();

    assertThat(result).containsExactly(key);

    verify(enclave).getForwardingKeys();
  }

  @Test
  public void encryptPayload() {

    byte[] message = "HELLOW".getBytes();

    PublicKey senderPublicKey = PublicKey.from("PublicKey".getBytes());
    List<PublicKey> recipientPublicKeys =
        Arrays.asList(PublicKey.from("RecipientPublicKey".getBytes()));

    EncodedPayload encodedPayload = Fixtures.createSample();

    List<AffectedTransaction> affectedTransactions =
        List.of(
            AffectedTransaction.Builder.create()
                .withHash("hash".getBytes())
                .withPayload(encodedPayload)
                .build());

    when(enclave.encryptPayload(eq(message), eq(senderPublicKey), eq(recipientPublicKeys), any()))
        .thenReturn(encodedPayload);

    final PrivacyMetadata privacyMetaData =
        PrivacyMetadata.Builder.create()
            .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
            .withAffectedTransactions(affectedTransactions)
            .build();

    EncodedPayload result =
        enclaveClient.encryptPayload(
            message, senderPublicKey, recipientPublicKeys, privacyMetaData);

    assertThat(result).isNotNull();

    byte[] encodedResult = payloadEncoder.encode(result);
    byte[] encodedEncodedPayload = payloadEncoder.encode(encodedPayload);

    assertThat(result.getPrivacyGroupId()).isNotPresent();

    assertThat(encodedResult).isEqualTo(encodedEncodedPayload);

    verify(enclave)
        .encryptPayload(
            eq(message), eq(senderPublicKey), eq(recipientPublicKeys), any(PrivacyMetadata.class));
  }

  @Test
  public void encryptPayloadWithPrivacyGroupId() {

    byte[] message = "HELLOW".getBytes();

    PublicKey senderPublicKey = PublicKey.from("PublicKey".getBytes());
    List<PublicKey> recipientPublicKeys =
        Arrays.asList(PublicKey.from("RecipientPublicKey".getBytes()));

    final EncodedPayload payloadWithGroupId =
        EncodedPayload.Builder.from(Fixtures.createSample())
            .withPrivacyGroupId(PrivacyGroup.Id.fromBytes("group".getBytes()))
            .build();

    List<AffectedTransaction> affectedTransactions =
        List.of(
            AffectedTransaction.Builder.create()
                .withHash("hash".getBytes())
                .withPayload(payloadWithGroupId)
                .build());

    final PrivacyMetadata privacyMetaData =
        PrivacyMetadata.Builder.create()
            .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
            .withAffectedTransactions(affectedTransactions)
            .withPrivacyGroupId(PrivacyGroup.Id.fromBytes("group".getBytes()))
            .build();

    when(enclave.encryptPayload(
            eq(message), eq(senderPublicKey), eq(recipientPublicKeys), any(PrivacyMetadata.class)))
        .thenReturn(payloadWithGroupId);

    EncodedPayload result =
        enclaveClient.encryptPayload(
            message, senderPublicKey, recipientPublicKeys, privacyMetaData);

    assertThat(result).isNotNull();

    byte[] encodedResult = payloadEncoder.encode(result);
    byte[] encodedEncodedPayload = payloadEncoder.encode(payloadWithGroupId);

    assertThat(encodedResult).isEqualTo(encodedEncodedPayload);

    ArgumentCaptor<PrivacyMetadata> argumentCaptor = ArgumentCaptor.forClass(PrivacyMetadata.class);

    verify(enclave)
        .encryptPayload(
            eq(message), eq(senderPublicKey), eq(recipientPublicKeys), argumentCaptor.capture());

    final PrivacyMetadata passingThroughPrivacyData = argumentCaptor.getValue();
    assertThat(passingThroughPrivacyData.getPrivacyGroupId())
        .isPresent()
        .get()
        .isEqualTo(PrivacyGroup.Id.fromBytes("group".getBytes()));
    assertThat(passingThroughPrivacyData.getPrivacyMode())
        .isEqualTo(privacyMetaData.getPrivacyMode());
    assertThat(passingThroughPrivacyData.getAffectedContractTransactions())
        .isEqualTo(privacyMetaData.getAffectedContractTransactions());
    assertThat(passingThroughPrivacyData.getExecHash()).isEqualTo(privacyMetaData.getExecHash());
  }

  @Test
  public void encryptPayloadRaw() {

    byte[] message = "HELLOW".getBytes();

    byte[] encryptedKey = "encryptedKey".getBytes();

    Nonce nonce = new Nonce("Nonce".getBytes());

    PublicKey senderPublicKey = PublicKey.from("SenderPublicKey".getBytes());

    List<PublicKey> recipientPublicKeys =
        Arrays.asList(PublicKey.from("RecipientPublicKey".getBytes()));

    RawTransaction rawTransaction =
        new RawTransaction(message, encryptedKey, nonce, senderPublicKey);

    EncodedPayload encodedPayload = Fixtures.createSample();

    List<AffectedTransaction> affectedTransactions =
        List.of(
            AffectedTransaction.Builder.create()
                .withHash("hash".getBytes())
                .withPayload(encodedPayload)
                .build());

    final PrivacyMetadata privacyMetaData =
        PrivacyMetadata.Builder.create()
            .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
            .withAffectedTransactions(affectedTransactions)
            .build();

    when(enclave.encryptPayload(any(RawTransaction.class), any(List.class), any()))
        .thenReturn(encodedPayload);

    EncodedPayload result =
        enclaveClient.encryptPayload(rawTransaction, recipientPublicKeys, privacyMetaData);

    assertThat(result).isNotNull();

    byte[] encodedResult = payloadEncoder.encode(result);
    byte[] encodedEncodedPayload = payloadEncoder.encode(encodedPayload);

    assertThat(encodedResult).isEqualTo(encodedEncodedPayload);

    verify(enclave).encryptPayload(any(RawTransaction.class), any(List.class), any());
  }

  @Test
  public void encryptPayloadRawWithPrivacyGroupId() {

    byte[] message = "HELLOW".getBytes();

    byte[] encryptedKey = "encryptedKey".getBytes();

    Nonce nonce = new Nonce("Nonce".getBytes());

    PublicKey senderPublicKey = PublicKey.from("SenderPublicKey".getBytes());

    List<PublicKey> recipientPublicKeys =
        Arrays.asList(PublicKey.from("RecipientPublicKey".getBytes()));

    RawTransaction rawTransaction =
        new RawTransaction(message, encryptedKey, nonce, senderPublicKey);

    EncodedPayload encodedPayloadWithGroupId =
        EncodedPayload.Builder.from(Fixtures.createSample())
            .withPrivacyGroupId(PrivacyGroup.Id.fromBytes("group".getBytes()))
            .build();

    List<AffectedTransaction> affectedTransactions =
        List.of(
            AffectedTransaction.Builder.create()
                .withHash("hash".getBytes())
                .withPayload(encodedPayloadWithGroupId)
                .build());

    final PrivacyMetadata privacyMetaData =
        PrivacyMetadata.Builder.create()
            .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
            .withAffectedTransactions(affectedTransactions)
            .withPrivacyGroupId(PrivacyGroup.Id.fromBytes("group".getBytes()))
            .build();

    when(enclave.encryptPayload(any(RawTransaction.class), any(List.class), any()))
        .thenReturn(encodedPayloadWithGroupId);

    EncodedPayload result =
        enclaveClient.encryptPayload(rawTransaction, recipientPublicKeys, privacyMetaData);

    assertThat(result).isNotNull();

    byte[] encodedResult = payloadEncoder.encode(result);
    byte[] encodedEncodedPayload = payloadEncoder.encode(encodedPayloadWithGroupId);

    assertThat(encodedResult).isEqualTo(encodedEncodedPayload);

    ArgumentCaptor<PrivacyMetadata> argumentCaptor = ArgumentCaptor.forClass(PrivacyMetadata.class);

    verify(enclave)
        .encryptPayload(any(RawTransaction.class), any(List.class), argumentCaptor.capture());

    final PrivacyMetadata passingThroughPrivacyData = argumentCaptor.getValue();
    assertThat(passingThroughPrivacyData.getPrivacyGroupId())
        .isPresent()
        .get()
        .isEqualTo(PrivacyGroup.Id.fromBytes("group".getBytes()));
    assertThat(passingThroughPrivacyData.getPrivacyMode())
        .isEqualTo(privacyMetaData.getPrivacyMode());
    assertThat(passingThroughPrivacyData.getAffectedContractTransactions())
        .isEqualTo(privacyMetaData.getAffectedContractTransactions());
    assertThat(passingThroughPrivacyData.getExecHash()).isEqualTo(privacyMetaData.getExecHash());
  }

  @Test
  public void encryptPayloadToRaw() {

    byte[] message = "HELLOW".getBytes();

    PublicKey senderPublicKey = PublicKey.from("SenderPublicKey".getBytes());

    byte[] encryptedKey = "encryptedKey".getBytes();
    Nonce nonce = new Nonce("Nonce".getBytes());
    RawTransaction rawTransaction =
        new RawTransaction(message, encryptedKey, nonce, senderPublicKey);

    when(enclave.encryptRawPayload(message, senderPublicKey)).thenReturn(rawTransaction);

    RawTransaction result = enclaveClient.encryptRawPayload(message, senderPublicKey);

    assertThat(result).isNotNull();

    assertThat(result).isEqualTo(rawTransaction);

    verify(enclave).encryptRawPayload(message, senderPublicKey);
  }

  @Test
  public void unencryptTransaction() throws Exception {

    EncodedPayload payload = Fixtures.createSample();

    PublicKey providedKey = PublicKey.from("ProvidedKey".getBytes());

    byte[] outcome = "SUCCESS".getBytes();

    when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class)))
        .thenReturn(outcome);

    byte[] result = enclaveClient.unencryptTransaction(payload, providedKey);

    assertThat(result).isEqualTo(outcome);

    verify(enclave).unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));
  }

  @Test
  public void unencryptRawPayload() throws Exception {

    byte[] message = "HELLOW".getBytes();

    PublicKey senderPublicKey = PublicKey.from("SenderPublicKey".getBytes());

    byte[] encryptedKey = "encryptedKey".getBytes();
    Nonce nonce = new Nonce("Nonce".getBytes());

    RawTransaction rawTransaction =
        new RawTransaction(message, encryptedKey, nonce, senderPublicKey);

    when(enclave.unencryptRawPayload(any(RawTransaction.class)))
        .thenReturn("unencryptedRawTransaction".getBytes());

    byte[] result = enclaveClient.unencryptRawPayload(rawTransaction);

    assertThat(result).containsExactly("unencryptedRawTransaction".getBytes());

    verify(enclave).unencryptRawPayload(any(RawTransaction.class));
  }

  @Test
  public void findInvalidSecurityHashes() throws Exception {

    EncodedPayload payload = Fixtures.createSample();

    TxHash txHash = new TxHash("acoth1".getBytes());
    AffectedTransaction affectedTransaction = mock(AffectedTransaction.class);
    when(affectedTransaction.getHash()).thenReturn(txHash);
    when(affectedTransaction.getPayload()).thenReturn(payload);

    Set<TxHash> invalidSecHashes = Set.of(txHash);

    when(enclave.findInvalidSecurityHashes(any(EncodedPayload.class), anyList()))
        .thenReturn(invalidSecHashes);

    Set<TxHash> result =
        enclaveClient.findInvalidSecurityHashes(payload, List.of(affectedTransaction));

    assertThat(result).containsExactly(txHash);
    assertThat(result.iterator().next()).isNotSameAs(txHash).isEqualTo(txHash);

    verify(enclave).findInvalidSecurityHashes(any(EncodedPayload.class), anyList());
  }

  @Test
  public void createNewRecipientBox() {

    EncodedPayload payload = Fixtures.createSample();

    PublicKey providedKey = PublicKey.from("ProvidedKey".getBytes());

    byte[] outcome = "SUCCESS".getBytes();

    when(enclave.createNewRecipientBox(any(EncodedPayload.class), any(PublicKey.class)))
        .thenReturn(outcome);

    byte[] result = enclaveClient.createNewRecipientBox(payload, providedKey);

    assertThat(result).isEqualTo(outcome);

    verify(enclave).createNewRecipientBox(any(EncodedPayload.class), any(PublicKey.class));
  }

  @Test
  public void statusStarted() {
    when(enclave.status()).thenReturn(Status.STARTED);
    assertThat(enclaveClient.status()).isEqualTo(Status.STARTED);

    verify(enclave).status();
  }

  @Test
  public void statusStopped() {

    when(enclave.status()).thenThrow(RuntimeException.class);
    assertThat(enclaveClient.status()).isEqualTo(Status.STOPPED);
    verify(enclave).status();
  }

  @Test
  public void enclaveUnavailable() throws Exception {

    ExecutorService executorService = mock(ExecutorService.class);

    Future<?> future = mock(Future.class);

    doReturn(future).when(executorService).submit(any(Callable.class));

    doThrow(TimeoutException.class).when(future).get(anyLong(), any(TimeUnit.class));

    RestfulEnclaveClient restfulEnclaveClient =
        new RestfulEnclaveClient(jersey.client(), jersey.target().getUri(), executorService);

    Status result = restfulEnclaveClient.status();

    assertThat(result).isEqualTo(Status.STOPPED);
  }

  @Test
  public void remoteEnclaveReturnsError() {

    when(enclave.defaultPublicKey()).thenThrow(new RuntimeException());

    try {
      enclaveClient.defaultPublicKey();
      failBecauseExceptionWasNotThrown(EnclaveException.class);
    } catch (EnclaveException ex) {
      verify(enclave).defaultPublicKey();
    }
  }
}
