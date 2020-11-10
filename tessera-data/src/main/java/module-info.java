open module tessera.tessera.data.main {
    requires java.instrument;

    requires java.persistence;
    requires org.bouncycastle.provider;
    requires org.slf4j;
    requires tessera.config.main;
    requires tessera.enclave.enclave.api.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.shared.main;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires java.validation;
//    opens com.quorum.tessera.data to org.eclipse.persistence.core;
//    opens com.quorum.tessera.data.staging to org.eclipse.persistence.core;

    exports com.quorum.tessera.data;
    exports com.quorum.tessera.data.staging;

    uses com.quorum.tessera.data.MessageHashFactory;

}
