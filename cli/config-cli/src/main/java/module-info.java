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

    exports com.quorum.tessera.config.cli;
}
