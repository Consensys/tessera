package com.quorum.tessera.api.common;

import com.quorum.tessera.openapi.OpenApiService;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ApiResourceTest {

    private ApiResource apiResource;

    private OpenApiService openApiService;

    private HttpHeaders headers;

    private ServletConfig config;

    private Application app;

    private UriInfo uriInfo;

    private Request request;

    @Before
    public void setUp() {
        openApiService = mock(OpenApiService.class);
        apiResource = new ApiResource(openApiService);

        headers = mock(HttpHeaders.class);
        config = mock(ServletConfig.class);
        app = mock(Application.class);
        uriInfo = mock(UriInfo.class);
        request = mock(Request.class);
    }

    @Test
    public void constructor() {
        ApiResource apiResource = new ApiResource();
        assertThat(apiResource).isNotNull();
    }

    @Test
    public void variants() {
        final List<Variant> variants = ApiResource.variants();

        final Variant json = new Variant(new MediaType("application", "json"), (Locale) null, null);
        final Variant yaml = new Variant(new MediaType("application", "yaml"), (Locale) null, null);

        assertThat(variants).containsExactlyInAnyOrder(json, yaml);
    }

    @Test
    public void api() throws Exception {
        final Variant variant = mock(Variant.class);
        final MediaType mediaType = mock(MediaType.class);
        final String subtype = "json";
        final Response response = mock(Response.class);

        when(request.selectVariant(anyList())).thenReturn(variant);
        when(variant.getMediaType()).thenReturn(mediaType);
        when(mediaType.getSubtype()).thenReturn(subtype);
        when(openApiService.getOpenApi(any(HttpHeaders.class), any(ServletConfig.class), any(Application.class), any(UriInfo.class), eq("json"))).thenReturn(response);

        final Response result = apiResource.api(headers, config, app, uriInfo, request);

        assertThat(result).isEqualTo(response);

        verify(request).selectVariant(ApiResource.variants());
    }

    @Test
    public void apiUnsupportedMediaType() throws Exception {
        when(request.selectVariant(any(List.class))).thenReturn(null);

        final Response result = apiResource.api(headers, config, app, uriInfo, request);

        assertThat(result.getStatus()).isEqualTo(400);
    }
}
