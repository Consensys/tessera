package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveServer;
import jakarta.ws.rs.core.Application;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnclaveApplication extends Application
    implements com.quorum.tessera.config.apps.TesseraApp {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveApplication.class);

  private final Enclave enclave;

  public EnclaveApplication() {
    this(EnclaveServer.create());
  }

  public EnclaveApplication(Enclave enclave) {
    LOGGER.debug("Create EnclaveApplication with {}", enclave);
    this.enclave = Objects.requireNonNull(enclave);
  }

  @Override
  public Set<Object> getSingletons() {
    return Set.of(new EnclaveResource(enclave), new DefaultExceptionMapper());
  }

  @Override
  public AppType getAppType() {
    return AppType.ENCLAVE;
  }

  @Override
  public CommunicationType getCommunicationType() {
    return CommunicationType.REST;
  }
}
