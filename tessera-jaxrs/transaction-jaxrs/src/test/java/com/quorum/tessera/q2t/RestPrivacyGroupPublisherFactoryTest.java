package com.quorum.tessera.q2t;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public class RestPrivacyGroupPublisherFactoryTest {

  private RestPrivacyGroupPublisherFactory factory;

  @Before
  public void onSetUp() {
    factory = new RestPrivacyGroupPublisherFactory();
  }

  @Test
  public void create() {

    final Config config = new Config();
    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setCommunicationType(CommunicationType.REST);
    serverConfig.setApp(AppType.P2P);
    serverConfig.setServerAddress("http://someaddeess");
    config.setServerConfigs(Arrays.asList(serverConfig));

    PrivacyGroupPublisher privacyGroupPublisher = factory.create(config);
    assertThat(privacyGroupPublisher).isExactlyInstanceOf(RestPrivacyGroupPublisher.class);
  }
}
