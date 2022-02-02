package com.quorum.tessera.data.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import com.quorum.tessera.data.*;
import jakarta.persistence.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class EncryptedMessageDAOTest {
  private EntityManagerFactory entityManagerFactory;

  private EncryptedMessageDAO encryptedMessageDAO;

  private TestConfig testConfig;

  public EncryptedMessageDAOTest(TestConfig testConfig) {
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

  @Parameterized.Parameters(name = "DB {0}")
  public static Collection<TestConfig> connectionDetails() {
    return List.of(TestConfig.values());
  }

  @Test
  public void create() {
    try (var mockedServiceLoader = mockStatic(ServiceLoader.class)) {

      ServiceLoader serviceLoader = mock(ServiceLoader.class);
      when(serviceLoader.findFirst()).thenReturn(Optional.of(mock(EncryptedMessageDAO.class)));

      mockedServiceLoader
          .when(() -> ServiceLoader.load(EncryptedMessageDAO.class))
          .thenReturn(serviceLoader);

      EncryptedMessageDAO.create();

      mockedServiceLoader.verify(() -> ServiceLoader.load(EncryptedMessageDAO.class));
      mockedServiceLoader.verifyNoMoreInteractions();
      verify(serviceLoader).findFirst();
      verifyNoMoreInteractions(serviceLoader);
    }
  }

  @Test
  public void upcheckReturnsTrue() {
    assertThat(encryptedMessageDAO.upcheck());
  }

  @Test
  public void upcheckFailDueToDB() {
    EntityManagerFactory mockEntityManagerFactory = mock(EntityManagerFactory.class);
    EntityManager mockEntityManager = mock(EntityManager.class);
    EntityTransaction mockEntityTransaction = mock(EntityTransaction.class);
    EntityManagerCallback mockEntityManagerCallback = mock(EntityManagerCallback.class);

    when(mockEntityManagerFactory.createEntityManager()).thenReturn(mockEntityManager);
    when(mockEntityManager.getTransaction()).thenReturn(mockEntityTransaction);
    when(mockEntityManagerCallback.execute(mockEntityManager)).thenThrow(RuntimeException.class);

    EncryptedMessageDAO encryptedMessageDAO = new EncryptedMessageDAOImpl(mockEntityManagerFactory);

    assertThat(encryptedMessageDAO.upcheck()).isFalse();
  }
}
