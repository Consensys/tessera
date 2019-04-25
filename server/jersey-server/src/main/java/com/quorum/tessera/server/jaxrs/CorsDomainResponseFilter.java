package com.quorum.tessera.server.jaxrs;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorsDomainResponseFilter implements ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorsDomainResponseFilter.class);

    private final List<String> tokens;

    public CorsDomainResponseFilter(List<String> tokens) {
        this.tokens = Objects.requireNonNull(tokens);
        LOGGER.info("Create filter with tokens {}", String.join(",", tokens));
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        if ("unixsocket".equals(requestContext.getUriInfo().getBaseUri().toString())) {
            return;
        }

        final String origin = requestContext.getHeaderString("Origin");

        if (Objects.nonNull(origin) && !Objects.equals(origin, "")) {

            MultivaluedMap<String, Object> headers = responseContext.getHeaders();

            headers.add("Access-Control-Allow-Origin", origin);
            headers.add("Access-Control-Allow-Credentials", "true");
            headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            headers.add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");

        }
    }

}
