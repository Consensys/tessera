package com.quorum.tessera.data;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EntityManagerDAOFactoryTest {

    private EntityManagerDAOFactory entityManagerDAOFactory;

    private static boolean createTables = false;

    @Before
    public void createInstance() {

        createTables = createTables ? false : true;

        Config config = mock(Config.class);
        JdbcConfig jdbcConfig = mock(JdbcConfig.class);
        when(jdbcConfig.getUsername()).thenReturn("junit");
        when(jdbcConfig.getPassword()).thenReturn("junit");
        when(jdbcConfig.getUrl()).thenReturn("jdbc:h2:mem:junit");
        when(config.getJdbcConfig()).thenReturn(jdbcConfig);
        when(jdbcConfig.isAutoCreateTables()).thenReturn(createTables);
        entityManagerDAOFactory = EntityManagerDAOFactory.newFactory(config);
        assertThat(entityManagerDAOFactory).isNotNull();
    }

    @Test
    public void jasyptPasswordShouldBeDecrypted() {
        Config config = mock(Config.class);
        JdbcConfig jdbcConfig = mock(JdbcConfig.class);
        when(jdbcConfig.getUsername()).thenReturn("junit");
        when(jdbcConfig.getPassword()).thenReturn("ENC(KLa6pRQpxI8Ez3Bo6D3cI6y13YYdntu7)");
        when(jdbcConfig.getUrl()).thenReturn("jdbc:h2:mem:junit");
        when(jdbcConfig.isAutoCreateTables()).thenReturn(createTables);
        when(config.getJdbcConfig()).thenReturn(jdbcConfig);

        assertThatExceptionOfType(EncryptionOperationNotPossibleException.class)
            .isThrownBy(() -> {
                ByteArrayInputStream in = new ByteArrayInputStream(("bogus" + System.lineSeparator() + "bogus").getBytes());
                System.setIn(in);
                entityManagerDAOFactory = EntityManagerDAOFactory.newFactory(config);
            });
    }

    @Test
    public void createEncryptedRawTransactionDAO() {
        EncryptedRawTransactionDAO encryptedRawTransactionDAO =
            entityManagerDAOFactory.createEncryptedRawTransactionDAO();
        assertThat(encryptedRawTransactionDAO).isNotNull();
    }

    @Test
    public void createEncryptedTransactionDAO() {
        EncryptedTransactionDAO encryptedTransactionDAO = entityManagerDAOFactory.createEncryptedTransactionDAO();
        assertThat(encryptedTransactionDAO).isNotNull();
    }

    @Test
    public void createStagingEntityDAO() {
        StagingEntityDAO stagingEntityDAO = entityManagerDAOFactory.createStagingEntityDAO();
        assertThat(stagingEntityDAO).isNotNull();
    }

    @Test
    public void createPrivacyGroupDAO() {
        PrivacyGroupDAO privacyGroupDAO = entityManagerDAOFactory.createPrivacyGroupDAO();
        assertThat(privacyGroupDAO).isNotNull();
    }
}
