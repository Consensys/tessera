package com.quorum.tessera.data;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.JdbcConfig;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
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
    public void createEncryptedRawTransactionDAO() {
        EncryptedRawTransactionDAO encryptedRawTransactionDAO = entityManagerDAOFactory.createEncryptedRawTransactionDAO();
        assertThat(encryptedRawTransactionDAO).isNotNull();

    }

    @Test
    public void createEncryptedTransactionDAO() {
        EncryptedTransactionDAO encryptedTransactionDAO = entityManagerDAOFactory.createEncryptedTransactionDAO();
        assertThat(encryptedTransactionDAO).isNotNull();

    }


}
