package com.quorum.tessera.p2p;

import com.quorum.tessera.api.filter.GlobalFilter;
import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.core.api.ServiceFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import com.quorum.tessera.partyinfo.PartyInfoParser;
import com.quorum.tessera.partyinfo.PartyInfoService;
import io.swagger.annotations.Api;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Client;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The main application that is submitted to the HTTP server Contains all the service classes created by the service
 * locator
 */
@Api
@GlobalFilter
@ApplicationPath("/")
public class P2PRestApp extends TesseraRestApplication {

    private final PartyInfoService partyInfoService;

    private final PartyInfoParser partyInfoParser = PartyInfoParser.create();

    private final Client client;

    private final Enclave enclave;

    private final Config config;

    public P2PRestApp() {
        final ServiceFactory serviceFactory = ServiceFactory.create();
        this.config = serviceFactory.config();

        this.partyInfoService = serviceFactory.partyInfoService();

        this.enclave = serviceFactory.enclave();

        this.client = new ClientFactory().buildFrom(config.getP2PServerConfig());
    }

    @Override
    public Set<Object> getSingletons() {

        final PartyInfoResource partyInfoResource =
                new PartyInfoResource(
                        partyInfoService,
                        partyInfoParser,
                        client,
                        enclave,
                        config.getFeatures().isEnableRemoteKeyValidation());

        final IPWhitelistFilter iPWhitelistFilter = new IPWhitelistFilter();

        final TransactionResource transactionResource = new TransactionResource();

        return Stream.of(partyInfoResource, iPWhitelistFilter, transactionResource).collect(Collectors.toSet());
    }

    @Override
    public AppType getAppType() {
        return AppType.P2P;
    }
}
