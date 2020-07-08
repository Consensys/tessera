module tessera.tessera.context.main {
    requires java.validation;
    requires java.ws.rs;
    requires org.slf4j;
    requires tessera.config.main;
    requires tessera.enclave.enclave.api.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.shared.main;

    exports com.quorum.tessera.context;
}
