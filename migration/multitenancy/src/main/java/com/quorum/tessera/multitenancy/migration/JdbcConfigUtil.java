package com.quorum.tessera.multitenancy.migration;

import com.quorum.tessera.config.JdbcConfig;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.Map;

public interface JdbcConfigUtil {

  static EntityManagerFactory entityManagerFactory(JdbcConfig jdbcConfig) {
    return Persistence.createEntityManagerFactory("tessera", toMap(jdbcConfig));
  }

  static Map toMap(JdbcConfig jdbcConfig) {
    return Map.of(
        "jakarta.persistence.jdbc.url", jdbcConfig.getUrl(),
        "jakarta.persistence.jdbc.user", jdbcConfig.getUsername(),
        "jakarta.persistence.jdbc.password", jdbcConfig.getPassword());
  }
}
