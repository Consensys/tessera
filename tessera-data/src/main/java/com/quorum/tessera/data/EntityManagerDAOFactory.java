package com.quorum.tessera.data;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.data.staging.StagingEntityDAOImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EntityManagerDAOFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityManagerDAOFactory.class);

    private final EntityManagerFactory entityManagerFactory;

    private final EntityManagerFactory stagingEntityManagerFactory;

    private EntityManagerDAOFactory(EntityManagerFactory entityManagerFactory,EntityManagerFactory stagingEntityManagerFactory) {
        this.entityManagerFactory = Objects.requireNonNull(entityManagerFactory);
        this.stagingEntityManagerFactory = Objects.requireNonNull(stagingEntityManagerFactory);

    }

    public static EntityManagerDAOFactory newFactory(Config config) {
        LOGGER.debug("New EntityManagerDAOFactory from {}",config);
        final String username = config.getJdbcConfig().getUsername();
        final String password = config.getJdbcConfig().getPassword();
        final String url = config.getJdbcConfig().getUrl();

        Map properties = new HashMap();
        properties.put("javax.persistence.jdbc.url",url);
        properties.put("javax.persistence.jdbc.user",username);
        properties.put("javax.persistence.jdbc.password",password);
        properties.put("eclipselink.logging.logger","org.eclipse.persistence.logging.slf4j.SLF4JLogger");
        properties.put("eclipselink.logging.level","FINE");
        properties.put("eclipselink.logging.parameters","true");
        properties.put("eclipselink.logging.level.sql","FINE");
        properties.put("javax.persistence.schema-generation.database.action",config.getJdbcConfig().isAutoCreateTables() ? "create" : "none");
       // properties.put("eclipselink.session.customizer","org.eclipse.persistence.sequencing.UUIDSequence");

        LOGGER.debug("Creating EntityManagerFactory from {}",properties);
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("tessera",properties);
        LOGGER.debug("Created EntityManagerFactory from {}",properties);


        EntityManagerFactory stagingEntityManagerFactory = Persistence.createEntityManagerFactory("tessera-recover",properties);

        return new EntityManagerDAOFactory(entityManagerFactory,stagingEntityManagerFactory);
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
