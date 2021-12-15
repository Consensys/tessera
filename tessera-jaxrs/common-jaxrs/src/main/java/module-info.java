module tessera.common.jaxrs {
  requires jakarta.persistence;
  requires jakarta.validation;
  requires jakarta.ws.rs;
  requires jakarta.xml.bind;
  requires org.apache.commons.lang3;
  requires org.slf4j;
  requires tessera.config;
  requires tessera.enclave.api;
  requires tessera.encryption.api;
  requires tessera.shared;
  requires tessera.context;
  requires tessera.transaction;
  requires tessera.data;
  requires tessera.partyinfo;
  requires jakarta.json;
  // requires jakarta.servlet;
  // TODO: CHange for jakarta.servlet when jetty sort out the module name
  requires jetty.servlet.api;
  requires io.swagger.v3.oas.annotations;

  exports com.quorum.tessera.api;
  exports com.quorum.tessera.api.common;
  exports com.quorum.tessera.api.filter;
  exports com.quorum.tessera.app;
  exports com.quorum.tessera.api.constraint;

  opens com.quorum.tessera.api;
  //    to
  //      org.eclipse.persistence.moxy,
  //      org.hibernate.validator,
  //      org.eclipse.persistence.core;

  exports com.quorum.tessera.api.exception;
// to hk2.locator;

}
