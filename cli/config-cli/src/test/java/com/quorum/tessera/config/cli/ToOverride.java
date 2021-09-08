package com.quorum.tessera.config.cli;

import jakarta.xml.bind.annotation.XmlElement;
import java.util.List;

class ToOverride {
  @XmlElement(name = "some_value")
  private String someValue;

  @XmlElement private String otherValue;

  @XmlElement private OtherTestClass complexProperty;

  @XmlElement private List<OtherTestClass> someList;

  @XmlElement private List<String> simpleList;

  String getSomeValue() {
    return someValue;
  }

  String getOtherValue() {
    return otherValue;
  }

  List<String> getSimpleList() {
    return simpleList;
  }

  OtherTestClass getComplexProperty() {
    return complexProperty;
  }

  List<OtherTestClass> getSomeList() {
    return someList;
  }

  void setSomeValue(String someValue) {
    this.someValue = someValue;
  }

  void setOtherValue(String otherValue) {
    this.otherValue = otherValue;
  }

  void setSomeList(List<OtherTestClass> someList) {
    this.someList = someList;
  }

  void setSimpleList(List<String> simpleList) {
    this.simpleList = simpleList;
  }

  void setComplexProperty(OtherTestClass otherTestClass) {
    complexProperty = otherTestClass;
  }

  static class OtherTestClass {
    @XmlElement private int count;

    @XmlElement private String strVal;

    @XmlElement private List<String> otherList;

    int getCount() {
      return count;
    }

    void setCount(int count) {
      this.count = count;
    }

    String getStrVal() {
      return strVal;
    }

    void setStrVal(String strVal) {
      this.strVal = strVal;
    }

    List<String> getOtherList() {
      return otherList;
    }

    void setOtherList(List<String> otherList) {
      this.otherList = otherList;
    }
  }
}
