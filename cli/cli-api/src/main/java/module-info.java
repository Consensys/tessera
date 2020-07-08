module tessera.cli.cli.api.main {
    requires java.management;
    requires info.picocli;
    requires org.slf4j;
    requires tessera.config.main;
    requires tessera.shared.main;

    exports com.quorum.tessera.cli;
    exports com.quorum.tessera.cli.keypassresolver;
    exports com.quorum.tessera.cli.parsers;
}
