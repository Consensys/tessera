package com.quorum.tessera.server.jaxrs;

import com.quorum.tessera.config.CrossDomainConfig;
import java.io.IOException;
import java.util.Objects;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorsDomainResponseFilter implements ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorsDomainResponseFilter.class);

    private final OriginMatchUtil originMatchUtil;
    
    private final CrossDomainConfig corsConfig;

    public CorsDomainResponseFilter(CrossDomainConfig corsConfig) {
        this.corsConfig = Objects.requireNonNull(corsConfig);
        this.originMatchUtil = new OriginMatchUtil(corsConfig.getAllowedOrigins());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        final String origin = requestContext.getHeaderString("Origin");
        
        if (originMatchUtil.matches(origin)) {

            MultivaluedMap<String, Object> headers = responseContext.getHeaders();

            headers.add("Access-Control-Allow-Origin", origin);
            headers.add("Access-Control-Allow-Credentials", corsConfig.getAllowCredentials().toString());
            headers.add("Access-Control-Allow-Methods", String.join(",", corsConfig.getAllowedMethods()));
            
            final String allowedHeaders;
            if(corsConfig.getAllowedHeaders() != null) {
                allowedHeaders = String.join(",",corsConfig.getAllowedHeaders());
            } else {
                allowedHeaders = requestContext.getHeaderString("Access-Control-Request-Headers"); 
            }
            
            headers.add("Access-Control-Allow-Headers", allowedHeaders);

        }
    }

}
