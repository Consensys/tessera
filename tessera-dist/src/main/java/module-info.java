module tessera.tessera.dist.main {

    exports com.quorum.tessera.launcher;

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
    requires tessera.tessera.recover.main;
    requires java.json;

    requires tessera.server.jaxrs.client.unixsocket.main;

//    requires tessera.encryption.encryption.jnacl.main;
//    requires tessera.encryption.encryption.api.main;

    uses com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver;
    uses com.quorum.tessera.p2p.resend.TransactionRequester;
    uses com.quorum.tessera.discovery.EnclaveKeySynchroniser;
    uses com.quorum.tessera.config.apps.TesseraApp;
    uses com.quorum.tessera.server.TesseraServerFactory;
    uses com.quorum.tessera.context.RuntimeContext;
    uses com.quorum.tessera.serviceloader.ServiceLoaderUtil;


}
