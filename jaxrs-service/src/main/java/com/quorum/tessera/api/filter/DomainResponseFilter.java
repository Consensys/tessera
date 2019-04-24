package com.quorum.tessera.api.filter;

import com.quorum.tessera.config.ServerConfig;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;

@DomainFilter
//In case we decide to apply the filter to every resource add @PreMatching
public class DomainResponseFilter implements ContainerResponseFilter {

    private Set<String> corsDomains;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        if ("unixsocket".equals(requestContext.getUriInfo().getBaseUri().toString())) {
            return;
        }

        final String origin = requestContext.getHeaderString("Origin");

        if (Objects.nonNull(origin) && !Objects.equals(origin, "") &&
            corsDomains.contains(origin)) {

            MultivaluedMap<String, Object> headers = responseContext.getHeaders();
            headers.add("Access-Control-Allow-Origin", origin);
            headers.add("Access-Control-Allow-Credentials", "true");
            headers.add("Access-Control-Allow-Headers", 
                requestContext.getHeaderString("Access-Control-Request-Headers"));
        }

    }


    @Context
    public void setConfiguration(Configuration configuration) {
        this.corsDomains = initCorsDomains(configuration);
    }

    Set<String> initCorsDomains(Configuration configuration) {
        return Optional.of((ServerConfig)configuration.getProperty("tessera.serverConfig")).map(ServerConfig::getCorsDomains)
            .map(domains ->
                Stream.of(domains.split(",")).map(String::trim).collect(Collectors.toSet())
            ).orElse(Collections.emptySet());
    }
}
