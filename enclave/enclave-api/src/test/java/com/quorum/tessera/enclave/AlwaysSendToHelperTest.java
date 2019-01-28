package com.quorum.tessera.enclave;

import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class AlwaysSendToHelperTest {

    @Test
    public void createWithNullList() {
        AlwaysSendToHelper instance = new AlwaysSendToHelper(null);
        assertThat(instance.getAlwaysSendTo()).isNotNull().isEmpty();
    }

    @Test
    public void createWithPopulatedList() {
        AlwaysSendToHelper instance = new AlwaysSendToHelper(Arrays.asList("ONE", "TWO"));
        assertThat(instance.getAlwaysSendTo()).containsExactly("ONE", "TWO");
    }
}
