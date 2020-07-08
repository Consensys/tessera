module tessera.tessera.jaxrs.jaxrs.client.main {
    requires java.ws.rs;
    requires tessera.config.main;
    requires tessera.security.main;
    requires tessera.shared.main;
    requires tessera.tessera.context.main;

    exports com.quorum.tessera.jaxrs.client;
}
