package com.quorum.tessera.api.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

/*
https://docs.oracle.com/javaee/7/api/javax/ws/rs/NameBinding.html
*/
@Logged
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter  {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);
    
    @Override
    public void filter(final ContainerRequestContext crc) {
        
        LOGGER.debug("Log around : {}",crc);
    }

    @Override
    public void filter(final ContainerRequestContext crc, final ContainerResponseContext crc1) {
        LOGGER.debug("Log around : {} : {}",crc,crc1);
    }
    
}
