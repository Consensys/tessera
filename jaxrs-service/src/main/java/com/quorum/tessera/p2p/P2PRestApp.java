package com.quorum.tessera.p2p;

import com.quorum.tessera.api.filter.GlobalFilter;
import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.service.locator.ServiceLocator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.ApplicationPath;

/**
 * The main application that is submitted to the HTTP server Contains all the service classes created by the service
 * locator
 */
@GlobalFilter
@ApplicationPath("/")
public class P2PRestApp extends TesseraRestApplication {

    private final ServiceLocator serviceLocator;

    public P2PRestApp() {
        this(ServiceLocator.create());
    }

    public P2PRestApp(ServiceLocator serviceLocator) {
        this.serviceLocator = Objects.requireNonNull(serviceLocator);
    }

    @Override
    public Set<Object> getSingletons() {

        Predicate<Object> isPartyInfoResource = o -> PartyInfoResource.class.isInstance(o);
        Predicate<Object> isIPWhitelistFilter = o -> IPWhitelistFilter.class.isInstance(o);
        Predicate<Object> isTransactionResource = o -> TransactionResource.class.isInstance(o);

        return Stream.concat(
                        Stream.of(new ApiResource()),
                        serviceLocator.getServices().stream()
                                .filter(Objects::nonNull)
                                .filter(o -> Objects.nonNull(o.getClass()))
                                .filter(o -> Objects.nonNull(o.getClass().getPackage()))
                                .filter(isIPWhitelistFilter.or(isPartyInfoResource).or(isTransactionResource)))
                .collect(Collectors.toSet());
    }

    @Override
    public AppType getAppType() {
        return AppType.P2P;
    }
}
