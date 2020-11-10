package com.quorum.tessera.data;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.util.EncryptedStringResolver;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.data.staging.StagingEntityDAOImpl;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EntityManagerDAOFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityManagerDAOFactory.class);

    private final EntityManagerFactory entityManagerFactory;

    private final EntityManagerFactory stagingEntityManagerFactory;

    private EntityManagerDAOFactory(
            EntityManagerFactory entityManagerFactory, EntityManagerFactory stagingEntityManagerFactory) {
        this.entityManagerFactory = Objects.requireNonNull(entityManagerFactory);
        this.stagingEntityManagerFactory = Objects.requireNonNull(stagingEntityManagerFactory);
    }

    public static EntityManagerDAOFactory newFactory(Config config) {
        LOGGER.debug("New EntityManagerDAOFactory from {}", config);
        final String username = config.getJdbcConfig().getUsername();
        final String password = config.getJdbcConfig().getPassword();

        final EncryptedStringResolver resolver = new EncryptedStringResolver();
        final String url = resolver.resolve(config.getJdbcConfig().getUrl());

        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        final DataSource dataSource = new HikariDataSource(hikariConfig);

        Map properties = new HashMap();

        properties.put("javax.persistence.nonJtaDataSource", dataSource);

        properties.put("eclipselink.logging.logger", "org.eclipse.persistence.logging.slf4j.SLF4JLogger");
        properties.put("eclipselink.logging.level", "FINE");
        properties.put("eclipselink.logging.parameters", "true");
        properties.put("eclipselink.logging.level.sql", "FINE");

        properties.put(
                "javax.persistence.schema-generation.database.action",
                config.getJdbcConfig().isAutoCreateTables() ? "create" : "none");

        LOGGER.debug("Creating EntityManagerFactory from {}", properties);
        final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("tessera", properties);
        LOGGER.debug("Created EntityManagerFactory from {}", properties);

        final Map stagingProperties = new HashMap(properties);
        stagingProperties.put("eclipselink.session.customizer", "com.quorum.tessera.eclipselink.AtomicLongSequence");
        stagingProperties.put("javax.persistence.schema-generation.database.action", "drop-and-create");

        final EntityManagerFactory stagingEntityManagerFactory =
                Persistence.createEntityManagerFactory("tessera-recover", stagingProperties);

        return new EntityManagerDAOFactory(entityManagerFactory, stagingEntityManagerFactory);
    }

    public EncryptedTransactionDAO createEncryptedTransactionDAO() {
        LOGGER.debug("Create EncryptedTransactionDAO");
        return new EncryptedTransactionDAOImpl(entityManagerFactory);
    }

    public EncryptedRawTransactionDAO createEncryptedRawTransactionDAO() {
        LOGGER.debug("Create EncryptedRawTransactionDAO");
        return new EncryptedRawTransactionDAOImpl(entityManagerFactory);
    }

    public StagingEntityDAO createStagingEntityDAO() {
        LOGGER.debug("Create StagingEntityDAO");
        return new StagingEntityDAOImpl(stagingEntityManagerFactory);
    }
}
