package com.quorum.tessera.version;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MandatoryRecipientsVersionTest {

  private MandatoryRecipientsVersion version = new MandatoryRecipientsVersion();

  @Test
  public void getVersion() {
    assertThat(version.getVersion()).isEqualTo("4.0");
  }
}
