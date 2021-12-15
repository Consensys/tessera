package com.quorum.tessera.q2t.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.*;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.threading.CancellableCountDownLatch;
import com.quorum.tessera.threading.CancellableCountDownLatchFactory;
import com.quorum.tessera.threading.ExecutorFactory;
import com.quorum.tessera.transaction.publish.BatchPublishPayloadException;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.transaction.publish.PublishPayloadException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AsyncBatchPayloadPublisherTest {

  private AsyncBatchPayloadPublisher asyncPublisher;

  private Executor executor;

  private ExecutorFactory executorFactory;

  private CancellableCountDownLatch countDownLatch;

  private CancellableCountDownLatchFactory countDownLatchFactory;

  private PayloadPublisher publisher;

  @Before
  public void onSetup() {
    this.executorFactory = mock(ExecutorFactory.class);
    this.executor = mock(Executor.class);
    when(executorFactory.createCachedThreadPool()).thenReturn(executor);

    this.countDownLatchFactory = mock(CancellableCountDownLatchFactory.class);
    this.countDownLatch = mock(CancellableCountDownLatch.class);
    when(countDownLatchFactory.create(anyInt())).thenReturn(countDownLatch);

    this.publisher = mock(PayloadPublisher.class);
    this.asyncPublisher =
        new AsyncBatchPayloadPublisher(executorFactory, countDownLatchFactory, publisher);
  }

  @After
  public void onTeardown() {
    verifyNoMoreInteractions(
        executor, executorFactory, countDownLatch, countDownLatchFactory, publisher);
  }

  @Test
  public void publishPayloadUsesThreadForEachRecipient() throws InterruptedException {
    final EncodedPayload payload = mock(EncodedPayload.class);

    final PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
    final PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());

    final List<PublicKey> recipients = List.of(recipient, otherRecipient);

    asyncPublisher.publishPayload(payload, recipients);

    verify(countDownLatchFactory).create(2);
    verify(executorFactory).createCachedThreadPool();
    verify(executor, times(2)).execute(any(Runnable.class));
    verify(countDownLatch).await();
  }

  @Test
  public void publishPayloadStripsAndPublishes() throws InterruptedException {
    final Executor realExecutor = Executors.newSingleThreadExecutor();
    when(executorFactory.createCachedThreadPool()).thenReturn(realExecutor);

    asyncPublisher =
        new AsyncBatchPayloadPublisher(executorFactory, countDownLatchFactory, publisher);

    final PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
    final PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());

    final List<PublicKey> recipients = List.of(recipient, otherRecipient);

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(mock(PublicKey.class))
            .withRecipientKeys(recipients)
            .withRecipientBoxes(List.of("box1".getBytes(), "box2".getBytes()))
            .build();

    doAnswer(
            invocation -> {
              // sleep main thread so publish threads can work
              Thread.sleep(200);
              return null;
            })
        .when(countDownLatch)
        .await();

    asyncPublisher.publishPayload(payload, recipients);

    verify(executorFactory, times(2)).createCachedThreadPool();
    verify(countDownLatchFactory).create(2);
    verify(publisher).publishPayload(any(), eq(recipient));
    verify(publisher).publishPayload(any(), eq(otherRecipient));
    verify(countDownLatch, times(2)).countDown();
    verify(countDownLatch).await();
  }

  @Test
  public void publishPayloadNoRecipientsDoesNothing() {
    final EncodedPayload payload = mock(EncodedPayload.class);
    final List<PublicKey> recipients = Collections.emptyList();

    asyncPublisher.publishPayload(payload, recipients);

    verify(executorFactory).createCachedThreadPool();
  }

  @Test
  public void publishPayloadWrapsInterruptedException() throws InterruptedException {
    EncodedPayload payload = mock(EncodedPayload.class);

    PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
    PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());

    List<PublicKey> recipients = List.of(recipient, otherRecipient);

    InterruptedException cause = new InterruptedException("some exception");

    doThrow(cause).when(countDownLatch).await();

    Throwable ex = catchThrowable(() -> asyncPublisher.publishPayload(payload, recipients));
    assertThat(ex).isExactlyInstanceOf(BatchPublishPayloadException.class);
    assertThat(ex).hasCause(cause);

    verify(executorFactory).createCachedThreadPool();
    verify(executor, times(2)).execute(any(Runnable.class));
    verify(countDownLatchFactory).create(2);
    verify(countDownLatch).await();
  }

  @Test
  public void publishPayloadCancelsCountDownLatchIfOneTaskFails() throws InterruptedException {
    final Executor realExecutor = Executors.newCachedThreadPool();
    when(executorFactory.createCachedThreadPool()).thenReturn(realExecutor);

    asyncPublisher =
        new AsyncBatchPayloadPublisher(executorFactory, countDownLatchFactory, publisher);

    final PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
    final PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());

    final List<PublicKey> recipients = List.of(recipient, otherRecipient);

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(mock(PublicKey.class))
            .withRecipientKeys(recipients)
            .withRecipientBoxes(List.of("box1".getBytes(), "box2".getBytes()))
            .build();

    final PublishPayloadException cause = new PublishPayloadException("some exception");

    doThrow(cause)
        .doNothing()
        .when(publisher)
        .publishPayload(any(EncodedPayload.class), any(PublicKey.class));

    doAnswer(
            invocation -> {
              // sleep main thread so publish threads can work
              Thread.sleep(200);
              return null;
            })
        .when(countDownLatch)
        .await();

    asyncPublisher.publishPayload(payload, recipients);

    verify(executorFactory, times(2)).createCachedThreadPool();
    verify(countDownLatchFactory).create(2);
    verify(publisher).publishPayload(any(), eq(recipient));
    verify(publisher).publishPayload(any(), eq(otherRecipient));
    verify(countDownLatch).countDown();
    verify(countDownLatch).cancelWithException(cause);
    verify(countDownLatch).await();
  }
}
