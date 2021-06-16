package com.quorum.tessera.p2p.resend;

import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import java.util.ServiceLoader;

/**
 * A client that can be used to make resend requests to other nodes. It cannot make requests to
 * other endpoints and may have different timeouts than P2P clients.
 */
public interface ResendClient {

  boolean makeResendRequest(String targetUrl, ResendRequest request);

  static ResendClient create() {
    return ServiceLoaderUtil.loadSingle(ServiceLoader.load(ResendClient.class));
  }
}
