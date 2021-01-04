package com.quorum.tessera.q2t;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisherFactory;
import com.quorum.tessera.threading.CancellableCountDownLatchFactory;
import com.quorum.tessera.threading.ExecutorFactory;

import javax.ws.rs.client.Client;

public class AsyncRestPrivacyGroupPublisherFactory implements PrivacyGroupPublisherFactory {

    @Override
    public PrivacyGroupPublisher create(Config config) {
        ExecutorFactory executorFactory = new ExecutorFactory();
        CancellableCountDownLatchFactory countDownLatchFactory = new CancellableCountDownLatchFactory();

        Discovery discovery = Discovery.getInstance();

        Client client = new ClientFactory().buildFrom(config.getP2PServerConfig());
        RestPrivacyGroupPublisher publisher = new RestPrivacyGroupPublisher(client);

        return new AsyncRestPrivacyGroupPublisher(executorFactory, countDownLatchFactory, discovery, publisher);
    }
}
