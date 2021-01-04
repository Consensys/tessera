package com.quorum.tessera.q2t;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.privacygroup.exception.PrivacyGroupPublishException;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;
import com.quorum.tessera.threading.CancellableCountDownLatch;
import com.quorum.tessera.threading.CancellableCountDownLatchFactory;
import com.quorum.tessera.threading.ExecutorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AsyncRestPrivacyGroupPublisher implements PrivacyGroupPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncRestPrivacyGroupPublisher.class);

    private final Executor executor;

    private final CancellableCountDownLatchFactory countDownLatchFactory;

    private final Discovery discovery;

    private final RestPrivacyGroupPublisher publisher;

    public AsyncRestPrivacyGroupPublisher(
            ExecutorFactory executorFactory,
            CancellableCountDownLatchFactory countDownLatchFactory,
            Discovery discovery,
            RestPrivacyGroupPublisher publisher) {
        this.publisher = publisher;
        this.executor = executorFactory.createCachedThreadPool();
        this.countDownLatchFactory = countDownLatchFactory;
        this.discovery = discovery;
    }

    @Override
    public void publishPrivacyGroup(byte[] data, List<PublicKey> recipientKeys) {

        if (recipientKeys.size() == 0) {
            return;
        }

        final NodeInfo ourNodeInfo = discovery.getCurrent();

        final Map<PublicKey, String> knownRecipients = ourNodeInfo.getRecipientsAsMap();

        final List<String> forwardingList =
                recipientKeys.stream()
                        .map(knownRecipients::get)
                        .filter(Predicate.not(ourNodeInfo.getUrl()::equals))
                        .collect(Collectors.toList());

        final CancellableCountDownLatch latch = countDownLatchFactory.create(forwardingList.size());

        forwardingList.forEach(
                targetUrl ->
                        executor.execute(
                                () -> {
                                    try {
                                        publisher.publish(data, targetUrl);
                                        latch.countDown();
                                    } catch (RuntimeException e) {
                                        LOGGER.info("Unable to publish privacy group: {}", e.getMessage());
                                        latch.cancelWithException(e);
                                    }
                                }));

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new PrivacyGroupPublishException(e.getMessage());
        }
    }
}
