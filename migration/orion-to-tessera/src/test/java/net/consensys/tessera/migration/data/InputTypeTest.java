package net.consensys.tessera.migration.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.Test;

public class InputTypeTest {

  @Test
  public void coverage() {

    Arrays.stream(InputType.values())
        .forEach(
            t -> {
              assertThat(t).isNotNull();
            });
  }
}
