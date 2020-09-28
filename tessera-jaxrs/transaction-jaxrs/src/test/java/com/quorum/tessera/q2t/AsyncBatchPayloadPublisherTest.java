package com.quorum.tessera.q2t;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.threading.CancellableCountDownLatch;
import com.quorum.tessera.threading.CancellableCountDownLatchFactory;
import com.quorum.tessera.threading.ExecutorFactory;
import com.quorum.tessera.transaction.publish.BatchPublishPayloadException;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.transaction.publish.PublishPayloadException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.*;

public class AsyncBatchPayloadPublisherTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncBatchPayloadPublisherTest.class);

    private AsyncBatchPayloadPublisher asyncPublisher;

    private Executor executor;

    private ExecutorFactory executorFactory;

    private CancellableCountDownLatch countDownLatch;

    private CancellableCountDownLatchFactory countDownLatchFactory;

    private PayloadPublisher publisher;

    private PayloadEncoder encoder;

    @Before
    public void onSetup() {
        this.executorFactory = mock(ExecutorFactory.class);
        this.executor = mock(Executor.class);
        when(executorFactory.create()).thenReturn(executor);

        this.countDownLatchFactory = mock(CancellableCountDownLatchFactory.class);
        this.countDownLatch = mock(CancellableCountDownLatch.class);
        when(countDownLatchFactory.create(anyInt())).thenReturn(countDownLatch);

        this.publisher = mock(PayloadPublisher.class);
        this.encoder = mock(PayloadEncoder.class);
        this.asyncPublisher =
                new AsyncBatchPayloadPublisher(executorFactory, countDownLatchFactory, publisher, encoder);
    }

    @After
    public void onTeardown() {
        verifyNoMoreInteractions(executor, executorFactory, countDownLatch, countDownLatchFactory, publisher, encoder);
    }

    @Test
    public void publishPayloadUsesThreadForEachRecipient() throws InterruptedException {
        EncodedPayload payload = mock(EncodedPayload.class);

        PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
        PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());

        List<PublicKey> recipients = List.of(recipient, otherRecipient);

        asyncPublisher.publishPayload(payload, recipients);

        verify(countDownLatchFactory).create(2);
        verify(executorFactory).create();
        verify(executor, times(2)).execute(any(Runnable.class));
        verify(countDownLatch).await();
    }

    @Test
    public void publishPayloadStripsAndPublishes() throws InterruptedException {
        Executor realExecutor = Executors.newCachedThreadPool();
        when(executorFactory.create()).thenReturn(realExecutor);

        asyncPublisher =
            new AsyncBatchPayloadPublisher(executorFactory, countDownLatchFactory, publisher, encoder);

        EncodedPayload payload = mock(EncodedPayload.class);
        EncodedPayload strippedPayload = mock(EncodedPayload.class);

        PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
        PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());

        List<PublicKey> recipients = List.of(recipient, otherRecipient);

        when(encoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(strippedPayload);

        doAnswer(
                        invocation -> {
                            // sleep main thread so publish threads can work
                            Thread.sleep(200);
                            return null;
                        })
                .when(countDownLatch)
                .await();

        asyncPublisher.publishPayload(payload, recipients);

        verify(executorFactory, times(2)).create();
        verify(countDownLatchFactory).create(2);
        verify(encoder).forRecipient(payload, recipient);
        verify(encoder).forRecipient(payload, otherRecipient);
        verify(publisher).publishPayload(strippedPayload, recipient);
        verify(publisher).publishPayload(strippedPayload, otherRecipient);
        verify(countDownLatch, times(2)).countDown();
        verify(countDownLatch).await();
    }

        @Test
        public void publishPayloadNoRecipientsDoesNothing() {
            EncodedPayload payload = mock(EncodedPayload.class);
            List<PublicKey> recipients = Collections.emptyList();

            asyncPublisher.publishPayload(payload, recipients);

            verify(executorFactory).create();
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

            verify(executorFactory).create();
            verify(executor, times(2)).execute(any(Runnable.class));
            verify(countDownLatchFactory).create(2);
            verify(countDownLatch).await();
        }

    @Test
    public void publishPayloadThrowsTaskExceptionIfOneFails() throws InterruptedException {
        Executor realExecutor = Executors.newCachedThreadPool();
        when(executorFactory.create()).thenReturn(realExecutor);

        asyncPublisher =
            new AsyncBatchPayloadPublisher(executorFactory, countDownLatchFactory, publisher, encoder);

        EncodedPayload payload = mock(EncodedPayload.class);
        EncodedPayload strippedPayload = mock(EncodedPayload.class);

        PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
        PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());

        List<PublicKey> recipients = List.of(recipient, otherRecipient);

        when(encoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(strippedPayload);

        PublishPayloadException cause = new PublishPayloadException("some exception");

        doThrow(cause)
            .doNothing()
            .when(publisher).publishPayload(any(EncodedPayload.class), any(PublicKey.class));

        doThrow(cause).when(countDownLatch).await();

        Throwable ex = catchThrowable(() -> asyncPublisher.publishPayload(payload, recipients));
        assertThat(ex).isEqualTo(cause);

        verify(executorFactory, times(2)).create();
        verify(countDownLatchFactory).create(2);
        verify(encoder).forRecipient(payload, recipient);
        verify(encoder).forRecipient(payload, otherRecipient);
        verify(publisher).publishPayload(strippedPayload, recipient);
        verify(publisher).publishPayload(strippedPayload, otherRecipient);
        verify(countDownLatch).countDown();
        verify(countDownLatch).cancelWithException(cause);
        verify(countDownLatch).await();
    }

    @Test
    public void publishPayloadRealLatchDoesNotWaitForAllTasksIfOneFails() throws InterruptedException {
        Executor realExecutor = Executors.newCachedThreadPool();
        when(executorFactory.create()).thenReturn(realExecutor);

        when(countDownLatchFactory.create(3)).thenReturn(new CancellableCountDownLatch(3));

        asyncPublisher =
            new AsyncBatchPayloadPublisher(executorFactory, countDownLatchFactory, publisher, encoder);

        EncodedPayload payload = mock(EncodedPayload.class);
        EncodedPayload strippedPayload = mock(EncodedPayload.class);

        PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
        PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());
        PublicKey anotherRecipient = PublicKey.from("ANOTHERRECIPIENT".getBytes());

        List<PublicKey> recipients = List.of(recipient, otherRecipient, anotherRecipient);

        when(encoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(strippedPayload);

        final AtomicInteger atomicInt = new AtomicInteger();

        PublishPayloadException cause = new PublishPayloadException("some publisher exception");

        doAnswer(
                        invocation -> {
                            longRunningTaskThenIncrement(atomicInt);
                            return null;
                        })
                .doAnswer(
                        invocation -> {
                            longRunningTaskThenIncrement(atomicInt);
                            return null;
                        })
                .doThrow(cause)
                .when(publisher)
                .publishPayload(any(EncodedPayload.class), any(PublicKey.class));

        Throwable ex = catchThrowable(() -> asyncPublisher.publishPayload(payload, recipients));
        assertThat(ex).isEqualTo(cause);

        verify(executorFactory, times(2)).create();
        verify(countDownLatchFactory).create(3);
        verify(encoder).forRecipient(payload, recipient);
        verify(encoder).forRecipient(payload, otherRecipient);
        verify(encoder).forRecipient(payload, anotherRecipient);
        verify(publisher).publishPayload(strippedPayload, recipient);
        verify(publisher).publishPayload(strippedPayload, otherRecipient);
        verify(publisher).publishPayload(strippedPayload, anotherRecipient);
    }

    @Test
    public void publishPayloadRealLatchSubsequentFailuresDoNothing()
            throws InterruptedException {
        Executor realExecutor = Executors.newCachedThreadPool();
        when(executorFactory.create()).thenReturn(realExecutor);

        when(countDownLatchFactory.create(3)).thenReturn(new CancellableCountDownLatch(3));

        asyncPublisher =
            new AsyncBatchPayloadPublisher(executorFactory, countDownLatchFactory, publisher, encoder);

        EncodedPayload payload = mock(EncodedPayload.class);
        EncodedPayload strippedPayload = mock(EncodedPayload.class);

        PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
        PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());
        PublicKey anotherRecipient = PublicKey.from("ANOTHERRECIPIENT".getBytes());

        List<PublicKey> recipients = List.of(recipient, otherRecipient, anotherRecipient);

        when(encoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(strippedPayload);

        PublishPayloadException cause = new PublishPayloadException("some publisher exception");

        doThrow(cause)
            .doThrow(new PublishPayloadException("some other exception"))
            .doThrow(new PublishPayloadException("yet another exception"))
            .when(publisher)
            .publishPayload(any(EncodedPayload.class), any(PublicKey.class));

        Throwable ex = catchThrowable(() -> asyncPublisher.publishPayload(payload, recipients));
        assertThat(ex).isEqualTo(cause);

        verify(executorFactory, times(2)).create();
        verify(countDownLatchFactory).create(3);
        verify(encoder).forRecipient(payload, recipient);
        verify(encoder).forRecipient(payload, otherRecipient);
        verify(encoder).forRecipient(payload, anotherRecipient);
        verify(publisher).publishPayload(strippedPayload, recipient);
        verify(publisher).publishPayload(strippedPayload, otherRecipient);
        verify(publisher).publishPayload(strippedPayload, anotherRecipient);
    }

    void longRunningTaskThenIncrement(AtomicInteger atomicInt) {
        int id = new Random().nextInt();

        LOGGER.debug("long running task " + id + " started");
        long startTime = System.nanoTime();
        new BigInteger(5000, 9, new Random());
        long stopTime = System.nanoTime();
        LOGGER.debug("long running task " + id + " completed: " + ((double) (stopTime - startTime) / 1000000000));

        atomicInt.incrementAndGet();
    }
}
