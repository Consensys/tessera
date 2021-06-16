package com.quorum.tessera.encryption.nacl.jnacl;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.encryption.Encryptor;
import org.junit.Before;
import org.junit.Test;

public class JnaclFactoryTest {

  private JnaclFactory jnaclFactory;

  @Before
  public void setUp() {
    this.jnaclFactory = new JnaclFactory();
  }

  @Test
  public void createInstance() {
    final Encryptor result = jnaclFactory.create();

    assertThat(jnaclFactory.getType()).isEqualTo("NACL");
    assertThat(result).isNotNull().isExactlyInstanceOf(Jnacl.class);
  }
}
