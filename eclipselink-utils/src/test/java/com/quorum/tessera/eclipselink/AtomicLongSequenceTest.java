package com.quorum.tessera.eclipselink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AtomicLongSequenceTest {

  private EntityManagerFactory entityManagerFactory;

  @Before
  public void init() throws Exception {

    Map properties = new HashMap();
    properties.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:test");
    properties.put("jakarta.persistence.jdbc.user", "junit");
    properties.put("jakarta.persistence.jdbc.password", "");
    properties.put(
        "eclipselink.logging.logger", "org.eclipse.persistence.logging.slf4j.SLF4JLogger");
    properties.put("eclipselink.logging.level", "FINE");
    properties.put("eclipselink.logging.parameters", "true");
    properties.put("eclipselink.logging.level.sql", "FINE");
    properties.put("jakarta.persistence.schema-generation.database.action", "drop-and-create");
    properties.put("eclipselink.cache.shared.default", "false");
    properties.put("eclipselink.session.customizer", AtomicLongSequence.class.getName());

    entityManagerFactory = Persistence.createEntityManagerFactory("mypu", properties);
  }

  @After
  public void onTearDown() {}

  @Test
  public void saveEntity() {
    SomeEntity someEntity = new SomeEntity();
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.persist(someEntity);

    assertThat(someEntity.getId()).isNotNull();

    SomeEntity anotherEntity = new SomeEntity();
    entityManager.persist(anotherEntity);
    assertThat(anotherEntity.getId()).isNotNull().isNotEqualTo(someEntity.getId());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getGeneratedVectorIsNotSupported() {
    AtomicLongSequence atomicLongSequence = new AtomicLongSequence();
    atomicLongSequence.getGeneratedVector(null, null, anyString(), anyInt());
  }

  @Test
  public void onDisconnectDoesNothing() {
    AtomicLongSequence atomicLongSequence = spy(new AtomicLongSequence());
    atomicLongSequence.onDisconnect();
    verify(atomicLongSequence).onDisconnect();
    verifyNoMoreInteractions(atomicLongSequence);
  }

  @Test
  public void customisedWithNullDbLogin() throws Exception {
    Session session = mock(Session.class);

    AtomicLongSequence atomicLongSequence = new AtomicLongSequence();
    atomicLongSequence.customize(session);

    verify(session).getLogin();
    verifyNoMoreInteractions(session);
  }

  @Test
  public void customisedWithDbLogin() throws Exception {

    Session session = mock(Session.class);
    DatabaseLogin databaseLogin = mock(DatabaseLogin.class);
    when(session.getLogin()).thenReturn(databaseLogin);

    AtomicLongSequence atomicLongSequence = new AtomicLongSequence();
    atomicLongSequence.customize(session);

    verify(databaseLogin).addSequence(atomicLongSequence);

    verify(session).getLogin();
    verifyNoMoreInteractions(session, databaseLogin);
  }
}
