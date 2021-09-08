module tessera.cli.config {
  requires java.management;
  requires jakarta.validation;
  requires jakarta.xml.bind;
  requires info.picocli;
  requires org.slf4j;
  requires tessera.cli.api;
  requires tessera.config;
  requires tessera.encryption.api;
  requires tessera.keygeneration;
  requires tessera.shared;

  uses com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver;
  uses com.quorum.tessera.passwords.PasswordReaderFactory;
  uses com.quorum.tessera.key.generation.KeyGeneratorFactory;
  uses com.quorum.tessera.config.cli.KeyDataMarshaller;

  opens com.quorum.tessera.config.cli to
      info.picocli;

  exports com.quorum.tessera.config.cli;

  provides com.quorum.tessera.config.cli.KeyDataMarshaller with
      com.quorum.tessera.config.cli.DefaultKeyDataMarshaller;
  provides com.quorum.tessera.config.cli.KeyVaultHandler with
      com.quorum.tessera.config.cli.DispatchingKeyVaultHandler;
}
