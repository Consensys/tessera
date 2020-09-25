package com.quorum.tessera.q2t;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.threading.CompletionServiceFactory;
import com.quorum.tessera.transaction.publish.BatchPublishPayloadException;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.transaction.publish.PublishPayloadException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class AsyncBatchPayloadPublisherTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncBatchPayloadPublisherTest.class);

    private AsyncBatchPayloadPublisher asyncPublisher;

    private CompletionService<Void> completionService;

    private CompletionServiceFactory completionServiceFactory;

    private PayloadPublisher publisher;

    private PayloadEncoder encoder;

    @Before
    public void onSetup() {
        this.completionServiceFactory = mock(CompletionServiceFactory.class);
        this.completionService = mock(CompletionService.class);
        when(completionServiceFactory.create(any(Executor.class))).thenReturn(completionService);

        this.publisher = mock(PayloadPublisher.class);
        this.encoder = mock(PayloadEncoder.class);
        this.asyncPublisher = new AsyncBatchPayloadPublisher(completionServiceFactory, publisher, encoder);
    }

    @After
    public void onTeardown() {
        verifyNoMoreInteractions(completionService, completionServiceFactory, publisher, encoder);
    }

    @Test
    public void publishPayloadMockCompletionService() throws InterruptedException {
        EncodedPayload payload = mock(EncodedPayload.class);

        PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
        PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());

        List<PublicKey> recipients = List.of(recipient, otherRecipient);

        when(completionService.take()).thenReturn(mock(Future.class));

        asyncPublisher.publishPayload(payload, recipients);

        verify(completionServiceFactory).create(any(Executor.class));
        verify(completionService, times(2)).submit(any(Callable.class));
        verify(completionService, times(2)).take();
    }

    @Test
    public void publishPayloadRealCompletionService() {
        Executor executor = Executors.newCachedThreadPool();
        when(completionServiceFactory.create(any(Executor.class))).thenReturn(new ExecutorCompletionService<>(executor));

        EncodedPayload payload = mock(EncodedPayload.class);
        EncodedPayload strippedPayload = mock(EncodedPayload.class);

        PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
        PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());

        List<PublicKey> recipients = List.of(recipient, otherRecipient);

        when(encoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class)))
                .thenReturn(strippedPayload);

        asyncPublisher.publishPayload(payload, recipients);

        verify(completionServiceFactory).create(any(Executor.class));
        verify(encoder).forRecipient(payload, recipient);
        verify(encoder).forRecipient(payload, otherRecipient);
        verify(publisher).publishPayload(strippedPayload, recipient);
        verify(publisher).publishPayload(strippedPayload, otherRecipient);
    }

    @Test
    public void publishPayloadWrapsRuntimeException() throws InterruptedException {
        EncodedPayload payload = mock(EncodedPayload.class);

        PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
        PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());

        List<PublicKey> recipients = List.of(recipient, otherRecipient);

        RuntimeException cause = new RuntimeException("some error");

        when(completionService.take()).thenThrow(cause);

        Throwable ex = catchThrowable(() -> asyncPublisher.publishPayload(payload, recipients));
        assertThat(ex).isExactlyInstanceOf(BatchPublishPayloadException.class);
        assertThat(ex.getCause()).isEqualTo(cause);

        verify(completionServiceFactory).create(any(Executor.class));
        verify(completionService, times(2)).submit(any(Callable.class));
        verify(completionService, times(2)).take();
    }

    @Test
    public void publishPayloadExecutionExceptionRewrapped() throws InterruptedException, ExecutionException {
        EncodedPayload payload = mock(EncodedPayload.class);

        PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
        PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());

        List<PublicKey> recipients = List.of(recipient, otherRecipient);

        RuntimeException rootCause = new RuntimeException("some error");
        ExecutionException cause = new ExecutionException(rootCause);

        Future<Void> future = mock(Future.class);
        when(completionService.take()).thenReturn(future);
        when(future.get()).thenThrow(cause);

        Throwable ex = catchThrowable(() -> asyncPublisher.publishPayload(payload, recipients));
        assertThat(ex).isExactlyInstanceOf(BatchPublishPayloadException.class);
        assertThat(ex.getCause()).isEqualTo(rootCause);

        verify(completionServiceFactory).create(any(Executor.class));
        verify(completionService, times(2)).submit(any(Callable.class));
        verify(completionService, times(2)).take();
    }

    @Test
    public void publishPayloadUnwrapsKeyNotFoundException() throws InterruptedException, ExecutionException {
        EncodedPayload payload = mock(EncodedPayload.class);

        PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
        PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());

        List<PublicKey> recipients = List.of(recipient, otherRecipient);

        KeyNotFoundException rootCause = new KeyNotFoundException("some key not found error");
        ExecutionException cause = new ExecutionException(rootCause);

        Future<Void> future = mock(Future.class);
        when(completionService.take()).thenReturn(future);
        when(future.get()).thenThrow(cause);

        Throwable ex = catchThrowable(() -> asyncPublisher.publishPayload(payload, recipients));
        assertThat(ex).isExactlyInstanceOf(KeyNotFoundException.class);
        assertThat(ex).isEqualTo(rootCause);

        verify(completionServiceFactory).create(any(Executor.class));
        verify(completionService, times(2)).submit(any(Callable.class));
        verify(completionService, times(2)).take();
    }

    @Test
    public void publishPayloadWrapsInterruptedException() throws InterruptedException, ExecutionException {
        EncodedPayload payload = mock(EncodedPayload.class);

        PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
        PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());

        List<PublicKey> recipients = List.of(recipient, otherRecipient);

        InterruptedException cause = new InterruptedException("some interrupted error");

        when(completionService.take()).thenThrow(cause);

        Throwable ex = catchThrowable(() -> asyncPublisher.publishPayload(payload, recipients));
        assertThat(ex).isExactlyInstanceOf(BatchPublishPayloadException.class);
        assertThat(ex.getCause()).isEqualTo(cause);

        verify(completionServiceFactory).create(any(Executor.class));
        verify(completionService, times(2)).submit(any(Callable.class));
        verify(completionService, times(2)).take();
    }

    @Test
    public void publishPayloadRealCompletionServiceDoesNotWaitForAllTasksIfOneFails() {
        Executor executor = Executors.newCachedThreadPool();
        when(completionServiceFactory.create(any(Executor.class))).thenReturn(new ExecutorCompletionService<>(executor));

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
        PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());
        PublicKey anotherRecipient = PublicKey.from("ANOTHERRECIPIENT".getBytes());

        List<PublicKey> recipients = List.of(recipient, otherRecipient, anotherRecipient);

        when(encoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class)))
                .thenReturn(mock(EncodedPayload.class));

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

        Throwable ex = catchThrowable(() -> asyncPublisher.publishPayload(encodedPayload, recipients));

        assertThat(ex).isExactlyInstanceOf(BatchPublishPayloadException.class);
        assertThat(ex.getCause()).isEqualTo(cause);

        assertThat(atomicInt.get())
                .withFailMessage("publish should have failed-fast and not waited for completion of all tasks")
                .isEqualTo(0);

        verify(completionServiceFactory).create(any(Executor.class));
        verify(encoder, times(3)).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
        verify(publisher, times(3)).publishPayload(any(EncodedPayload.class), any(PublicKey.class));
    }

    @Test
    public void publishPayloadRealCompletionServiceExitsEarlyIfTaskFails() {
        Executor executor = Executors.newCachedThreadPool();
        when(completionServiceFactory.create(any(Executor.class))).thenReturn(new ExecutorCompletionService<>(executor));

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
        PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());
        PublicKey anotherRecipient = PublicKey.from("ANOTHERRECIPIENT".getBytes());

        List<PublicKey> recipients = List.of(recipient, otherRecipient, anotherRecipient);

        when(encoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class)))
            .thenReturn(mock(EncodedPayload.class));

        final AtomicInteger atomicInt = new AtomicInteger();

        PublishPayloadException cause = new PublishPayloadException("some publisher exception");

        doThrow(cause)
            .doAnswer(
                invocation -> {
                    LOGGER.debug("publisher::publishPayload, args={}", invocation.getArguments());
                    return null;
                })
            .doAnswer(
                invocation -> {
                    LOGGER.debug("publisher::publishPayload, args={}", invocation.getArguments());
                    return null;
                })
            .when(publisher)
            .publishPayload(any(EncodedPayload.class), any(PublicKey.class));

        Throwable ex = catchThrowable(() -> asyncPublisher.publishPayload(encodedPayload, recipients));

        assertThat(ex).isExactlyInstanceOf(BatchPublishPayloadException.class);
        assertThat(ex.getCause()).isEqualTo(cause);

        assertThat(atomicInt.get())
            .withFailMessage("publish should have failed-fast and not waited for completion of all tasks")
            .isEqualTo(0);

        verify(completionServiceFactory).create(any(Executor.class));
        verify(encoder, times(3)).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
        verify(publisher, times(3)).publishPayload(any(EncodedPayload.class), any(PublicKey.class));
    }

    @Test
    public void publishPayloadRealCompletionServiceIsDrainedInBackgroundIfTaskFails() {
        Executor executor = Executors.newCachedThreadPool();
        when(completionServiceFactory.create(any(Executor.class))).thenReturn(new ExecutorCompletionService<>(executor));

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
        PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());
        PublicKey anotherRecipient = PublicKey.from("ANOTHERRECIPIENT".getBytes());

        List<PublicKey> recipients = List.of(recipient, otherRecipient, anotherRecipient);

        when(encoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class)))
            .thenReturn(mock(EncodedPayload.class));

        final AtomicInteger atomicInt = new AtomicInteger();

        PublishPayloadException cause = new PublishPayloadException("some publisher exception");

        doThrow(cause)
            .doAnswer(
                invocation -> {
                    atomicInt.incrementAndGet();
                    return null;
                })
            .doAnswer(
                invocation -> {
                    atomicInt.incrementAndGet();
                    return null;
                })
            .when(publisher)
            .publishPayload(any(EncodedPayload.class), any(PublicKey.class));

        Throwable ex = catchThrowable(() -> asyncPublisher.publishPayload(encodedPayload, recipients));

        assertThat(ex).isExactlyInstanceOf(BatchPublishPayloadException.class);
        assertThat(ex.getCause()).isEqualTo(cause);

        int want = 2;
        assertThat(atomicInt.get())
            .withFailMessage("expected <%s> tasks to be executed in background, got %<s>", want, atomicInt.get())
            .isEqualTo(want);

        verify(completionServiceFactory).create(any(Executor.class));
        verify(encoder, times(3)).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
        verify(publisher, times(3)).publishPayload(any(EncodedPayload.class), any(PublicKey.class));
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
