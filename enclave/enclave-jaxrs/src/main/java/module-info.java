module tessera.enclave.enclave.jaxrs.main {
    requires java.json;
    requires java.ws.rs;
    requires java.xml.bind;
    requires info.picocli;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires tessera.cli.cli.api.main;
    requires tessera.config.main;
    requires tessera.enclave.enclave.api.main;
    requires tessera.enclave.enclave.server.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.server.jersey.server.main;
    requires tessera.server.server.api.main;
    requires tessera.shared.main;
    requires tessera.tessera.jaxrs.jaxrs.client.main;

    opens com.quorum.tessera.enclave.rest to org.eclipse.persistence.moxy,org.eclipse.persistence.core;

    exports com.quorum.tessera.enclave.rest to org.eclipse.persistence.core,hk2.locator,jersey.server;

    provides com.quorum.tessera.enclave.EnclaveClient
        with com.quorum.tessera.enclave.rest.EnclaveClientProvider;
}


