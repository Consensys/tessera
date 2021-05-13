package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;
import com.quorum.tessera.recovery.resend.ResendBatchPublisherFactory;

public class RestResendBatchPublisherFactory implements ResendBatchPublisherFactory {

  @Override
  public ResendBatchPublisher create(Config config) {
    RecoveryClient client = RecoveryClientFactory.newFactory(config).create(config);
    return new RestResendBatchPublisher(client);
  }

  @Override
  public CommunicationType communicationType() {
    return CommunicationType.REST;
  }
}
