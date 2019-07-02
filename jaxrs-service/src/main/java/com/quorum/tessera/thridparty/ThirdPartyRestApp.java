package com.quorum.tessera.thridparty;

import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.service.locator.ServiceLocator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.ApplicationPath;

/** The third party API */
@ApplicationPath("/")
public class ThirdPartyRestApp extends TesseraRestApplication {

    private final ServiceLocator serviceLocator;

    public ThirdPartyRestApp() {
        this(ServiceLocator.create());
    }

    public ThirdPartyRestApp(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    @Override
    public Set<Object> getSingletons() {

        Predicate<Object> isIPWhitelistFilter = o -> IPWhitelistFilter.class.isInstance(o);
        Predicate<Object> isTransactionResource = o -> RawTransactionResource.class.isInstance(o);

        return serviceLocator.getServices().stream()
                .filter(Objects::nonNull)
                .filter(o -> Objects.nonNull(o.getClass()))
                .filter(o -> Objects.nonNull(o.getClass().getPackage()))
                .filter(isTransactionResource.or(isIPWhitelistFilter))
                .collect(Collectors.toSet());
    }

    @Override
    public AppType getAppType() {
        return AppType.THIRD_PARTY;
    }
}
