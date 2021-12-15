package com.quorum.tessera.config.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerAddressValidator implements ConstraintValidator<ValidServerAddress, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerAddressValidator.class);

  private boolean bindingAddress;

  private List<String> supportedSchemes;

  @Override
  public void initialize(ValidServerAddress a) {
    this.supportedSchemes = Arrays.asList(a.supportedSchemes());
    this.bindingAddress = a.isBindingAddress();
  }

  @Override
  public boolean isValid(String v, ConstraintValidatorContext cvc) {

    if (Objects.isNull(v)) {
      return true;
    }

    final URI uri;
    try {
      uri = new URI(v);
    } catch (URISyntaxException ex) {
      LOGGER.debug(v, ex);
      return false;
    }

    String scheme = uri.getScheme();

    if (!supportedSchemes.contains(scheme)) {
      return false;
    }

    if (scheme.startsWith("http")) {
      if (uri.getPort() == -1) {
        return false;
      }
    }

    if (bindingAddress) {
      return true;
    }

    if (Objects.equals("unix", scheme)) {
      return true;
    }

    return !Objects.equals("0.0.0.0", uri.getHost());
  }
}
