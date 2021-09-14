package com.quorum.tessera.thirdparty;

import static java.util.stream.Collectors.toSet;

import com.quorum.tessera.api.common.RawTransactionResource;
import com.quorum.tessera.api.common.UpCheckResource;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.transaction.TransactionManager;
import jakarta.ws.rs.ApplicationPath;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/** The third party API */
@ApplicationPath("/")
public class ThirdPartyRestApp extends TesseraRestApplication
    implements com.quorum.tessera.config.apps.TesseraApp {

  private final Discovery discovery;

  private final TransactionManager transactionManager;

  public ThirdPartyRestApp() {
    this(Discovery.create(), TransactionManager.create());
  }

  protected ThirdPartyRestApp(Discovery discovery, TransactionManager transactionManager) {
    this.discovery = Objects.requireNonNull(discovery);
    this.transactionManager = Objects.requireNonNull(transactionManager);
  }

  @Override
  public Set<Object> getSingletons() {
    final RawTransactionResource rawTransactionResource =
        new RawTransactionResource(transactionManager);
    final PartyInfoResource partyInfoResource = new PartyInfoResource(discovery);
    final KeyResource keyResource = new KeyResource();
    final UpCheckResource upCheckResource = new UpCheckResource();
    return Set.of(rawTransactionResource, partyInfoResource, keyResource, upCheckResource);
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
