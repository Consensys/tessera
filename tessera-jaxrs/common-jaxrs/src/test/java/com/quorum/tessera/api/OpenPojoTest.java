package com.quorum.tessera.api;

import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.Test;

import java.util.List;

public class OpenPojoTest {

    @Test
    public void testPojos() {
        List<Class> classesToTest = List.of(
            DeleteRequest.class,
            PayloadDecryptRequest.class,
            PayloadEncryptResponse.class,
            ReceiveRequest.class,
            ReceiveResponse.class,
            SendRequest.class,
            SendResponse.class,
            SendSignedRequest.class,
            StoreRawRequest.class,
            StoreRawResponse.class,
            PrivacyGroupRequest.class,
            PrivacyGroupResponse.class,
            PrivacyGroupRetrieveRequest.class,
            PrivacyGroupDeleteRequest.class,
            PrivacyGroupSearchRequest.class,
            BesuReceiveResponse.class
        );

        final Validator pojoValidator = ValidatorBuilder.create()
            .with(new GetterTester())
            .with(new SetterTester())
            .build();

        classesToTest.stream()
            .map(PojoClassFactory::getPojoClass)
            .forEach(p -> {
                pojoValidator.validate(p);
            });

    }

}
