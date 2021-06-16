module tessera.cli.api {
  requires java.management;
  requires info.picocli;
  requires org.slf4j;
  requires tessera.config;
  requires tessera.shared;
  requires tessera.encryption.api;

  exports com.quorum.tessera.cli;
  exports com.quorum.tessera.cli.keypassresolver;
  exports com.quorum.tessera.cli.parsers;

  uses com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver;

  opens com.quorum.tessera.cli.parsers to
      info.picocli;
  opens com.quorum.tessera.cli to
      info.picocli;

  provides com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver with
      com.quorum.tessera.cli.keypassresolver.CliKeyPasswordResolver;
}
