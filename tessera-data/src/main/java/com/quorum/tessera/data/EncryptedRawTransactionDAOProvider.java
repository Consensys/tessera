package com.quorum.tessera.data;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class EncryptedRawTransactionDAOProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedTransactionDAOProvider.class);

    public static EncryptedRawTransactionDAO provider() {

        Config config = ConfigFactory.create().getConfig();
        final DataSource dataSource = DataSourceFactory.create().create(config.getJdbcConfig());

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

        return new EncryptedRawTransactionDAOImpl(entityManagerFactory);
    }

}
