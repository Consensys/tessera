package com.quorum.tessera.p2p;

import com.quorum.tessera.api.filter.GlobalFilter;
import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.partyinfo.PartyInfoParser;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.service.locator.ServiceLocator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Client;

/**
 * The main application that is submitted to the HTTP server Contains all the service classes created by the service
 * locator
 */
@GlobalFilter
@ApplicationPath("/")
public class P2PRestApp extends TesseraRestApplication {

    private PartyInfoService partyInfoService;

    private PartyInfoParser partyInfoParser = PartyInfoParser.create();

    private Client client;

    private Enclave enclave;

    public P2PRestApp() {
        this(ServiceLocator.create());
    }

    public P2PRestApp(ServiceLocator serviceLocator) {

        Set<Object> services = serviceLocator.getServices();

        this.partyInfoService =
                services.stream()
                        .filter(PartyInfoService.class::isInstance)
                        .map(PartyInfoService.class::cast)
                        .findAny()
                        .orElseThrow(() -> new IllegalStateException("Cannot find partyInfoService"));

        this.enclave =
                services.stream()
                        .filter(Enclave.class::isInstance)
                        .map(Enclave.class::cast)
                        .findAny()
                        .orElseThrow(() -> new IllegalStateException("Cannot find enclave"));

        Config config =
                services.stream()
                        .filter(Config.class::isInstance)
                        .map(Config.class::cast)
                        .findAny()
                        .orElseThrow(() -> new IllegalStateException("Cannot find config"));

        ServerConfig serverConfig = config.getP2PServerConfig();

        this.client = new com.quorum.tessera.jaxrs.client.ClientFactory().buildFrom(serverConfig);
    }

    @Override
    public Set<Object> getSingletons() {

        PartyInfoResource partyInfoResource = new PartyInfoResource(partyInfoService, partyInfoParser, client, enclave);

        IPWhitelistFilter iPWhitelistFilter = new IPWhitelistFilter();

        TransactionResource transactionResource = new TransactionResource();

        return Stream.of(new ApiResource(), partyInfoResource, iPWhitelistFilter, transactionResource)
                .collect(Collectors.toSet());
    }

    @Override
    public AppType getAppType() {
        return AppType.P2P;
    }
}
