package com.quorum.tessera.thirdparty;

import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.core.api.ServiceFactory;
import com.quorum.tessera.partyinfo.PartyInfoService;
import io.swagger.annotations.Api;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.ApplicationPath;

/** The third party API */
@Api
@ApplicationPath("/")
public class ThirdPartyRestApp extends TesseraRestApplication {

    private final PartyInfoService partyInfoService;

    private final ConfigService configService;

    public ThirdPartyRestApp() {
        final ServiceFactory serviceFactory = ServiceFactory.create();
        this.partyInfoService = serviceFactory.partyInfoService();
        this.configService = serviceFactory.configService();
    }

    @Override
    public Set<Object> getSingletons() {

        final IPWhitelistFilter iPWhitelistFilter = new IPWhitelistFilter();
        final RawTransactionResource rawTransactionResource = new RawTransactionResource();
        final PartyInfoResource partyInfoResource = new PartyInfoResource(partyInfoService);
        final KeyResource keyResource = new KeyResource(configService);

        return Stream.of(iPWhitelistFilter, rawTransactionResource, partyInfoResource, keyResource)
                .collect(Collectors.toSet());
    }

    @Override
    public AppType getAppType() {
        return AppType.THIRD_PARTY;
    }
}
