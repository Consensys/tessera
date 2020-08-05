module tessera.tessera.dist.main {
    requires java.validation;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires tessera.cli.cli.api.main;
    requires tessera.cli.config.cli.main;
    requires tessera.config.main;
    requires tessera.enclave.enclave.api.main;
    requires tessera.server.jersey.server.main;
    requires tessera.server.server.api.main;
    requires tessera.tessera.context.main;
    requires tessera.tessera.core.main;
    requires tessera.tessera.partyinfo.main;
    requires tessera.shared.main;
    requires tessera.tessera.jaxrs.sync.jaxrs.main;
    requires java.json;
}
