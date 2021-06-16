module tessera.openapi.common {
  requires static io.swagger.v3.core;
  requires static io.swagger.v3.oas.annotations;
  requires static io.swagger.v3.oas.models;
  requires static com.fasterxml.jackson.databind;

  exports com.quorum.tessera.openapi;
}
