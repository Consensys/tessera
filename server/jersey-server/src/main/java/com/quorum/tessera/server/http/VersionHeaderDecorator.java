package com.quorum.tessera.server.http;

import com.quorum.tessera.version.VersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class VersionHeaderDecorator implements Filter {


    public static final String CURRENT_VERSION = "tesseraApiVersion";

    public static final String PREVIOUS_VERSION = "tesseraApiVersionOld";

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionHeaderDecorator.class);

    private VersionInfo versionInfo;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        versionInfo = VersionInfo.create();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
                    final HttpServletResponse httpServletResponse = HttpServletResponse.class.cast(servletResponse);
                    final HttpServletRequest httpServletRequest = HttpServletRequest.class.cast(servletRequest);

                    LOGGER.debug("caller uri {}",httpServletRequest.getRequestURI());

                    final String tesseraApiVersion = httpServletRequest.getHeader(CURRENT_VERSION);
                    final String tesseraApiVersionOld = httpServletRequest.getHeader(PREVIOUS_VERSION);

                    LOGGER.debug("httpServletRequest.headers[{}] {}",CURRENT_VERSION, tesseraApiVersion);
                    LOGGER.debug("httpServletRequest.headers[{}] {}",PREVIOUS_VERSION, tesseraApiVersionOld);

                    httpServletResponse.setHeader(CURRENT_VERSION, versionInfo.currentVersion());
                    httpServletResponse.setHeader(PREVIOUS_VERSION, versionInfo.previousVersion());

                    filterChain.doFilter(servletRequest, servletResponse);

    }
}
