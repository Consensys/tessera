package com.quorum.tessera.q2t;

import com.quorum.tessera.privacygroup.publish.BatchPrivacyGroupPublisher;
import com.quorum.tessera.privacygroup.publish.BatchPrivacyGroupPublisherFactory;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AsyncBatchPrivacyGroupPublisherFactoryTest {

    @Test
    public void create() {

        BatchPrivacyGroupPublisherFactory factory = new AsyncBatchPrivacyGroupPublisherFactory();
        BatchPrivacyGroupPublisher publisher = factory.create(mock(PrivacyGroupPublisher.class));

        assertThat(publisher).isNotNull();
    }
}
