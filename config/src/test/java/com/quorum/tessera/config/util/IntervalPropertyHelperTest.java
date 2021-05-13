package com.quorum.tessera.config.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class IntervalPropertyHelperTest {

  @Test
  public void testDefaultValues() {
    final IntervalPropertyHelper util = new IntervalPropertyHelper(Collections.emptyMap());

    assertThat(util.partyInfoInterval()).isEqualTo(5000);
    assertThat(util.enclaveKeySyncInterval()).isEqualTo(2000);
    assertThat(util.syncInterval()).isEqualTo(60000);
    assertThat(util.resendWaitTime()).isEqualTo("7200000");
  }

  @Test
  public void getValues() {
    final Map<String, String> props = new HashMap<>();
    props.put("partyInfoInterval", "2000");
    props.put("enclaveKeySyncInterval", "3000");
    props.put("syncInterval", "4000");
    props.put("resendWaitTime", "4000");

    final IntervalPropertyHelper util = new IntervalPropertyHelper(props);

    assertThat(util.partyInfoInterval()).isEqualTo(2000);
    assertThat(util.enclaveKeySyncInterval()).isEqualTo(3000);
    assertThat(util.syncInterval()).isEqualTo(4000);
    assertThat(util.resendWaitTime()).isEqualTo("4000");
  }

  @Test
  public void testExceptions() {
    final Map<String, String> props = new HashMap<>();
    props.put("partyInfoInterval", null);
    props.put("enclaveKeySyncInterval", "abc");
    props.put("syncInterval", "4000L");
    props.put("resendWaitTime", "4000L");

    final IntervalPropertyHelper util = new IntervalPropertyHelper(props);

    assertThat(util.partyInfoInterval()).isEqualTo(5000);
    assertThat(util.enclaveKeySyncInterval()).isEqualTo(2000);
    assertThat(util.syncInterval()).isEqualTo(60000);
    assertThat(util.resendWaitTime()).isEqualTo("7200000");
  }
}
