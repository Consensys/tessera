package com.quorum.tessera.data.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

import com.quorum.tessera.data.EntityManagerTemplate;
import com.quorum.tessera.data.PrivacyGroupDAO;
import com.quorum.tessera.data.PrivacyGroupEntity;
import com.quorum.tessera.data.TestConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class PrivacyGroupDAOTest {

  private PrivacyGroupDAO privacyGroupDAO;

  private TestConfig testConfig;

  private static final ThreadLocal<EntityManager> ENTITY_MANAGER = new ThreadLocal<>();

  public PrivacyGroupDAOTest(TestConfig testConfig) {
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
    properties.put("eclipselink.logging.level.sql", "FINE");
    properties.put("eclipselink.cache.shared.default", "false");
    properties.put("jakarta.persistence.schema-generation.database.action", "create");

    EntityManagerFactory entityManagerFactory =
        Persistence.createEntityManagerFactory("tessera", properties);
    privacyGroupDAO = new PrivacyGroupDAOImpl(entityManagerFactory);
    ENTITY_MANAGER.set(entityManagerFactory.createEntityManager());
  }

  @After
  public void onTearDown() {
    EntityManager entityManager = ENTITY_MANAGER.get();
    entityManager.getTransaction().begin();
    entityManager.createQuery("delete from PrivacyGroupEntity ").executeUpdate();
    entityManager.getTransaction().commit();
    ENTITY_MANAGER.remove();
  }

  @Test
  public void saveDoesNotAllowNullId() {
    PrivacyGroupEntity privacyGroup = new PrivacyGroupEntity();

    try {
      privacyGroupDAO.save(privacyGroup);
      failBecauseExceptionWasNotThrown(PersistenceException.class);
    } catch (PersistenceException ex) {
      String expectedMessage = String.format(testConfig.getRequiredFieldColumnTemplate(), "ID");

      assertThat(ex)
          .isInstanceOf(PersistenceException.class)
          .hasMessageContaining(expectedMessage)
          .hasMessageContaining("ID");
    }
  }

  @Test
  public void saveDoesNotAllowNullData() {
    PrivacyGroupEntity privacyGroup = new PrivacyGroupEntity();
    privacyGroup.setId("id".getBytes());

    try {
      privacyGroupDAO.save(privacyGroup);
      failBecauseExceptionWasNotThrown(PersistenceException.class);
    } catch (PersistenceException ex) {
      String expectedMessage = String.format(testConfig.getRequiredFieldColumnTemplate(), "DATA");

      assertThat(ex)
          .isInstanceOf(PersistenceException.class)
          .hasMessageContaining(expectedMessage)
          .hasMessageContaining("DATA");
    }
  }

  @Test
  public void saveDuplicateIdThrowException() {
    PrivacyGroupEntity entity =
        new PrivacyGroupEntity("id".getBytes(), "lookup".getBytes(), "data".getBytes());

    privacyGroupDAO.save(entity);

    try {
      privacyGroupDAO.save(entity);
      failBecauseExceptionWasNotThrown(PersistenceException.class);
    } catch (PersistenceException ex) {
      assertThat(ex)
          .isInstanceOf(PersistenceException.class)
          .hasMessageContaining(testConfig.getUniqueConstraintViolationMessage());
    }
  }

  @Test
  public void saveAndRetrieve() {
    PrivacyGroupEntity entity =
        new PrivacyGroupEntity("id".getBytes(), "lookup".getBytes(), "data".getBytes());
    privacyGroupDAO.save(entity);

    Optional<PrivacyGroupEntity> retrieved = privacyGroupDAO.retrieve("id".getBytes());

    assertThat(retrieved).isPresent();
    assertThat(retrieved.get().getId()).isEqualTo("id".getBytes());
    assertThat(retrieved.get().getLookupId()).isEqualTo("lookup".getBytes());
    assertThat(retrieved.get().getData()).isEqualTo("data".getBytes());
  }

  @Test
  public void saveAndUpdate() {
    PrivacyGroupEntity entity =
        new PrivacyGroupEntity("id".getBytes(), "lookup".getBytes(), "data".getBytes());
    privacyGroupDAO.save(entity);

    entity.setData("newData".getBytes());

    privacyGroupDAO.update(entity);

    Optional<PrivacyGroupEntity> retrieved = privacyGroupDAO.retrieve("id".getBytes());

    assertThat(retrieved).isPresent();
    assertThat(retrieved.get().getId()).isEqualTo("id".getBytes());
    assertThat(retrieved.get().getLookupId()).isEqualTo("lookup".getBytes());
    assertThat(retrieved.get().getData()).isEqualTo("newData".getBytes());
  }

  @Test
  public void saveAndFindByLookupId() {
    final List<PrivacyGroupEntity> shouldBeEmpty =
        privacyGroupDAO.findByLookupId("lookup".getBytes());
    assertThat(shouldBeEmpty).isEmpty();

    final PrivacyGroupEntity entity =
        new PrivacyGroupEntity("id1".getBytes(), "lookup".getBytes(), "data".getBytes());
    privacyGroupDAO.save(entity);
    final PrivacyGroupEntity another =
        new PrivacyGroupEntity("id2".getBytes(), "lookup".getBytes(), "data".getBytes());
    privacyGroupDAO.save(another);

    final List<PrivacyGroupEntity> pgs = privacyGroupDAO.findByLookupId("lookup".getBytes());
    assertThat(pgs).isNotEmpty();
    assertThat(pgs).containsExactlyInAnyOrder(entity, another);
  }

  @Test
  public void saveAndFindAll() {
    final List<PrivacyGroupEntity> shouldBeEmpty = privacyGroupDAO.findAll();
    assertThat(shouldBeEmpty).isEmpty();

    final PrivacyGroupEntity entity =
        new PrivacyGroupEntity("id1".getBytes(), "lookup".getBytes(), "data".getBytes());
    privacyGroupDAO.save(entity);
    final PrivacyGroupEntity another =
        new PrivacyGroupEntity("id2".getBytes(), "lookup".getBytes(), "data".getBytes());
    privacyGroupDAO.save(another);

    final List<PrivacyGroupEntity> pgs = privacyGroupDAO.findAll();
    assertThat(pgs).isNotEmpty();
    assertThat(pgs).containsExactlyInAnyOrder(entity, another);
  }

  @Test
  public void savePrivacyGroupWithCallback() throws Exception {
    final PrivacyGroupEntity entity =
        new PrivacyGroupEntity("id".getBytes(), "lookup".getBytes(), "data".getBytes());

    Callable<Void> callback = mock(Callable.class);

    privacyGroupDAO.save(entity, callback);

    EntityManager entityManager = ENTITY_MANAGER.get();
    final PrivacyGroupEntity result = entityManager.find(PrivacyGroupEntity.class, "id".getBytes());
    assertThat(result).isNotNull();

    verify(callback).call();
  }

  @Test
  public void savePrivacyGroupWithCallbackException() throws Exception {
    final PrivacyGroupEntity entity =
        new PrivacyGroupEntity("id".getBytes(), "lookup".getBytes(), "data".getBytes());

    Callable<Void> callback = mock(Callable.class);
    when(callback.call()).thenThrow(new Exception("OUCH"));

    try {
      privacyGroupDAO.save(entity, callback);
      failBecauseExceptionWasNotThrown(PersistenceException.class);
    } catch (PersistenceException ex) {
      assertThat(ex).isNotNull().hasMessageContaining("OUCH");
    }

    EntityManager entityManager = ENTITY_MANAGER.get();
    final PrivacyGroupEntity result = entityManager.find(PrivacyGroupEntity.class, "id".getBytes());
    assertThat(result).isNull();

    verify(callback).call();
  }

  @Test
  public void savePrivacyGroupWithRuntimeException() throws Exception {
    final PrivacyGroupEntity entity =
        new PrivacyGroupEntity("id".getBytes(), "lookup".getBytes(), "data".getBytes());

    Callable<Void> callback = mock(Callable.class);
    when(callback.call()).thenThrow(new RuntimeException("OUCH"));

    try {
      privacyGroupDAO.save(entity, callback);
      failBecauseExceptionWasNotThrown(RuntimeException.class);
    } catch (RuntimeException ex) {
      assertThat(ex).isNotNull().hasMessageContaining("OUCH");
    }

    EntityManager entityManager = ENTITY_MANAGER.get();
    final PrivacyGroupEntity result = entityManager.find(PrivacyGroupEntity.class, "id".getBytes());
    assertThat(result).isNull();

    verify(callback).call();
  }

  @Test
  public void updatePrivacyGroupWithCallback() throws Exception {
    final PrivacyGroupEntity entity =
        new PrivacyGroupEntity("id".getBytes(), "lookup".getBytes(), "data".getBytes());

    Callable<Void> callback = mock(Callable.class);

    privacyGroupDAO.update(entity, callback);

    EntityManager entityManager = ENTITY_MANAGER.get();
    final PrivacyGroupEntity result = entityManager.find(PrivacyGroupEntity.class, "id".getBytes());
    assertThat(result).isNotNull();

    verify(callback).call();
  }

  @Test
  public void updatePrivacyGroupWithCallbackException() throws Exception {
    final PrivacyGroupEntity entity =
        new PrivacyGroupEntity("id".getBytes(), "lookup".getBytes(), "data".getBytes());

    Callable<Void> callback = mock(Callable.class);
    when(callback.call()).thenThrow(new Exception("OUCH"));

    try {
      privacyGroupDAO.update(entity, callback);
      failBecauseExceptionWasNotThrown(PersistenceException.class);
    } catch (PersistenceException ex) {
      assertThat(ex).isNotNull().hasMessageContaining("OUCH");
    }

    EntityManager entityManager = ENTITY_MANAGER.get();
    final PrivacyGroupEntity result = entityManager.find(PrivacyGroupEntity.class, "id".getBytes());
    assertThat(result).isNull();

    verify(callback).call();
  }

  @Test
  public void updatePrivacyGroupWithRuntimeException() throws Exception {
    final PrivacyGroupEntity entity =
        new PrivacyGroupEntity("id".getBytes(), "lookup".getBytes(), "data".getBytes());

    Callable<Void> callback = mock(Callable.class);
    when(callback.call()).thenThrow(new RuntimeException("OUCH"));

    try {
      privacyGroupDAO.update(entity, callback);
      failBecauseExceptionWasNotThrown(RuntimeException.class);
    } catch (RuntimeException ex) {
      assertThat(ex).isNotNull().hasMessageContaining("OUCH");
    }

    EntityManager entityManager = ENTITY_MANAGER.get();
    final PrivacyGroupEntity result = entityManager.find(PrivacyGroupEntity.class, "id".getBytes());
    assertThat(result).isNull();

    verify(callback).call();
  }

  @Test
  public void retrieveOrSave() {
    final List<PrivacyGroupEntity> shouldBeEmpty =
        privacyGroupDAO.findByLookupId("lookup".getBytes());
    assertThat(shouldBeEmpty).isEmpty();

    final PrivacyGroupEntity entity =
        new PrivacyGroupEntity("id".getBytes(), "lookup".getBytes(), "data".getBytes());

    privacyGroupDAO.retrieveOrSave(entity);

    Optional<PrivacyGroupEntity> retrieved = privacyGroupDAO.retrieve("id".getBytes());
    assertThat(retrieved).isPresent();
  }

  @Test
  public void retrieveOrSaveExistedEntity() {

    final PrivacyGroupEntity entity =
        new PrivacyGroupEntity("id".getBytes(), "lookup".getBytes(), "data".getBytes());
    privacyGroupDAO.save(entity);

    final Optional<PrivacyGroupEntity> existed = privacyGroupDAO.retrieve("id".getBytes());

    assertThat(existed).isPresent();

    privacyGroupDAO.retrieveOrSave(entity);
  }

  @Test
  public void concurrentSavesExceptionIgnoredIfCausedByDuplicate() throws InterruptedException {

    final List<PrivacyGroupEntity> shouldBeEmpty =
        privacyGroupDAO.findByLookupId("lookup".getBytes());
    assertThat(shouldBeEmpty).isEmpty();

    ExecutorService executor = Executors.newCachedThreadPool();

    final PrivacyGroupEntity entity =
        new PrivacyGroupEntity("id".getBytes(), "lookup".getBytes(), "data".getBytes());

    final Callable<PrivacyGroupEntity> create = () -> privacyGroupDAO.retrieveOrSave(entity);
    List<Callable<PrivacyGroupEntity>> callables =
        Stream.generate(() -> create).limit(3).collect(Collectors.toList());

    executor.invokeAll(callables).stream()
        .map(
            future -> {
              try {
                return future.get();
              } catch (Exception e) {
                throw new IllegalStateException(e);
              }
            })
        .forEach(System.out::println);
  }

  @Test(expected = PersistenceException.class)
  public void retrieveOrSaveValidError() {
    final PrivacyGroupEntity entity =
        new PrivacyGroupEntity(null, "lookup".getBytes(), "data".getBytes());
    privacyGroupDAO.retrieveOrSave(entity);
  }

  @Test(expected = IllegalStateException.class)
  public void retrieveOrSaveThrows() {

    EntityManagerTemplate template =
        new EntityManagerTemplate(ENTITY_MANAGER.get().getEntityManagerFactory());

    Supplier<PrivacyGroupEntity> mockRetriever = mock(Supplier.class);
    when(mockRetriever.get()).thenReturn(null);

    Supplier<PrivacyGroupEntity> mockFactory = mock(Supplier.class);
    when(mockFactory.get()).thenThrow(new IllegalStateException("OUCH"));

    template.retrieveOrSave(mockRetriever, mockFactory);
  }

  @Test
  public void create() {
    try (var mockedServiceLoader = mockStatic(ServiceLoader.class)) {

      ServiceLoader serviceLoader = mock(ServiceLoader.class);
      when(serviceLoader.findFirst()).thenReturn(Optional.of(mock(PrivacyGroupDAO.class)));

      mockedServiceLoader
          .when(() -> ServiceLoader.load(PrivacyGroupDAO.class))
          .thenReturn(serviceLoader);

      PrivacyGroupDAO.create();

      mockedServiceLoader.verify(() -> ServiceLoader.load(PrivacyGroupDAO.class));
      mockedServiceLoader.verifyNoMoreInteractions();
      verify(serviceLoader).findFirst();
      verifyNoMoreInteractions(serviceLoader);
    }
  }

  @Parameterized.Parameters(name = "DB {0}")
  public static Collection<TestConfig> connectionDetails() {
    return List.of(TestConfig.values());
  }
}
