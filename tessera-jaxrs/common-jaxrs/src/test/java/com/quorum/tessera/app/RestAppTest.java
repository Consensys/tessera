package com.quorum.tessera.app;

import static org.assertj.core.api.Assertions.*;

import com.quorum.tessera.config.CommunicationType;
import org.junit.Before;
import org.junit.Test;

public class RestAppTest {

  private SampleApp sampleApp;

  @Before
  public void setUp() {
    sampleApp = new SampleApp();
  }

  @Test
  public void getClasses() {
    assertThat(sampleApp.getClasses()).isNotEmpty();
    assertThat(sampleApp.getAppType()).isNotNull();
    assertThat(sampleApp.getCommunicationType()).isEqualTo(CommunicationType.REST);
  }
}
