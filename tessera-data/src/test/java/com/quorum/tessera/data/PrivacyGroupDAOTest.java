package com.quorum.tessera.data;

import com.quorum.tessera.data.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import java.util.*;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class PrivacyGroupDAOTest {

    private EntityManagerFactory entityManagerFactory;

    private PrivacyGroupDAO privacyGroupDAO;

    private TestConfig testConfig;

    public PrivacyGroupDAOTest(TestConfig testConfig) {
        this.testConfig = testConfig;
    }

    @Before
    public void onSetUp() {

        Map properties = new HashMap();
        properties.put("javax.persistence.jdbc.url", testConfig.getUrl());
        properties.put("javax.persistence.jdbc.user", "junit");
        properties.put("javax.persistence.jdbc.password", "");
        properties.put("eclipselink.logging.logger", "org.eclipse.persistence.logging.slf4j.SLF4JLogger");
        properties.put("eclipselink.logging.level", "FINE");
        properties.put("eclipselink.logging.parameters", "true");
        properties.put("eclipselink.logging.level.sql", "FINE");
        properties.put("eclipselink.cache.shared.default", "false");
        properties.put("javax.persistence.schema-generation.database.action", "create");

        entityManagerFactory = Persistence.createEntityManagerFactory("tessera", properties);
        privacyGroupDAO = new PrivacyGroupDAOImpl(entityManagerFactory);
    }

    @After
    public void onTearDown() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.createQuery("delete from PrivacyGroupEntity ").executeUpdate();
        entityManager.getTransaction().commit();
    }

    @Test
    public void saveDoesNotAllowNullId() {
        PrivacyGroupEntity privacyGroup = new PrivacyGroupEntity();

        try {
            privacyGroupDAO.save(privacyGroup);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        } catch (PersistenceException ex) {
            String expectedMessage = String.format(testConfig.getRequiredFieldColumTemplate(), "ID");

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
            String expectedMessage = String.format(testConfig.getRequiredFieldColumTemplate(), "DATA");

            assertThat(ex)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining(expectedMessage)
                    .hasMessageContaining("DATA");
        }
    }

    @Test
    public void saveDuplicateIdThrowException() {
        PrivacyGroupEntity entity = new PrivacyGroupEntity("id".getBytes(), "lookup".getBytes(), "data".getBytes());

        privacyGroupDAO.save(entity);

        try {
            privacyGroupDAO.save(entity);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        } catch (PersistenceException ex) {
            assertThat(ex)
                    .isInstanceOf(PersistenceException.class)
                    .hasMessageContaining(testConfig.getUniqueContraintViolationMessage());
        }
    }

    @Test
    public void saveAndRetrieve() {
        PrivacyGroupEntity entity = new PrivacyGroupEntity("id".getBytes(), "lookup".getBytes(), "data".getBytes());
        privacyGroupDAO.save(entity);

        Optional<PrivacyGroupEntity> retrieved = privacyGroupDAO.retrieve("id".getBytes());

        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getId()).isEqualTo("id".getBytes());
        assertThat(retrieved.get().getLookupId()).isEqualTo("lookup".getBytes());
        assertThat(retrieved.get().getData()).isEqualTo("data".getBytes());
    }

    @Test
    public void saveAndFindByLookupId() {
        final List<PrivacyGroupEntity> shouldBeEmpty = privacyGroupDAO.findByLookupId("lookup".getBytes());
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
    public void savePrivacyGroupWithCallback() throws Exception {
        final PrivacyGroupEntity entity =
                new PrivacyGroupEntity("id".getBytes(), "lookup".getBytes(), "data".getBytes());

        Callable<Void> callback = mock(Callable.class);

        privacyGroupDAO.save(entity, callback);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
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

        EntityManager entityManager = entityManagerFactory.createEntityManager();
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

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        final PrivacyGroupEntity result = entityManager.find(PrivacyGroupEntity.class, "id".getBytes());
        assertThat(result).isNull();

        verify(callback).call();
    }

    @Parameterized.Parameters(name = "DB {0}")
    public static Collection<TestConfig> connectionDetails() {
        return List.of(TestConfig.values());
    }
}
