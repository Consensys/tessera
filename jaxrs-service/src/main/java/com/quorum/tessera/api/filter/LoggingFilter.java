package com.quorum.tessera.api.filter;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.container.PreMatching;

@PreMatching
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void filter(final ContainerRequestContext request) {
        log("Enter", request);
    }

    @Override
    public void filter(final ContainerRequestContext request, final ContainerResponseContext response) {
        log("Exit", request);
        String path = Optional.ofNullable(request.getUriInfo()).map(UriInfo::getPath).orElse(null);
        Optional.ofNullable(response.getStatusInfo()).ifPresent(statusType -> LOGGER.info("Response for {} : {} {}", path, statusType.getStatusCode(), statusType.getReasonPhrase()));
    }

    private static void log(String prefix, ContainerRequestContext request) {
        String path = Optional.ofNullable(request.getUriInfo()).map(UriInfo::getPath).orElse(null);
        LOGGER.info("{} Request : {} : {}", prefix, request.getMethod(), "/" + path);

    }
    

 
}
