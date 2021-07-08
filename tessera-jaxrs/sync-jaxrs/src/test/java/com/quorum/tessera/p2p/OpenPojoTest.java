package com.quorum.tessera.p2p;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.NoPrimitivesRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import com.quorum.tessera.p2p.recovery.PushBatchRequest;
import com.quorum.tessera.p2p.recovery.ResendBatchRequest;
import com.quorum.tessera.p2p.recovery.ResendBatchResponse;
import com.quorum.tessera.p2p.resend.ResendRequest;
import com.quorum.tessera.p2p.resend.ResendResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class OpenPojoTest {

  @Test
  public void testPojos() {

    List<Class> classList =
        List.of(
            ResendRequest.class,
            ResendResponse.class,
            ResendBatchRequest.class,
            ResendBatchResponse.class,
            PushBatchRequest.class);

    List<PojoClass> pojoClasses =
        classList.stream().map(PojoClassFactory::getPojoClass).collect(Collectors.toList());

    final Validator pojoValidator =
        ValidatorBuilder.create()
            .with(new GetterTester())
            .with(new SetterTester())
            .with(new NoPrimitivesRule())
            .build();

    pojoValidator.validate(pojoClasses);
  }
}
