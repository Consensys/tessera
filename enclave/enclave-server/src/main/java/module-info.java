module tessera.enclave.enclave.server.main {
    requires info.picocli;
    requires org.slf4j;
    requires tessera.cli.cli.api.main;
    requires tessera.config.main;
    requires tessera.shared.main;

    exports com.quorum.tessera.enclave.server;

    opens com.quorum.tessera.enclave.server to info.picocli;

    uses com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver;

    provides com.quorum.tessera.cli.CliAdapter with
        com.quorum.tessera.enclave.server.EnclaveCliAdapter;
}
