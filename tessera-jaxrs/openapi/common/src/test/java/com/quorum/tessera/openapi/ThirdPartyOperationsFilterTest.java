package com.quorum.tessera.openapi;

import io.swagger.v3.core.model.ApiDescription;
import io.swagger.v3.oas.models.Operation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ThirdPartyOperationsFilterTest {

  private ThirdPartyOperationsFilter operationsFilter;

  private Operation operation;

  private ApiDescription api;

  private Map<String, List<String>> params;

  private Map<String, String> cookies;

  private Map<String, List<String>> headers;

  @Before
  public void setUp() {
    operationsFilter = new ThirdPartyOperationsFilter();
    operation = Mockito.mock(Operation.class);
    api = Mockito.mock(ApiDescription.class);
    params = Collections.emptyMap();
    cookies = Collections.emptyMap();
    headers = Collections.emptyMap();
  }

  @Test
  public void filerThirdPartyOperation() {
    Mockito.when(operation.getTags()).thenReturn(List.of("third-party", "other-tag"));

    Optional<Operation> result =
        operationsFilter.filterOperation(operation, api, params, cookies, headers);

    Assertions.assertThat(result).isPresent();
    Assertions.assertThat(result).hasValue(operation);

    Mockito.verify(operation).getTags();
  }

  @Test
  public void filerNonThirdPartyOperation() {
    Mockito.when(operation.getTags()).thenReturn(List.of("not-third-party", "other-tag"));

    Optional<Operation> result =
        operationsFilter.filterOperation(operation, api, params, cookies, headers);

    Assertions.assertThat(result).isNotPresent();

    Mockito.verify(operation).getTags();
  }
}
