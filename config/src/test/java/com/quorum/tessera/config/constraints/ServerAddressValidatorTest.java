package com.quorum.tessera.config.constraints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.Test;

public class ServerAddressValidatorTest {

  @Test
  public void valid() {

    ServerAddressValidator validator = new ServerAddressValidator();

    ValidServerAddress validServerAddress = mock(ValidServerAddress.class);
    when(validServerAddress.supportedSchemes()).thenReturn(new String[] {"http"});

    validator.initialize(validServerAddress);

    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    assertThat(validator.isValid("https://www.ilovesparraws.com:80/somepatth", context)).isFalse();
    assertThat(validator.isValid("http://www.ilovesparraws.com:80/somepatth", context)).isTrue();
  }

  @Test
  public void dontAllowZeroIpWhenNotBindingAddress() {

    ServerAddressValidator validator = new ServerAddressValidator();

    ValidServerAddress validServerAddress = mock(ValidServerAddress.class);

    when(validServerAddress.isBindingAddress()).thenReturn(false);
    when(validServerAddress.supportedSchemes()).thenReturn(new String[] {"http"});

    validator.initialize(validServerAddress);

    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    assertThat(validator.isValid("http://0.0.0.0:80", context)).isFalse();
  }

  @Test
  public void dontAllowZeroIpWhenBindingAddress() {

    ServerAddressValidator validator = new ServerAddressValidator();

    ValidServerAddress validServerAddress = mock(ValidServerAddress.class);

    when(validServerAddress.isBindingAddress()).thenReturn(true);
    when(validServerAddress.supportedSchemes()).thenReturn(new String[] {"http"});

    validator.initialize(validServerAddress);

    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    assertThat(validator.isValid("http://0.0.0.0:80", context)).isTrue();
  }

  @Test
  public void nullValueIsIgnored() {

    ServerAddressValidator validator = new ServerAddressValidator();

    ValidServerAddress validServerAddress = mock(ValidServerAddress.class);

    when(validServerAddress.isBindingAddress()).thenReturn(true);
    when(validServerAddress.supportedSchemes()).thenReturn(new String[] {"http"});

    validator.initialize(validServerAddress);

    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    assertThat(validator.isValid(null, context)).isTrue();
  }

  @Test
  public void unixSchemStypeIntCheckedForZeroIp() {

    ServerAddressValidator validator = new ServerAddressValidator();

    ValidServerAddress validServerAddress = mock(ValidServerAddress.class);

    when(validServerAddress.isBindingAddress()).thenReturn(false);
    when(validServerAddress.supportedSchemes()).thenReturn(new String[] {"unix"});

    validator.initialize(validServerAddress);

    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    assertThat(validator.isValid("unix:/bogus", context)).isTrue();
  }

  @Test
  public void httpPortIsRequired() {

    ServerAddressValidator validator = new ServerAddressValidator();

    ValidServerAddress validServerAddress = mock(ValidServerAddress.class);
    when(validServerAddress.supportedSchemes()).thenReturn(new String[] {"http"});

    validator.initialize(validServerAddress);

    ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    assertThat(validator.isValid("http://www.ilovesparraws.com", context)).isFalse();
  }
}
