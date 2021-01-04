package com.quorum.tessera.q2t;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.privacygroup.exception.PrivacyGroupPublishException;
import com.quorum.tessera.threading.CancellableCountDownLatch;
import com.quorum.tessera.threading.CancellableCountDownLatchFactory;
import com.quorum.tessera.threading.ExecutorFactory;
import org.junit.Before;
import org.junit.Test;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class AsyncRestPrivacyGroupPublisherTest {

    private ExecutorFactory mockExecutorFactory;

    private Executor mockExecutor;

    private CancellableCountDownLatchFactory mockCountDownLatchFactory;

    private CancellableCountDownLatch mockCountDownLatch;

    private RestPrivacyGroupPublisher mockPublisher;

    private Discovery mockDiscovery;

    private List<PublicKey> recipients;

    private AsyncRestPrivacyGroupPublisher publisher;

    @Before
    public void onSetup() {

        mockExecutor = mock(Executor.class);
        mockCountDownLatch = mock(CancellableCountDownLatch.class);

        mockExecutorFactory = mock(ExecutorFactory.class);
        when(mockExecutorFactory.createCachedThreadPool()).thenReturn(mockExecutor);

        mockCountDownLatchFactory = mock(CancellableCountDownLatchFactory.class);
        when(mockCountDownLatchFactory.create(anyInt())).thenReturn(mockCountDownLatch);

        mockDiscovery = mock(Discovery.class);
        final NodeInfo own = mock(NodeInfo.class);
        final PublicKey recipient = PublicKey.from("RECIPIENT".getBytes());
        final PublicKey otherRecipient = PublicKey.from("OTHERRECIPIENT".getBytes());
        recipients = List.of(recipient, otherRecipient);
        when(own.getUrl()).thenReturn("http://own.com/");
        when(own.getRecipientsAsMap())
                .thenReturn(Map.of(recipient, "http://url1.com/", otherRecipient, "http://url2.com/"));
        when(mockDiscovery.getCurrent()).thenReturn(own);

        mockPublisher = mock(RestPrivacyGroupPublisher.class);

        publisher =
                new AsyncRestPrivacyGroupPublisher(
                        mockExecutorFactory, mockCountDownLatchFactory, mockDiscovery, mockPublisher);
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

        publisher.publishPrivacyGroup(data, recipients);

        verify(mockCountDownLatchFactory).create(2);
        verify(mockExecutorFactory).createCachedThreadPool();
        verify(mockExecutor, times(2)).execute(any(Runnable.class));
        verify(mockCountDownLatch).await();
    }

    @Test
    public void publishInterruptedException() throws InterruptedException {

        InterruptedException cause = new InterruptedException("some exception");

        doThrow(cause).when(mockCountDownLatch).await();

        Throwable ex = catchThrowable(() -> publisher.publishPrivacyGroup(new byte[5], recipients));
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

        final AsyncRestPrivacyGroupPublisher publisher =
                new AsyncRestPrivacyGroupPublisher(
                        mockExecutorFactory, mockCountDownLatchFactory, mockDiscovery, mockPublisher);

        doAnswer(
                        invocation -> {
                            // sleep main thread so publish threads can work
                            Thread.sleep(200);
                            return null;
                        })
                .when(mockCountDownLatch)
                .await();

        final byte[] data = new byte[5];

        publisher.publishPrivacyGroup(data, recipients);

        verify(mockPublisher).publish(eq(data), eq("http://url1.com/"));
        verify(mockPublisher).publish(eq(data), eq("http://url2.com/"));

        verify(mockExecutorFactory, times(2)).createCachedThreadPool();
        verify(mockCountDownLatchFactory).create(2);

        verify(mockCountDownLatch, times(2)).countDown();
        verify(mockCountDownLatch).await();
    }

    @Test
    public void publishReturnsError() throws InterruptedException {

        final Executor realExecutor = Executors.newSingleThreadExecutor();
        when(mockExecutorFactory.createCachedThreadPool()).thenReturn(realExecutor);

        final AsyncRestPrivacyGroupPublisher publisher =
                new AsyncRestPrivacyGroupPublisher(
                        mockExecutorFactory, mockCountDownLatchFactory, mockDiscovery, mockPublisher);

        final byte[] data = new byte[5];

        doThrow(new PrivacyGroupPublishException("OUCH")).when(mockPublisher).publish(eq(data), eq("http://url2.com/"));

        doAnswer(
                        invocation -> {
                            // sleep main thread so publish threads can work
                            Thread.sleep(200);
                            return null;
                        })
                .when(mockCountDownLatch)
                .await();

        publisher.publishPrivacyGroup(data, recipients);

        verify(mockCountDownLatch).cancelWithException(any());
    }
}
