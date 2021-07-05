package com.quorum.tessera.thirdparty;

import static java.util.stream.Collectors.toSet;

import com.quorum.tessera.api.common.RawTransactionResource;
import com.quorum.tessera.api.common.UpCheckResource;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.core.api.ServiceFactory;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.messaging.Messaging;
import com.quorum.tessera.messaging.MessagingFactory;
import com.quorum.tessera.thirdparty.messaging.MessageResource;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.TransactionManagerFactory;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.ApplicationPath;

/** The third party API */
@ApplicationPath("/")
public class ThirdPartyRestApp extends TesseraRestApplication {

  private final Discovery discovery;

  private final TransactionManager transactionManager;

  private final Messaging messaging;

  public ThirdPartyRestApp() {
    final ServiceFactory serviceFactory = ServiceFactory.create();

    Config config = serviceFactory.config();
    this.discovery = Discovery.getInstance();
    this.transactionManager = TransactionManagerFactory.create().create(config);
    this.messaging = MessagingFactory.create().create(config);
  }

  @Override
  public Set<Object> getSingletons() {
    final RawTransactionResource rawTransactionResource =
        new RawTransactionResource(transactionManager);
    final PartyInfoResource partyInfoResource = new PartyInfoResource(discovery);
    final KeyResource keyResource = new KeyResource();
    final UpCheckResource upCheckResource = new UpCheckResource(transactionManager);
    final MessageResource messagingResource = new MessageResource(messaging);

    return Stream.of(
            rawTransactionResource,
            partyInfoResource,
            keyResource,
            upCheckResource,
            messagingResource)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<Class<?>> getClasses() {
    return Stream.concat(super.getClasses().stream(), Stream.of(ThirdPartyApiResource.class))
        .collect(toSet());
  }

  @Override
  public AppType getAppType() {
    return AppType.THIRD_PARTY;
  }
}
