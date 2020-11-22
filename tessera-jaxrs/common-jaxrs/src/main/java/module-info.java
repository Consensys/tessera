module tessera.tessera.jaxrs.common.jaxrs.main {
    requires java.persistence;
    requires java.validation;
    requires java.ws.rs;
    requires java.xml.bind;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires io.swagger.v3.oas.annotations;
    requires tessera.config.main;
    requires tessera.enclave.enclave.api.main;
    requires tessera.encryption.encryption.api.main;
    requires tessera.shared.main;
    requires tessera.tessera.context.main;
    requires tessera.tessera.core.main;
    requires tessera.tessera.data.main;
    requires tessera.tessera.partyinfo.main;
    requires java.servlet;
    requires java.json;

    exports com.quorum.tessera.api;
    exports com.quorum.tessera.api.common;
    exports com.quorum.tessera.api.filter;
    exports com.quorum.tessera.app;
    exports com.quorum.tessera.api.constraint;

    opens com.quorum.tessera.api to org.eclipse.persistence.moxy,org.hibernate.validator,org.eclipse.persistence.core;

    exports com.quorum.tessera.api.exception to hk2.locator;

}
