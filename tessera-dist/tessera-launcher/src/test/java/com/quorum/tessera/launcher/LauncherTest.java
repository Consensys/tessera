package com.quorum.tessera.launcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.quorum.tessera.recovery.Recovery;
import com.quorum.tessera.server.TesseraServer;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class LauncherTest extends TestConfig {

  @Before
  public void init() {
    MockTesseraServerFactory.getInstance().clearHolder();
  }

  @Test
  public void testNormalLaunch() throws Exception {

    Launcher.NORMAL.launchServer(serverConfig());
    final List<TesseraServer> servers = MockTesseraServerFactory.getInstance().getHolder();
    assertThat(servers.size()).isEqualTo(3);

    servers.forEach(
        s -> {
          try {
            verify(s).start();
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
  }

  @Test
  public void testRecoveryLaunch() throws Exception {

    try {
      Launcher.RECOVERY.launchServer(serverConfig());
    } catch (Exception ex) {

      final List<TesseraServer> servers = MockTesseraServerFactory.getInstance().getHolder();

      assertThat(servers.size()).isEqualTo(1);

      verify(servers.get(0)).start();

      Recovery recoveryManager = MockRecoveryFactory.getInstance().getHolder().get(0);

      verify(recoveryManager).recover();
    }
  }

  @Test
  public void testInvalidLaunch() {
    try {
      Launcher.NORMAL.launchServer(invalidConfig());
    } catch (Exception ex) {
      assertThat(ex)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Cant create app for ADMIN");
    }
  }

  @Test
  public void testCreate() {

    assertThat(Launcher.create(true)).isEqualTo(Launcher.RECOVERY);
    assertThat(Launcher.create(false)).isEqualTo(Launcher.NORMAL);
  }
}
