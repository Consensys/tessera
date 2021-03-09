module tessera.cli.config.cli.main {

    requires java.management;
    requires java.validation;
    requires java.xml.bind;
    requires info.picocli;
    requires org.slf4j;
    requires tessera.cli.cli.api.main;
    requires tessera.config.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.key.generation.main;
    requires tessera.shared.main;

    uses com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver;
    uses com.quorum.tessera.passwords.PasswordReaderFactory;
    uses com.quorum.tessera.key.generation.KeyGeneratorFactory;
    uses com.quorum.tessera.config.cli.KeyDataMarshaller;

    opens com.quorum.tessera.config.cli to info.picocli;

    exports com.quorum.tessera.config.cli;

    provides com.quorum.tessera.config.cli.KeyDataMarshaller with
        com.quorum.tessera.config.cli.DefaultKeyDataMarshaller;

    provides com.quorum.tessera.config.cli.KeyVaultHandler
        with com.quorum.tessera.config.cli.DispatchingKeyVaultHandler;

}
