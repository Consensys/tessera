package com.quorum.tessera.server.http;

import com.quorum.tessera.version.ApiVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import static com.quorum.tessera.shared.Constants.API_VERSION_HEADER;

public class VersionHeaderDecorator implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionHeaderDecorator.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
                    final HttpServletResponse httpServletResponse = HttpServletResponse.class.cast(servletResponse);
                    final HttpServletRequest httpServletRequest = HttpServletRequest.class.cast(servletRequest);

                    LOGGER.debug("caller uri {}",httpServletRequest.getRequestURI());

                    final List<String> supportedApiVersions = Collections.list(httpServletRequest.getHeaders(API_VERSION_HEADER));

                    LOGGER.debug("httpServletRequest.headers[{}] {}", API_VERSION_HEADER, supportedApiVersions);

                    List<String> versions = ApiVersion.versions();

                    versions.forEach(v -> httpServletResponse.addHeader(API_VERSION_HEADER,v));

                    filterChain.doFilter(servletRequest, servletResponse);

    }
}
