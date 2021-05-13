package com.quorum.tessera.cli;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.io.NoopSystemAdapter;
import com.quorum.tessera.io.SystemAdapter;
import org.junit.Test;

public class CliAdapterTest {

  @Test
  public void sys() {
    CliAdapter cliAdapter = new MockCliAdapter();
    SystemAdapter systemAdapter = cliAdapter.sys();

    assertThat(systemAdapter).isExactlyInstanceOf(NoopSystemAdapter.class);
  }
}
