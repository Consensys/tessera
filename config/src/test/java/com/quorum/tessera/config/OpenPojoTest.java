package com.quorum.tessera.config;

import static nl.jqno.equalsverifier.Warning.NONFINAL_FIELDS;
import static nl.jqno.equalsverifier.Warning.STRICT_INHERITANCE;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class OpenPojoTest {

  @Test
  public void testGettersAndSetters() {
    final Validator pojoValidator =
        ValidatorBuilder.create()
            .with(new GetterMustExistRule())
            .with(new GetterTester())
            .with(new SetterTester())
            .build();

    List<Class> classes =
        List.of(
            JdbcConfig.class,
            SslConfig.class,
            PrivateKeyData.class,
            ServerConfig.class,
            DefaultKeyVaultConfig.class,
            Config.class,
            Version.class,
            FeatureToggles.class,
            InfluxConfig.class,
            ArgonOptions.class,
            Peer.class,
            ResidentGroup.class);

    for (Class type : classes) {
      PojoClass pojoClass = PojoClassFactory.getPojoClass(type);
      pojoValidator.validate(pojoClass);
    }
  }

  @Test
  public void equalsAndHashcode() {
    EqualsVerifier.configure()
        .suppress(STRICT_INHERITANCE, NONFINAL_FIELDS)
        .forClass(FeatureToggles.class)
        .verify();

    EqualsVerifier.configure()
        .suppress(STRICT_INHERITANCE, NONFINAL_FIELDS)
        .forClass(EncryptorConfig.class)
        .usingGetClass()
        .verify();

    EqualsVerifier.configure()
        .suppress(STRICT_INHERITANCE, NONFINAL_FIELDS)
        .forClass(Peer.class)
        .usingGetClass()
        .verify();
  }
}
