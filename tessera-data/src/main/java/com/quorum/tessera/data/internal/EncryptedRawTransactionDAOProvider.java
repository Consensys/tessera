package com.quorum.tessera.data.internal;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.data.DataSourceFactory;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptedRawTransactionDAOProvider {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(EncryptedTransactionDAOProvider.class);

  public static EncryptedRawTransactionDAO provider() {

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
        config.getJdbcConfig().isAutoCreateTables() ? "create" : "none");

    LOGGER.debug("Creating EntityManagerFactory from {}", properties);
    final EntityManagerFactory entityManagerFactory =
        Persistence.createEntityManagerFactory("tessera", properties);
    LOGGER.debug("Created EntityManagerFactory from {}", properties);

    return new EncryptedRawTransactionDAOImpl(entityManagerFactory);
  }
}
