module tessera.common.jaxrs {
  requires java.persistence;
  requires jakarta.validation;
  requires java.ws.rs;
  requires java.xml.bind;
  requires org.apache.commons.lang3;
  requires org.slf4j;
  requires io.swagger.v3.oas.annotations;
  requires tessera.config;
  requires tessera.enclave.api;
  requires tessera.encryption.api;
  requires tessera.shared;
  requires tessera.context;
  requires tessera.transaction;
  requires tessera.data;
  requires tessera.partyinfo;
  requires java.servlet;
  requires java.json;

  exports com.quorum.tessera.api;
  exports com.quorum.tessera.api.common;
  exports com.quorum.tessera.api.filter;
  exports com.quorum.tessera.app;
  exports com.quorum.tessera.api.constraint;

  opens com.quorum.tessera.api to
      org.eclipse.persistence.moxy,
      org.hibernate.validator,
      org.eclipse.persistence.core;

  exports com.quorum.tessera.api.exception to
      hk2.locator;
}
