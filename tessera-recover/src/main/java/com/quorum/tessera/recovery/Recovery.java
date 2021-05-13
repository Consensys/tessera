package com.quorum.tessera.recovery;

import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import java.util.ServiceLoader;

public interface Recovery {

  int recover();

  RecoveryResult request();

  RecoveryResult stage();

  RecoveryResult sync();

  static Recovery create() {
    return ServiceLoaderUtil.loadSingle(ServiceLoader.load(Recovery.class));
  }
}
