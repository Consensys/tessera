package com.quorum.tessera.q2t.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.exception.PrivacyGroupPublishException;
import com.quorum.tessera.threading.CancellableCountDownLatch;
import com.quorum.tessera.threading.CancellableCountDownLatchFactory;
import com.quorum.tessera.threading.ExecutorFactory;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;

public class AsyncBatchPrivacyGroupPublisherTest {

  private ExecutorFactory mockExecutorFactory;

  private Executor mockExecutor;

  private CancellableCountDownLatchFactory mockCountDownLatchFactory;

  private CancellableCountDownLatch mockCountDownLatch;

  private RestPrivacyGroupPublisher mockPublisher;

  final PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
  final PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());

  private AsyncBatchPrivacyGroupPublisher publisher;

  @Before
  public void onSetup() {

    mockExecutor = mock(Executor.class);
    mockCountDownLatch = mock(CancellableCountDownLatch.class);

    mockExecutorFactory = mock(ExecutorFactory.class);
    when(mockExecutorFactory.createCachedThreadPool()).thenReturn(mockExecutor);

    mockCountDownLatchFactory = mock(CancellableCountDownLatchFactory.class);
    when(mockCountDownLatchFactory.create(anyInt())).thenReturn(mockCountDownLatch);

    mockPublisher = mock(RestPrivacyGroupPublisher.class);

    publisher =
        new AsyncBatchPrivacyGroupPublisher(
            mockExecutorFactory, mockCountDownLatchFactory, mockPublisher);
  }

  @Test
  public void publishNoRecipientsDoesNothing() {

    final byte[] data = new byte[5];
    final List<PublicKey> recipients = Collections.emptyList();

    publisher.publishPrivacyGroup(data, recipients);

    verify(mockExecutorFactory).createCachedThreadPool();
  }

  @Test
  public void publishUsesThreadForEachRecipient() throws InterruptedException {

    final byte[] data = new byte[5];

    publisher.publishPrivacyGroup(data, List.of(recipient, otherRecipient));

    verify(mockCountDownLatchFactory).create(2);
    verify(mockExecutorFactory).createCachedThreadPool();
    verify(mockExecutor, times(2)).execute(any(Runnable.class));
    verify(mockCountDownLatch).await();
  }

  @Test
  public void publishInterruptedException() throws InterruptedException {

    InterruptedException cause = new InterruptedException("some exception");

    doThrow(cause).when(mockCountDownLatch).await();

    Throwable ex =
        catchThrowable(
            () -> publisher.publishPrivacyGroup(new byte[5], List.of(recipient, otherRecipient)));
    assertThat(ex).isExactlyInstanceOf(PrivacyGroupPublishException.class);
    assertThat(ex).hasMessage("some exception");

    verify(mockExecutorFactory).createCachedThreadPool();
    verify(mockExecutor, times(2)).execute(any(Runnable.class));
    verify(mockCountDownLatchFactory).create(2);
    verify(mockCountDownLatch).await();
  }

  @Test
  public void publishSuccess() throws InterruptedException {

    final Executor realExecutor = Executors.newSingleThreadExecutor();
    when(mockExecutorFactory.createCachedThreadPool()).thenReturn(realExecutor);

    final AsyncBatchPrivacyGroupPublisher publisher =
        new AsyncBatchPrivacyGroupPublisher(
            mockExecutorFactory, mockCountDownLatchFactory, mockPublisher);

    doAnswer(
            invocation -> {
              // sleep main thread so publish threads can work
              Thread.sleep(200);
              return null;
            })
        .when(mockCountDownLatch)
        .await();

    final byte[] data = new byte[5];

    publisher.publishPrivacyGroup(data, List.of(recipient, otherRecipient));

    verify(mockPublisher).publishPrivacyGroup(eq(data), eq(recipient));
    verify(mockPublisher).publishPrivacyGroup(eq(data), eq(otherRecipient));

    verify(mockExecutorFactory, times(2)).createCachedThreadPool();
    verify(mockCountDownLatchFactory).create(2);

    verify(mockCountDownLatch, times(2)).countDown();
    verify(mockCountDownLatch).await();
  }

  @Test
  public void publishReturnsError() throws InterruptedException {

    final Executor realExecutor = Executors.newSingleThreadExecutor();
    when(mockExecutorFactory.createCachedThreadPool()).thenReturn(realExecutor);

    final AsyncBatchPrivacyGroupPublisher publisher =
        new AsyncBatchPrivacyGroupPublisher(
            mockExecutorFactory, mockCountDownLatchFactory, mockPublisher);

    final byte[] data = new byte[5];

    doThrow(new PrivacyGroupPublishException("OUCH"))
        .when(mockPublisher)
        .publishPrivacyGroup(eq(data), eq(otherRecipient));

    doAnswer(
            invocation -> {
              // sleep main thread so publish threads can work
              Thread.sleep(200);
              return null;
            })
        .when(mockCountDownLatch)
        .await();

    publisher.publishPrivacyGroup(data, List.of(recipient, otherRecipient));

    verify(mockCountDownLatch).cancelWithException(any());
  }
}
