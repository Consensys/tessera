module tessera.server.jersey {
  requires java.management;
  requires jakarta.ws.rs;
  requires jakarta.validation;
  requires org.eclipse.jetty.server;
  requires org.eclipse.jetty.servlet;
  requires org.slf4j;
  requires tessera.config;
  requires tessera.security;
  requires tessera.server.utils;
  requires tessera.server.api;
  requires tessera.shared;
  requires jakarta.inject;
  requires jersey.server;
  requires jersey.common;
  requires jersey.container.servlet.core;
  requires jul.to.slf4j;
  requires jakarta.annotation;
  requires jersey.bean.validation;
  requires jersey.hk2;
  requires java.net.http;
  requires org.glassfish.hk2.api;
  requires jakarta.mail;

  exports com.quorum.tessera.server.jersey;
  exports com.quorum.tessera.server.http;
  exports com.quorum.tessera.server.jaxrs to
      org.glassfish.hk2.api,
      org.glassfish.hk2.locator;
  exports com.quorum.tessera.server.monitoring to
      org.glassfish.hk2.api,
      org.glassfish.hk2.locator,
      jersey.server;

  opens com.quorum.tessera.server.jaxrs to
      org.glassfish.hk2.api,
      org.glassfish.hk2.locator,
      org.glassfish.hk2.utilities;

  provides com.quorum.tessera.server.TesseraServerFactory with
      com.quorum.tessera.server.jersey.JerseyServerFactory;
}
