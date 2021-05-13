package com.quorum.tessera.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.Test;

public class ApiObjectTest {

  @Test
  public void testAccessorsForApiObjects() {
    Validator validator =
        ValidatorBuilder.create().with(new GetterTester()).with(new SetterTester()).build();

    validator.validate(
        "com.quorum.tessera.api",
        pojoClass -> !pojoClass.getClazz().getName().contains(VersionTest.class.getName()));
  }

  @Test
  public void nonEmptyConstructor() {
    assertThat(new SendResponse("Data", new String[] {"arbitrary"}, "senderKey"))
        .isNotNull()
        .extracting(SendResponse::getKey)
        .isNotNull();

    assertThat(new SendResponse("Data", new String[] {"arbitrary"}, "senderKey"))
        .isNotNull()
        .extracting(SendResponse::getManagedParties)
        .isNotNull();

    assertThat(new SendResponse("Data", new String[] {"arbitrary"}, "senderKey"))
        .isNotNull()
        .extracting(SendResponse::getSenderKey)
        .containsExactly("senderKey");

    assertThat(new StoreRawResponse("Data".getBytes()))
        .isNotNull()
        .extracting(StoreRawResponse::getKey)
        .isNotNull();

    assertThat(new PrivacyGroupResponse("id", "name", "description", "type", new String[] {}))
        .isNotNull();
  }
}
