package com.quorum.tessera.partyinfo.node;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Test;

public class VersionInfoTest {

  @Test
  public void create() {
    Set<String> versions = Set.of("v1", "v2");
    VersionInfo versionInfo = VersionInfo.from(versions);
    assertThat(versionInfo).isNotNull();
    assertThat(versionInfo.supportedApiVersions()).isEqualTo(versions);
  }
}
