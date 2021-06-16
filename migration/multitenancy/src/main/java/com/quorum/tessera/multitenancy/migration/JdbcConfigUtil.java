package com.quorum.tessera.multitenancy.migration;

import com.quorum.tessera.config.JdbcConfig;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public interface JdbcConfigUtil {

  static EntityManagerFactory entityManagerFactory(JdbcConfig jdbcConfig) {
    return Persistence.createEntityManagerFactory("tessera", toMap(jdbcConfig));
  }

  static Map toMap(JdbcConfig jdbcConfig) {
    return Map.of(
        "javax.persistence.jdbc.url", jdbcConfig.getUrl(),
        "javax.persistence.jdbc.user", jdbcConfig.getUsername(),
        "javax.persistence.jdbc.password", jdbcConfig.getPassword());
  }
}
