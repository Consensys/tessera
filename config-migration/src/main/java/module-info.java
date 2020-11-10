module tessera.config.migration.main {
    requires java.validation;
    requires info.picocli;
    requires org.slf4j;
    requires toml4j;
    requires tessera.cli.cli.api.main;
    requires tessera.config.main;
    requires tessera.shared.main;

    provides com.quorum.tessera.cli.CliAdapter with
        com.quorum.tessera.config.migration.LegacyCliAdapter;
}
