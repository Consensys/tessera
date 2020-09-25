package com.quorum.tessera.q2t;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.threading.CompletionServiceFactory;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

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

//    @Test
//    public void publishPayloadExitsEarlyIfTaskFails() {
//        EncodedPayload encodedPayload = mock(EncodedPayload.class);
//
//        PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
//        PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());
//        PublicKey anotherRecipient = PublicKey.from("ANOTHERRECIPIENT".getBytes());
//
//        List<PublicKey> recipients = List.of(recipient, otherRecipient, anotherRecipient);
//
//        when(encoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class)))
//                .thenReturn(mock(EncodedPayload.class));
//
//        final AtomicInteger atomicInt = new AtomicInteger();
//
//        doAnswer(
//                        invocation -> {
//                            longRunningTaskThenIncrement(atomicInt);
//                            return null;
//                        })
//                .doAnswer(
//                        invocation -> {
//                            longRunningTaskThenIncrement(atomicInt);
//                            return null;
//                        })
//                .doThrow(new PublishPayloadException("some publisher exception"))
//                .when(publisher)
//                .publishPayload(any(EncodedPayload.class), any(PublicKey.class));
//
//        Throwable ex = catchThrowable(() -> asyncPublisher.publishPayload(encodedPayload, recipients));
//
//        assertThat(ex).isExactlyInstanceOf(BatchPublishPayloadException.class);
//        assertThat(ex.getCause()).isExactlyInstanceOf(PublishPayloadException.class);
//        assertThat(ex.getCause()).hasMessage("some publisher exception");
//
//        assertThat(atomicInt.get())
//                .withFailMessage("publish should have failed-fast and not waited for completion of all tasks")
//                .isEqualTo(0);
//
//        verify(encoder, times(3)).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
//        verify(publisher, times(3)).publishPayload(any(EncodedPayload.class), any(PublicKey.class));
//    }
//
//    @Test
//    public void publishPayloadInterrupted() throws InterruptedException {
//        CompletionService<Void> completionService = mock(CompletionService.class);
//        when(completionServiceFactory.create(any(Executor.class))).thenReturn(completionService);
//
//        EncodedPayload encodedPayload = mock(EncodedPayload.class);
//
//        PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
//
//        List<PublicKey> recipients = List.of(recipient);
//
//        when(completionService.submit(any(Callable.class))).thenReturn(mock(Future.class));
//
//        when(encoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class)))
//                .thenReturn(mock(EncodedPayload.class));
//
//        when(completionService.take()).thenThrow(new InterruptedException("some publish interrupted error"));
//
//        Throwable ex = catchThrowable(() -> asyncPublisher.publishPayload(encodedPayload, recipients));
//
//        assertThat(ex).isExactlyInstanceOf(BatchPublishPayloadException.class);
//        assertThat(ex.getCause()).isExactlyInstanceOf(InterruptedException.class);
//        assertThat(ex.getCause()).hasMessage("some publish interrupted error");
//
//        verify(completionService).submit(any(Callable.class));
//        verify(completionService).take();
//    }
//
//    @Test
//    public void publishPayloadKeyNotFound() {
//        EncodedPayload encodedPayload = mock(EncodedPayload.class);
//
//        PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
//
//        List<PublicKey> recipients = List.of(recipient);
//
//        when(encoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class)))
//                .thenReturn(mock(EncodedPayload.class));
//
//        final AtomicInteger atomicInt = new AtomicInteger();
//
//        doThrow(new KeyNotFoundException("some error"))
//                .when(publisher)
//                .publishPayload(any(EncodedPayload.class), any(PublicKey.class));
//
//        Throwable ex = catchThrowable(() -> asyncPublisher.publishPayload(encodedPayload, recipients));
//
//        assertThat(ex).isExactlyInstanceOf(KeyNotFoundException.class);
//        assertThat(ex).hasMessage("some error");
//
//        assertThat(atomicInt.get())
//                .withFailMessage("publish should have failed-fast and not waited for completion of all tasks")
//                .isEqualTo(0);
//
//        verify(encoder).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
//        verify(publisher).publishPayload(any(EncodedPayload.class), any(PublicKey.class));
//    }
//
//    void longRunningTaskThenIncrement(AtomicInteger atomicInteger) {
//        int id = new Random().nextInt();
//
//        LOGGER.debug("long running task " + id + " started");
//        long startTime = System.nanoTime();
//        new BigInteger(5000, 9, new Random());
//        long stopTime = System.nanoTime();
//        LOGGER.debug("long running task " + id + " completed: " + ((double) (stopTime - startTime) / 1000000000));
//
//        atomicInteger.incrementAndGet();
//    }
}
