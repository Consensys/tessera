module tessera.enclave.enclave.server.main {
    requires info.picocli;
    requires org.slf4j;
    requires tessera.cli.cli.api.main;
    requires tessera.config.main;
    requires tessera.shared.main;

    exports com.quorum.tessera.enclave.server;
}
