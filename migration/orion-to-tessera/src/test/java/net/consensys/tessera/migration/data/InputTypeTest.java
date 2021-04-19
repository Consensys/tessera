package net.consensys.tessera.migration.data;

import org.junit.Test;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class InputTypeTest {

    @Test
    public void coverage() {

        Arrays.stream(InputType.values()).forEach(t -> {
            assertThat(t).isNotNull();
        });
    }

}
