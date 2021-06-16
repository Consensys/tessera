package com.quorum.tessera.partyinfo;

import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import java.util.ServiceLoader;

public interface P2pClient {

  boolean sendPartyInfo(String targetUrl, byte[] data);

  static P2pClient create() {
    return ServiceLoaderUtil.loadSingle(ServiceLoader.load(P2pClient.class));
  }
}
