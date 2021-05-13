package com.quorum.tessera.thirdparty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import org.junit.Before;
import org.junit.Test;

public class ThirdPartyApiResourceTest {

  private ThirdPartyApiResource apiResource;

  private Request request;

  @Before
  public void setUp() {
    apiResource = new ThirdPartyApiResource();
    request = mock(Request.class);
  }

  @Test
  public void getOpenApiDocName() {
    String result = apiResource.getOpenApiDocName();
    assertThat(result).isEqualTo("openapi.thirdparty");
  }

  @Test
  public void getResourceUrl() {
    URL result = apiResource.getResourceUrl("/openapi.thirdparty.yaml");
    assertThat(result).isNotNull();
  }

  @Test
  public void getVariants() {
    List<Variant> result = apiResource.getVariants();

    final Variant json = new Variant(new MediaType("application", "json"), (Locale) null, null);
    final Variant yaml = new Variant(new MediaType("application", "yaml"), (Locale) null, null);

    assertThat(result).containsExactlyInAnyOrder(json, yaml);
  }

  @Test
  public void api() throws Exception {
    final Variant variant = mock(Variant.class);
    final MediaType mediaType = mock(MediaType.class);
    final String subtype = "yaml";

    when(request.selectVariant(anyList())).thenReturn(variant);
    when(variant.getMediaType()).thenReturn(mediaType);
    when(mediaType.getSubtype()).thenReturn(subtype);

    final Response result = apiResource.api(request);

    assertThat(result.getEntity()).isNotNull();
    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.getMediaType()).isEqualTo(mediaType);

    verify(request).selectVariant(apiResource.getVariants());
  }

  @Test
  public void apiUnsupportedMediaType() throws Exception {
    when(request.selectVariant(anyList())).thenReturn(null);

    final Response result = apiResource.api(request);

    assertThat(result.getStatus()).isEqualTo(400);

    verify(request).selectVariant(apiResource.getVariants());
  }
}
