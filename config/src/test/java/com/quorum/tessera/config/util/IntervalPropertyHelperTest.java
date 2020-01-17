package com.quorum.tessera.config.util;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class IntervalPropertyHelperTest {

    @Test
    public void testDefaultValues() {
        IntervalPropertyHelper util = new IntervalPropertyHelper(Collections.emptyMap());
        Assertions.assertThat(util.partyInfoInterval()).isEqualTo(5000);
        Assertions.assertThat(util.enclaveKeySyncInterval()).isEqualTo(2000);
        Assertions.assertThat(util.syncInterval()).isEqualTo(60000);
    }

    @Test
    public void getValues() {
        Map<String, String> props = new HashMap<>();
        props.put("partyInfoInterval", "2000");
        props.put("enclaveKeySyncInterval", "3000");
        props.put("syncInterval", "4000");
        IntervalPropertyHelper util = new IntervalPropertyHelper(props);
        Assertions.assertThat(util.partyInfoInterval()).isEqualTo(2000);
        Assertions.assertThat(util.enclaveKeySyncInterval()).isEqualTo(3000);
        Assertions.assertThat(util.syncInterval()).isEqualTo(4000);
    }

    @Test
    public void testExceptions() {
        Map<String, String> props = new HashMap<>();
        props.put("partyInfoInterval", null);
        props.put("enclaveKeySyncInterval", "abc");
        props.put("syncInterval", "4000L");
        IntervalPropertyHelper util = new IntervalPropertyHelper(props);
        Assertions.assertThat(util.partyInfoInterval()).isEqualTo(5000);
        Assertions.assertThat(util.enclaveKeySyncInterval()).isEqualTo(2000);
        Assertions.assertThat(util.syncInterval()).isEqualTo(60000);
    }

}
