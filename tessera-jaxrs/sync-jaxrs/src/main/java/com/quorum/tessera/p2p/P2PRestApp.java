package com.quorum.tessera.p2p;

import com.quorum.tessera.core.api.ServiceFactory;
import com.quorum.tessera.api.filter.GlobalFilter;
import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.partyinfo.PartyInfoParser;
import com.quorum.tessera.partyinfo.PartyInfoService;
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
        ServiceFactory serviceFactory = ServiceFactory.create();

        this.partyInfoService = serviceFactory.partyInfoService();

        this.enclave = serviceFactory.enclave();

        Config config = serviceFactory.config();

        ServerConfig serverConfig = config.getP2PServerConfig();

        this.client = new com.quorum.tessera.jaxrs.client.ClientFactory().buildFrom(serverConfig);
    }

    @Override
    public Set<Object> getSingletons() {

        PartyInfoResource partyInfoResource = new PartyInfoResource(partyInfoService, partyInfoParser, client, enclave);

        IPWhitelistFilter iPWhitelistFilter = new IPWhitelistFilter();

        TransactionResource transactionResource = new TransactionResource();

        return Stream.of(partyInfoResource, iPWhitelistFilter, transactionResource).collect(Collectors.toSet());
    }

    @Override
    public AppType getAppType() {
        return AppType.P2P;
    }
}
