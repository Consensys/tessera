package com.quorum.tessera.openapi;

import org.junit.Test;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

public class OpenApiServiceTest {

    @Test
    public void getOpenApi() {
        HttpHeaders headers = mock(HttpHeaders.class);
        ServletConfig config = mock(ServletConfig.class);
        Application app = mock(Application.class);
        UriInfo uriInfo = mock(UriInfo.class);
        String type = "";

        OpenApiService openApiService = new OpenApiService();
        Throwable ex = catchThrowable(() -> openApiService.getOpenApi(headers, config, app, uriInfo, type));
        assertThat(ex).isNotNull();
    }

}
