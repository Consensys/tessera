package com.jpmorgan.quorum.encryption.ec;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.encryption.Encryptor;
import org.junit.Before;
import org.junit.Test;

public class EllipticalCurveEncryptorFactoryTest {

  private EllipticalCurveEncryptorFactory encryptorFactory;

  @Before
  public void setUp() {
    this.encryptorFactory = new EllipticalCurveEncryptorFactory();
  }

  @Test
  public void createInstance() {
    final Encryptor result = encryptorFactory.create();
    assertThat(encryptorFactory.getType()).isEqualTo("EC");
    assertThat(result).isNotNull().isExactlyInstanceOf(EllipticalCurveEncryptor.class);
  }
}
