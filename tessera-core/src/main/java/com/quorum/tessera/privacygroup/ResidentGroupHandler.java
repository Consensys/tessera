package com.quorum.tessera.privacygroup;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import java.util.ServiceLoader;

public interface ResidentGroupHandler {

  void onCreate(Config config);

  static ResidentGroupHandler create() {
    return ServiceLoaderUtil.loadSingle(ServiceLoader.load(ResidentGroupHandler.class));
  }
}
