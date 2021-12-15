package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.*;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.JaxbUtil;
import jakarta.xml.bind.annotation.XmlElement;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverrideUtilTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(OverrideUtilTest.class);

  @Test
  public void fieldsPrimitive() {
    class PrimitiveBox {
      int i;
    }

    Map<String, Class> result = OverrideUtil.fields("primitive", PrimitiveBox.class);
    assertThat(result.keySet())
        .filteredOn(s -> !s.contains("$jacocoData"))
        .containsExactlyInAnyOrder("primitive.i");

    assertThat(result.get("primitive.i")).isEqualTo(int.class);
  }

  @Test
  public void fieldsCollection() {
    class SimpleBox {
      String s;
      Path p;
    }
    class CollectionBox {
      List<String> simple;
      List<SimpleBox> complex;
    }

    Map<String, Class> result = OverrideUtil.fields("collection", CollectionBox.class);
    assertThat(result.keySet())
        .filteredOn(s -> !s.contains("$jacocoData"))
        .containsExactlyInAnyOrder(
            "collection.simple", "collection.complex.s", "collection.complex.p");

    assertThat(result.get("collection.simple")).isEqualTo(String[].class);
    assertThat(result.get("collection.complex.s")).isEqualTo(String.class);
    assertThat(result.get("collection.complex.p")).isEqualTo(Path.class);
  }

  @Test
  public void fieldsConfig() {
    Map<String, Class> fields = OverrideUtil.fields(null, Config.class);
    assertThat(fields).isNotEmpty();
  }

  @Test
  @Ignore
  public void overrideExistingValueKeyDataWithPublicKey() {

    Config config = OverrideUtil.createInstance(Config.class);

    final String publicKeyValue = "PUBLIC_KEY";

    OverrideUtil.setValue(config, "keys.keyData.publicKey", publicKeyValue);

    assertThat(config.getKeys()).isNotNull();

    KeyConfiguration keyConfig = config.getKeys();

    assertThat(keyConfig.getKeyData()).hasSize(1);

    assertThat(keyConfig.getKeyData().get(0).getPublicKey()).isEqualTo(publicKeyValue);
  }

  @Test
  public void resolveFieldXmlElementName() {

    Field result = OverrideUtil.resolveField(SomeClass.class, "some_value");

    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("someValue");
  }

  @Test
  public void resolveField() {

    Field result = OverrideUtil.resolveField(SomeClass.class, "otherValue");

    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("otherValue");
  }

  static class SomeClass {

    private static SomeClass create() {
      return new SomeClass();
    }

    @XmlElement(name = "some_value")
    String someValue;

    @XmlElement String otherValue;
  }

  static class OtherClass {

    List<SomeClass> someList;
  }

  enum Foo {
    INSTANCE
  }

  @Test
  public void isSimple() {

    assertThat(OverrideUtil.isSimple(int.class)).isTrue();
    assertThat(OverrideUtil.isSimple(boolean.class)).isTrue();
    assertThat(OverrideUtil.isSimple(long.class)).isTrue();
    assertThat(OverrideUtil.isSimple(Foo.class)).isTrue();
    assertThat(OverrideUtil.isSimple(String.class)).isTrue();
    assertThat(OverrideUtil.isSimple(Integer.class)).isTrue();
    assertThat(OverrideUtil.isSimple(Long.class)).isTrue();
    assertThat(OverrideUtil.isSimple(Boolean.class)).isTrue();
    assertThat(OverrideUtil.isSimple(List.class)).isFalse();
  }

  @Test
  public void toArrayType() {
    assertThat(OverrideUtil.toArrayType(String.class)).isEqualTo(String[].class);
    assertThat(OverrideUtil.toArrayType(Path.class)).isEqualTo(Path[].class);
  }

  @Test
  public void createInstance() {
    Peer result = OverrideUtil.createInstance(Peer.class);
    assertThat(result).isNotNull();
  }

  @Test
  public void createInstanceMapCreatesLinkedHashMap() {
    Map<Object, Object> result = OverrideUtil.createInstance(Map.class);
    assertThat(result).isNotNull();
    assertThat(result).isExactlyInstanceOf(LinkedHashMap.class);
  }

  @Test
  public void classForName() {
    Class type = OverrideUtil.classForName(getClass().getName());
    assertThat(type).isEqualTo(getClass());
  }

  @Test
  public void convertTo() {

    assertThat(OverrideUtil.convertTo(Path.class, "SOMEFILE")).isEqualTo(Paths.get("SOMEFILE"));

    assertThat(OverrideUtil.convertTo(String.class, "SOMESTR")).isEqualTo("SOMESTR");

    assertThat(OverrideUtil.convertTo(Integer.class, "999")).isEqualTo(999);

    assertThat(OverrideUtil.convertTo(Long.class, "999")).isEqualTo(999L);

    assertThat(OverrideUtil.convertTo(Boolean.class, "true")).isTrue();

    assertThat(OverrideUtil.convertTo(SslAuthenticationMode.class, "STRICT"))
        .isEqualTo(SslAuthenticationMode.STRICT);

    assertThat(OverrideUtil.convertTo(String.class, null)).isNull();
  }

  @Test
  public void initialiseNestedObjects() {

    Config config = new Config(null, null, null, null, null, true, true);

    OverrideUtil.initialiseNestedObjects(config);

    LOGGER.debug(JaxbUtil.marshalToStringNoValidation(config));

    assertThat(config.getJdbcConfig()).isNotNull();
    assertThat(config.getKeys()).isNotNull();
    assertThat(config.getPeers()).isEmpty();
    assertThat(config.getAlwaysSendTo()).isEmpty();
    assertThat(config.isDisablePeerDiscovery()).isTrue();
  }

  @Test
  public void initialiseNestedObjectsWithNullValueDoesNothing() {
    final Throwable throwable = catchThrowable(() -> OverrideUtil.initialiseNestedObjects(null));
    assertThat(throwable).isNull();
  }

  @Test
  public void createConfigInstance() {
    Config config = OverrideUtil.createInstance(Config.class);
    assertThat(config).isNotNull();

    LOGGER.debug(JaxbUtil.marshalToStringNoValidation(config));

    assertThat(config.getJdbcConfig()).isNotNull();
    assertThat(config.getKeys()).isNotNull();
    assertThat(config.getPeers()).isEmpty();
    assertThat(config.getAlwaysSendTo()).isEmpty();
  }

  @Test
  public void createConfigInstanceWithInterfaceReturnsNull() {
    final OverrideUtil interfaceObject = OverrideUtil.createInstance(OverrideUtil.class);
    assertThat(interfaceObject).isNull();
  }

  @Test
  public void convertToByteArray() {
    final byte[] result = OverrideUtil.convertTo(byte[].class, "HELLOW");
    assertThat(result).isEqualTo("HELLOW".getBytes());
  }

  @Test
  public void setValue() {
    Config config = OverrideUtil.createInstance(Config.class);

    OverrideUtil.setValue(config, "jdbc.username", "someuser");
    OverrideUtil.setValue(config, "peers[0].url", "snonce1");
    OverrideUtil.setValue(config, "peers[1].url", "snonce2");

    assertThat(config.getJdbcConfig().getUsername()).isEqualTo("someuser");

    assertThat(config.getPeers().get(0).getUrl()).isEqualTo("snonce1");
    assertThat(config.getPeers().get(1).getUrl()).isEqualTo("snonce2");
  }

  @Test
  public void setValueWithoutAdditions() {
    final OtherClass someList = new OtherClass();
    OverrideUtil.setValue(someList, "someList[0].someValue", "password1");
    OverrideUtil.setValue(someList, "someList[1].someValue", "password2");
    assertThat(someList.someList.get(0).someValue).isEqualTo("password1");
    assertThat(someList.someList.get(1).someValue).isEqualTo("password2");
  }

  @Test
  public void setValueOnNullDoesNothing() {
    final Throwable throwable =
        catchThrowable(() -> OverrideUtil.setValue(null, "jdbc.username", "someuser"));
    assertThat(throwable).isNull();
  }

  @Test
  public void setValuePreservePreDefined() throws Exception {
    final Config config;
    try (InputStream data = getClass().getResourceAsStream("/sample-config.json")) {
      config = JaxbUtil.unmarshal(data, Config.class);
    }

    OverrideUtil.setValue(config, "jdbc.username", "someuser");

    assertThat(config.getJdbcConfig().getUsername()).isEqualTo("someuser");
    assertThat(config.getJdbcConfig().getPassword()).isEqualTo("tiger");
  }

  // TODO: Need to support oerrides in config module
  @Ignore
  @Test
  public void definePrivateAndPublicKeyWithOverridesOnly() throws Exception {

    Config config = OverrideUtil.createInstance(Config.class);

    OverrideUtil.setValue(config, "keys[0].keyData.publicKey", "PUBLICKEY");
    OverrideUtil.setValue(config, "keys[0].keyData.privateKey", "PRIVATEKEY");
    // UNmarshlling to COnfig to
    try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
      JaxbUtil.marshalWithNoValidation(config, bout);
      Config result =
          JaxbUtil.unmarshal(new ByteArrayInputStream(bout.toByteArray()), Config.class);
      assertThat(result.getKeys()).isNotNull();

      KeyConfiguration keyConfig = result.getKeys();

      assertThat(keyConfig.getKeyData()).hasSize(1);

      assertThat(keyConfig.getKeyData().get(0).getPrivateKey()).isEqualTo("PRIVATEKEY");

      assertThat(keyConfig.getKeyData().get(0).getPublicKey()).isEqualTo("PUBLICKEY");
    }
  }

  @Test
  public void defineAlwaysSendToWithOverridesOnly() throws Exception {

    Config config = OverrideUtil.createInstance(Config.class);

    OverrideUtil.setValue(config, "alwaysSendTo[0]", "ONE");
    OverrideUtil.setValue(config, "alwaysSendTo[1]", "TWO");

    try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
      JaxbUtil.marshalWithNoValidation(config, bout);

      Config result =
          JaxbUtil.unmarshal(new ByteArrayInputStream(bout.toByteArray()), Config.class);

      assertThat(result.getAlwaysSendTo()).hasSize(2);

      assertThat(result.getAlwaysSendTo()).containsOnly("ONE", "TWO");
    }
  }

  @Test
  public void setValueWithAnonClassDoesNothing() {

    SomeIFace anon =
        new SomeIFace() {
          private String value = "HEllow";

          @Override
          public String getValue() {
            return value;
          }
        };

    OverrideUtil.setValue(anon, "value", "SOMETHING");
  }

  interface SomeIFace {

    String getValue();
  }

  @Test
  public void setValueCollectionButNoPositionProvided() {
    final String initialValue = "initial test value";
    final String overriddenValue = "overridden test value";

    final ToOverride toOverride = new ToOverride();

    final List<String> simpleList = Arrays.asList("element 1", initialValue, "element 3");
    toOverride.setSimpleList(simpleList);

    Throwable ex =
        catchThrowable(() -> OverrideUtil.setValue(toOverride, "simpleList", overriddenValue));

    assertThat(ex).isNotNull();
    assertThat(ex).isExactlyInstanceOf(CliException.class);
    assertThat(ex)
        .hasMessage(
            "simpleList: position not provided for Collection parameter override simpleList");
  }

  @Test
  public void setValueElementOfSimpleCollectionReplaced() {
    final String initialValue = "initial test value";
    final String overriddenValue = "overridden test value";

    final ToOverride toOverride = new ToOverride();

    final List<String> simpleList = Arrays.asList("element 1", initialValue, "element 3");
    toOverride.setSimpleList(simpleList);

    OverrideUtil.setValue(toOverride, "simpleList[1]", overriddenValue);

    assertThat(toOverride.getSimpleList()).hasSize(3);
    assertThat(toOverride.getSimpleList().get(0)).isEqualTo("element 1");
    assertThat(toOverride.getSimpleList().get(1)).isEqualTo(overriddenValue);
    assertThat(toOverride.getSimpleList().get(2)).isEqualTo("element 3");
  }

  @Test
  public void setValuePropertyOfElementInComplexCollectionReplaced() {
    final int initialValue = 11;
    final int overriddenValue = 20;

    final ToOverride toOverride = new ToOverride();

    final ToOverride.OtherTestClass otherClass = new ToOverride.OtherTestClass();
    otherClass.setCount(initialValue);

    final List<ToOverride.OtherTestClass> someList = new ArrayList<>();
    someList.add(otherClass);

    toOverride.setSomeList(someList);

    OverrideUtil.setValue(toOverride, "someList[0].count", Integer.toString(overriddenValue));

    assertThat(toOverride.getSomeList()).hasSize(1);
    assertThat(toOverride.getSomeList().get(0).getCount()).isEqualTo(overriddenValue);
  }

  @Test
  public void setValueElementOfSimpleCollectionInComplexCollectionReplaced() {
    final String initialValue = "initial test value";
    final String overriddenValue = "updated test value";

    final ToOverride toOverride = new ToOverride();

    final List<String> otherList = Arrays.asList("some value", initialValue);
    final ToOverride.OtherTestClass otherClass = new ToOverride.OtherTestClass();
    otherClass.setOtherList(otherList);

    final List<ToOverride.OtherTestClass> someList = new ArrayList<>();
    someList.add(otherClass);

    toOverride.setSomeList(someList);

    OverrideUtil.setValue(toOverride, "someList[0].otherList[1]", overriddenValue);

    assertThat(toOverride.getSomeList()).hasSize(1);
    assertThat(toOverride.getSomeList().get(0).getOtherList()).hasSize(2);
    assertThat(toOverride.getSomeList().get(0).getOtherList().get(1)).isEqualTo(overriddenValue);
  }

  @Test
  public void setValueSimplePropertyReplaced() {
    final String initialValue = "the initial value";
    final String overriddenValue = "the overridden value";

    final ToOverride toOverride = new ToOverride();
    toOverride.setOtherValue(initialValue);

    OverrideUtil.setValue(toOverride, "otherValue", overriddenValue);

    assertThat(toOverride.getOtherValue()).isEqualTo(overriddenValue);
  }

  @Test
  public void setValuePropertyOfComplexPropertyReplaced() {
    final int initialValue = 11;
    final int overriddenValue = 20;

    final ToOverride.OtherTestClass complexProperty = new ToOverride.OtherTestClass();
    complexProperty.setCount(initialValue);

    final ToOverride toOverride = new ToOverride();
    toOverride.setComplexProperty(complexProperty);

    OverrideUtil.setValue(toOverride, "complexProperty.count", Integer.toString(overriddenValue));

    assertThat(toOverride.getComplexProperty()).isNotNull();
    assertThat(toOverride.getComplexProperty().getCount()).isEqualTo(overriddenValue);
  }

  @Test
  public void setValueSimpleCollectionCreated() {
    final String overriddenValue = "overridden test value";

    final ToOverride toOverride = new ToOverride();

    OverrideUtil.setValue(toOverride, "simpleList[2]", overriddenValue);

    assertThat(toOverride.getSimpleList()).isNotNull();
    assertThat(toOverride.getSimpleList()).hasSize(3);
    assertThat(toOverride.getSimpleList().get(0)).isNull();
    assertThat(toOverride.getSimpleList().get(1)).isNull();
    assertThat(toOverride.getSimpleList().get(2)).isEqualTo(overriddenValue);
  }

  @Test
  public void setValueComplexCollectionCreated() {
    final int overriddenCount = 11;
    final int otherOverriddenCount = 22;
    final String overriddenValue = "overridden test value";

    final ToOverride toOverride = new ToOverride();

    OverrideUtil.setValue(toOverride, "someList[1].count", Integer.toString(overriddenCount));
    OverrideUtil.setValue(toOverride, "someList[2].count", Integer.toString(otherOverriddenCount));
    OverrideUtil.setValue(toOverride, "someList[2].strVal", overriddenValue);

    assertThat(toOverride.getSomeList()).isNotNull();
    assertThat(toOverride.getSomeList()).hasSize(3);

    assertThat(toOverride.getSomeList().get(0)).isNotNull();
    assertThat(toOverride.getSomeList().get(1)).isNotNull();
    assertThat(toOverride.getSomeList().get(2)).isNotNull();

    assertThat(toOverride.getSomeList().get(0).getCount()).isZero();
    assertThat(toOverride.getSomeList().get(0).getStrVal()).isNull();
    assertThat(toOverride.getSomeList().get(1).getCount()).isEqualTo(overriddenCount);
    assertThat(toOverride.getSomeList().get(1).getStrVal()).isNull();
    assertThat(toOverride.getSomeList().get(2).getCount()).isEqualTo(otherOverriddenCount);
    assertThat(toOverride.getSomeList().get(2).getStrVal()).isEqualTo(overriddenValue);
  }

  @Test
  public void setValueSimpleCollectionInComplexCollectionCreated() {
    final String overriddenValue = "overridden test value";

    final ToOverride toOverride = new ToOverride();

    OverrideUtil.setValue(toOverride, "someList[0].otherList[1]", overriddenValue);

    assertThat(toOverride.getSomeList()).isNotNull();
    assertThat(toOverride.getSomeList()).hasSize(1);
    assertThat(toOverride.getSomeList().get(0)).isNotNull();

    assertThat(toOverride.getSomeList().get(0).getOtherList()).isNotNull();
    assertThat(toOverride.getSomeList().get(0).getOtherList()).hasSize(2);

    assertThat(toOverride.getSomeList().get(0).getOtherList().get(0)).isNull();
    assertThat(toOverride.getSomeList().get(0).getOtherList().get(1)).isEqualTo(overriddenValue);
  }

  @Test
  public void setValueNullSimplePropertySet() {
    final String overriddenValue = "overridden test value";

    final ToOverride toOverride = new ToOverride();

    OverrideUtil.setValue(toOverride, "otherValue", overriddenValue);

    assertThat(toOverride.getOtherValue()).isNotNull();
    assertThat(toOverride.getOtherValue()).isEqualTo(overriddenValue);
  }

  @Test
  public void setValueNullPropertyOfComplexPropertySet() {
    final String overriddenValue = "overridden test value";

    final ToOverride toOverride = new ToOverride();
    final ToOverride.OtherTestClass complexProperty = new ToOverride.OtherTestClass();
    toOverride.setComplexProperty(complexProperty);

    OverrideUtil.setValue(toOverride, "complexProperty.strVal", overriddenValue);

    assertThat(toOverride.getComplexProperty()).isNotNull();
    assertThat(toOverride.getComplexProperty().getStrVal()).isNotNull();
    assertThat(toOverride.getComplexProperty().getStrVal()).isEqualTo(overriddenValue);
  }

  @Test
  public void setValueSimpleCollectionExtended() {
    final String overriddenValue = "overridden test value";

    final ToOverride toOverride = new ToOverride();
    final List<String> simpleList = new ArrayList<>();
    simpleList.add("element1");
    toOverride.setSimpleList(simpleList);

    assertThat(toOverride.getSimpleList()).hasSize(1);

    OverrideUtil.setValue(toOverride, "simpleList[1]", overriddenValue);

    assertThat(toOverride.getSimpleList()).isNotNull();
    assertThat(toOverride.getSimpleList()).hasSize(2);
    assertThat(toOverride.getSimpleList().get(0)).isEqualTo("element1");
    assertThat(toOverride.getSimpleList().get(1)).isEqualTo(overriddenValue);
  }

  @Test
  public void setValueComplexCollectionExtended() {
    final String overriddenValue = "overridden test value";

    final ToOverride toOverride = new ToOverride();
    final ToOverride.OtherTestClass otherTestClass = new ToOverride.OtherTestClass();
    otherTestClass.setStrVal("element1");

    final List<ToOverride.OtherTestClass> someList = new ArrayList<>();
    someList.add(otherTestClass);

    toOverride.setSomeList(someList);

    assertThat(toOverride.getSomeList()).hasSize(1);

    OverrideUtil.setValue(toOverride, "someList[1].strVal", overriddenValue);

    assertThat(toOverride.getSomeList()).isNotNull();
    assertThat(toOverride.getSomeList()).hasSize(2);
    assertThat(toOverride.getSomeList().get(0).getStrVal()).isEqualTo("element1");
    assertThat(toOverride.getSomeList().get(1).getStrVal()).isEqualTo(overriddenValue);
  }

  @Test
  public void setValueSimpleCollectionInComplexCollectionExtended() {
    final String overriddenValue = "overridden test value";

    final ToOverride toOverride = new ToOverride();
    final List<String> otherList = new ArrayList<>();
    otherList.add("otherElement1");

    final ToOverride.OtherTestClass otherTestClass = new ToOverride.OtherTestClass();
    otherTestClass.setOtherList(otherList);

    final List<ToOverride.OtherTestClass> someList = new ArrayList<>();
    someList.add(otherTestClass);

    toOverride.setSomeList(someList);

    assertThat(toOverride.getSomeList()).hasSize(1);
    assertThat(toOverride.getSomeList().get(0).getOtherList()).hasSize(1);

    OverrideUtil.setValue(toOverride, "someList[0].otherList[1]", overriddenValue);

    assertThat(toOverride.getSomeList()).isNotNull();
    assertThat(toOverride.getSomeList()).hasSize(1);
    assertThat(toOverride.getSomeList().get(0)).isNotNull();
    assertThat(toOverride.getSomeList().get(0).getOtherList()).hasSize(2);

    assertThat(toOverride.getSomeList().get(0).getOtherList().get(0)).isEqualTo("otherElement1");
    assertThat(toOverride.getSomeList().get(0).getOtherList().get(1)).isEqualTo(overriddenValue);
  }

  @Test
  public void setValueMapPropertyAdded() {
    final HashMap<String, String> toOverride = new HashMap<>();

    OverrideUtil.setValue(toOverride, "property", "value");

    assertThat(toOverride).hasSize(1);
    assertThat(toOverride).contains(entry("property", "value"));
  }

  @Test
  public void setValueMapPeriodSeparatedPropertyAdded() {
    final HashMap<String, String> toOverride = new HashMap<>();

    OverrideUtil.setValue(toOverride, "property.subproperty", "value");

    assertThat(toOverride).hasSize(1);
    assertThat(toOverride).contains(entry("property.subproperty", "value"));
  }

  @Test
  public void setValueMapPropertyReplaced() {
    final HashMap<String, String> toOverride = new HashMap<>();
    toOverride.put("property", "initial value");

    assertThat(toOverride).hasSize(1);
    assertThat(toOverride).contains(entry("property", "initial value"));

    OverrideUtil.setValue(toOverride, "property", "updated value");

    assertThat(toOverride).hasSize(1);
    assertThat(toOverride).contains(entry("property", "updated value"));
  }

  @Test
  public void mapsAreNullByDefault() {
    Config config = OverrideUtil.createInstance(Config.class);
    assertThat(config.getEncryptor().getProperties()).isNull();
  }
}
