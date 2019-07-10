package com.quorum.tessera.thridparty;

import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.ApplicationPath;

/** The third party API */
@ApplicationPath("/")
public class ThirdPartyRestApp extends TesseraRestApplication {

    @Override
    public Set<Object> getSingletons() {

        IPWhitelistFilter iPWhitelistFilter = new IPWhitelistFilter();
        RawTransactionResource rawTransactionResource = new RawTransactionResource();

        return Stream.of(iPWhitelistFilter, rawTransactionResource).collect(Collectors.toSet());
    }

    @Override
    public AppType getAppType() {
        return AppType.THIRD_PARTY;
    }
}
