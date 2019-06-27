package com.quorum.tessera.q2t;

import com.quorum.tessera.api.filter.GlobalFilter;
import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.apps.Q2TApp;
import com.quorum.tessera.service.locator.ServiceLocator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.ApplicationPath;

/**
 * The main application that is submitted to the HTTP server Contains all the service classes created by the service
 * locator
 */
@GlobalFilter
@ApplicationPath("/")
public class Q2TRestApp extends TesseraRestApplication implements Q2TApp {

  private final ServiceLocator serviceLocator;

  private final String contextName;

  public Q2TRestApp(final ServiceLocator serviceLocator, final String contextName) {
    this.serviceLocator = Objects.requireNonNull(serviceLocator);
    this.contextName = Objects.requireNonNull(contextName);
  }

  @Override
  public Set<Object> getSingletons() {

    Predicate<Object> isIPWhitelistFilter = o -> IPWhitelistFilter.class.isInstance(o);
    Predicate<Object> isTransactionResource = o -> TransactionResource.class.isInstance(o);

    return serviceLocator.getServices(contextName).stream()
        .filter(Objects::nonNull)
        .filter(o -> Objects.nonNull(o.getClass()))
        .filter(o -> Objects.nonNull(o.getClass().getPackage()))
        .filter(isTransactionResource.or(isIPWhitelistFilter))
        .collect(Collectors.toSet());
  }
}
