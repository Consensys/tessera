package com.quorum.tessera.enclave;

import com.quorum.tessera.service.Service;
import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import java.util.ServiceLoader;

public interface EnclaveServer extends Enclave, Service {

  static EnclaveServer create() {
    return ServiceLoaderUtil.loadSingle(ServiceLoader.load(EnclaveServer.class));
  }
}
