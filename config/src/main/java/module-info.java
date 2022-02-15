import com.quorum.tessera.config.internal.ConfigFactoryProvider;

open module tessera.config {
  requires jakarta.validation;
  requires java.xml;
  requires java.xml.bind;
  requires jasypt;
  requires org.apache.commons.lang3;
  requires org.slf4j;
  requires tessera.argontwo;
  requires tessera.encryption.api;
  requires tessera.shared;

  exports com.quorum.tessera.config;
  exports com.quorum.tessera.config.apps;
  exports com.quorum.tessera.config.keypairs;
  exports com.quorum.tessera.config.keys;
  exports com.quorum.tessera.config.util;
  exports com.quorum.tessera.config.adapters;
  exports com.quorum.tessera.config.constraints;

  uses com.quorum.tessera.config.util.EnvironmentVariableProviderFactory;
  uses com.quorum.tessera.config.ConfigFactory;

  provides com.quorum.tessera.config.util.EnvironmentVariableProviderFactory with
      com.quorum.tessera.config.util.EnvironmentVariableProviderFactoryImpl;
  provides com.quorum.tessera.config.ConfigFactory with
      ConfigFactoryProvider;
}
