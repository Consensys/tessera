package com.quorum.tessera.data;

import com.quorum.tessera.data.internal.EncryptedMessageDAOImpl;
import com.quorum.tessera.enclave.EncodedPayload;
import jakarta.persistence.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class EncryptedMessageTest {

  /*private EntityManagerFactory entityManagerFactory;

  private EncryptedMessageDAO encryptedMessageDAO;

  private TestConfig testConfig;

  public EncryptedMessageTest(TestConfig testConfig) {
    this.testConfig = testConfig;
  }

  @Before
  public void onSetUp() {

    Map properties = new HashMap();
    properties.put("jakarta.persistence.jdbc.url", testConfig.getUrl());
    properties.put("jakarta.persistence.jdbc.user", "junit");
    properties.put("jakarta.persistence.jdbc.password", "");
    properties.put(
      "eclipselink.logging.logger", "org.eclipse.persistence.logging.slf4j.SLF4JLogger");
    properties.put("eclipselink.logging.level", "FINE");
    properties.put("eclipselink.logging.parameters", "true");
    properties.put("eclipselink.logging.level.sql", "FINEST");
    properties.put("eclipselink.cache.shared.default", "false");
    properties.put("jakarta.persistence.schema-generation.database.action", "create");

    entityManagerFactory = Persistence.createEntityManagerFactory("tessera", properties);

    encryptedMessageDAO = new EncryptedMessageDAOImpl(entityManagerFactory);
  }

  @Test
  public void testSave() {

      EncryptedMessage encryptedMessage = new EncryptedMessage();
      encryptedMessage.setHash(new MessageHash(new byte[] {5}));
      encryptedMessage.setContent("test".getBytes());
      encryptedMessage.setTimestamp(System.currentTimeMillis());

      try {
        encryptedMessageDAO.save(encryptedMessage);
      } catch (PersistenceException ex) {
        assertThat(ex)
          .isInstanceOf(PersistenceException.class);
      }


    }

  @Parameterized.Parameters(name = "DB {0}")
  public static Collection<TestConfig> connectionDetails() {
    return List.of(TestConfig.values());
  }

  @Test
  public void testConstructorWithArgs(){
    EncryptedMessage encryptedMessage1 = new EncryptedMessage(new MessageHash("test".getBytes()),"test23".getBytes());
    assertThat(encryptedMessage1).isNotNull();


    EncryptedMessage encryptedMessage2 = new EncryptedMessage(new MessageHash("test".getBytes()),"test23".getBytes());
    assertThat(encryptedMessage2).isNotNull();

    assertThat(encryptedMessage1.equals(encryptedMessage2));
  }

  @Test
  public void testFields() {

    EncryptedMessage encryptedMessage = new EncryptedMessage();
    MessageHash messageHash = new MessageHash("test".getBytes());
    encryptedMessage.setHash(messageHash);
    encryptedMessage.setContent("test".getBytes());
    encryptedMessage.setTimestamp(System.currentTimeMillis());

    try {
      encryptedMessageDAO.save(encryptedMessage);
    } catch (PersistenceException ex) {
      assertThat(ex)
        .isInstanceOf(PersistenceException.class);
    }

    Optional<EncryptedMessage> optional = encryptedMessageDAO.retrieveByHash(messageHash);
    EncryptedMessage encryptedMessage1 = optional.get();
    assertThat(encryptedMessage1.getContent()).isNotNull();
    assertThat(encryptedMessage1.getTimestamp()).isNotNull();
  }*/
  @Test
  public void createInstance() {

    EncryptedMessage encryptedMessage = new EncryptedMessage();
    MessageHash hash = mock(MessageHash.class);
    encryptedMessage.setHash(hash);
    encryptedMessage.setTimestamp(System.currentTimeMillis());
    encryptedMessage.setContent("test".getBytes());
    encryptedMessage.onPersist();

    assertThat(encryptedMessage.getHash()).isSameAs(hash);
    assertThat(encryptedMessage.getContent()).isNotNull();
    assertThat(encryptedMessage.getTimestamp()).isNotNull();
  }

  @Test
  public void createInstanceWithConstructorArgs() {

    MessageHash hash = mock(MessageHash.class);
    EncryptedMessage encryptedMessage = new EncryptedMessage(hash,"test".getBytes());

    assertThat(encryptedMessage.getHash()).isSameAs(hash);
  }

  @Test
  public void subclassesEqual() {

    class OtherClass extends EncryptedMessage {}

    final OtherClass other = new OtherClass();
    final EncryptedMessage et = new EncryptedMessage();

    final boolean equal = Objects.equals(et, other);

    assertThat(equal).isTrue();
  }

  @Test
  public void differentClassesNotEqual() {

    final Object other = "OTHER";
    final EncryptedMessage et = new EncryptedMessage();

    final boolean equal = Objects.equals(et, other);

    assertThat(equal).isFalse();
  }

  @Test
  public void sameObjectHasSameHash() {

    final EncryptedMessage et = new EncryptedMessage();

    assertThat(et.hashCode()).isEqualTo(et.hashCode());
  }
}


