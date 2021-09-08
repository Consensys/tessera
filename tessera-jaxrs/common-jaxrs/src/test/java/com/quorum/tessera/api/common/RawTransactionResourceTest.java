package com.quorum.tessera.api.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.api.StoreRawRequest;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.TransactionManager;
import jakarta.ws.rs.core.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RawTransactionResourceTest {

  private RawTransactionResource transactionResource;

  private TransactionManager transactionManager;

  @Before
  public void onSetup() {
    this.transactionManager = mock(TransactionManager.class);

    this.transactionResource = new RawTransactionResource(transactionManager);
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(transactionManager);
  }

  @Test
  public void store() {

    com.quorum.tessera.transaction.StoreRawResponse response =
        mock(com.quorum.tessera.transaction.StoreRawResponse.class);
    MessageHash transactionHash = mock(MessageHash.class);
    when(transactionHash.getHashBytes()).thenReturn("TXN".getBytes());
    when(response.getHash()).thenReturn(transactionHash);
    when(transactionManager.store(any())).thenReturn(response);

    final StoreRawRequest storeRawRequest = new StoreRawRequest();
    storeRawRequest.setPayload("PAYLOAD".getBytes());
    storeRawRequest.setFrom("Sender".getBytes());
    final Response result = transactionResource.store(storeRawRequest);

    assertThat(result.getStatus()).isEqualTo(200);
    verify(transactionManager).store(any());
  }

  @Test
  public void storeUsingDefaultKey() {
    com.quorum.tessera.transaction.StoreRawResponse response =
        mock(com.quorum.tessera.transaction.StoreRawResponse.class);
    MessageHash transactionHash = mock(MessageHash.class);
    when(transactionHash.getHashBytes()).thenReturn("TXN".getBytes());
    when(response.getHash()).thenReturn(transactionHash);
    when(transactionManager.store(any())).thenReturn(response);
    when(transactionManager.defaultPublicKey()).thenReturn(PublicKey.from("SENDER".getBytes()));

    final StoreRawRequest storeRawRequest = new StoreRawRequest();
    storeRawRequest.setPayload("PAYLOAD".getBytes());
    final Response result = transactionResource.store(storeRawRequest);

    assertThat(result.getStatus()).isEqualTo(200);
    verify(transactionManager).store(any());
    verify(transactionManager).defaultPublicKey();
  }

  @Test
  public void storeVersion21() {

    com.quorum.tessera.transaction.StoreRawResponse response =
        mock(com.quorum.tessera.transaction.StoreRawResponse.class);
    MessageHash transactionHash = mock(MessageHash.class);
    when(transactionHash.getHashBytes()).thenReturn("TXN".getBytes());
    when(response.getHash()).thenReturn(transactionHash);
    when(transactionManager.store(any())).thenReturn(response);

    final StoreRawRequest storeRawRequest = new StoreRawRequest();
    storeRawRequest.setPayload("PAYLOAD".getBytes());
    storeRawRequest.setFrom("Sender".getBytes());
    final Response result = transactionResource.storeVersion21(storeRawRequest);

    assertThat(result.getStatus()).isEqualTo(200);
    verify(transactionManager).store(any());
  }

  @Test
  public void storeUsingDefaultKeyVersion21() {
    com.quorum.tessera.transaction.StoreRawResponse response =
        mock(com.quorum.tessera.transaction.StoreRawResponse.class);
    MessageHash transactionHash = mock(MessageHash.class);
    when(transactionHash.getHashBytes()).thenReturn("TXN".getBytes());
    when(response.getHash()).thenReturn(transactionHash);
    when(transactionManager.store(any())).thenReturn(response);
    when(transactionManager.defaultPublicKey()).thenReturn(PublicKey.from("SENDER".getBytes()));

    final StoreRawRequest storeRawRequest = new StoreRawRequest();
    storeRawRequest.setPayload("PAYLOAD".getBytes());
    final Response result = transactionResource.storeVersion21(storeRawRequest);

    assertThat(result.getStatus()).isEqualTo(200);
    verify(transactionManager).store(any());
    verify(transactionManager).defaultPublicKey();
  }

  @Test
  public void defaultConstrcutor() {
    RawTransactionResource resource;
    try (var mockedStaticTM = mockStatic(TransactionManager.class)) {
      TransactionManager transactionManager = mock(TransactionManager.class);
      mockedStaticTM.when(TransactionManager::create).thenReturn(transactionManager);
      resource = new RawTransactionResource();
      mockedStaticTM.verify(TransactionManager::create);
      mockedStaticTM.verifyNoMoreInteractions();
      verifyNoInteractions(transactionManager);
    }
    assertThat(resource).isNotNull();
  }
}
