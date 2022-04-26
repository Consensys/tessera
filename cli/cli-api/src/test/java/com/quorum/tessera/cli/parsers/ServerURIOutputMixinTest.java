package com.quorum.tessera.cli.parsers;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.Config;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Test;

public class ServerURIOutputMixinTest {

  private ServerURIOutputMixin serverURIOutputMixin;

  @Before
  public void beforeTest() {
    serverURIOutputMixin = new ServerURIOutputMixin();
  }

  @Test
  public void testConfigPathIsUpdatedWithSpecifiedPath() {
    final Path testPath = Path.of("/testPath");
    final Config config = new Config();
    assertThat(config.getOutputServerURIPath()).isNull();
    serverURIOutputMixin.updateConfig(testPath, config);
    assertThat(config.getOutputServerURIPath()).isEqualTo(testPath);
  }

  @Test
  public void testConfigPathStaysNullWithoutSpecifiedPath() {
    final Config config = new Config();
    assertThat(config.getOutputServerURIPath()).isNull();
    serverURIOutputMixin.updateConfig(null, config);
    assertThat(config.getOutputServerURIPath()).isNull();
  }
}
