module tessera.tessera.partyinfo.main {
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires swagger.annotations;
    requires tessera.config.main;
    requires tessera.enclave.enclave.api.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.shared.main;
    requires tessera.tessera.context.main;

    exports com.quorum.tessera.partyinfo;
    exports com.quorum.tessera.partyinfo.model;
    exports com.quorum.tessera.sync;
}
