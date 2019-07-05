package com.quorum.tessera.admin;

import com.quorum.tessera.api.filter.GlobalFilter;
import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.ApplicationPath;

/** An app that allows access to node management resources */
@GlobalFilter
@ApplicationPath("/")
public class AdminRestApp extends TesseraRestApplication {

    public AdminRestApp() {}

    @Override
    public Set<Object> getSingletons() {

        ConfigResource configResource = new ConfigResource();

        IPWhitelistFilter iPWhitelistFilter = new IPWhitelistFilter();

        return Stream.of(configResource, iPWhitelistFilter).collect(Collectors.toSet());
    }

    @Override
    public AppType getAppType() {
        return AppType.ADMIN;
    }
}
