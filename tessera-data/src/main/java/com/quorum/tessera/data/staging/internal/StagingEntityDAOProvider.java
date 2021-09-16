package com.quorum.tessera.data.staging.internal;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.data.DataSourceFactory;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StagingEntityDAOProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(StagingEntityDAOProvider.class);

  public static StagingEntityDAO provider() {
    LOGGER.debug("Creating StagingEntityDAO");
    Config config = ConfigFactory.create().getConfig();

    final DataSource dataSource = DataSourceFactory.create().create(config.getJdbcConfig());

    Map properties = new HashMap();

    properties.put("jakarta.persistence.nonJtaDataSource", dataSource);

    properties.put(
        "eclipselink.logging.logger", "org.eclipse.persistence.logging.slf4j.SLF4JLogger");
    properties.put("eclipselink.logging.level", "FINE");
    properties.put("eclipselink.logging.parameters", "true");
    properties.put("eclipselink.logging.level.sql", "FINE");

    properties.put(
        "jakarta.persistence.schema-generation.database.action",
        config.getJdbcConfig().isAutoCreateTables() ? "drop-and-create" : "none");

    properties.put(
        "eclipselink.session.customizer", "com.quorum.tessera.eclipselink.AtomicLongSequence");

    LOGGER.debug("Creating EntityManagerFactory from {}", properties);
    final EntityManagerFactory entityManagerFactory =
        Persistence.createEntityManagerFactory("tessera-recover", properties);
    LOGGER.debug("Created EntityManagerFactory from {}", properties);

    StagingEntityDAO stagingEntityDAO = new StagingEntityDAOImpl(entityManagerFactory);
    LOGGER.debug("Created StagingEntityDAO {}", stagingEntityDAO);

    return stagingEntityDAO;
  }
}
