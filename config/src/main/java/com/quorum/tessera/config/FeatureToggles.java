package com.quorum.tessera.config;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class FeatureToggles {

  @XmlElement(defaultValue = "false")
  private boolean enableRemoteKeyValidation;

  @XmlElement(defaultValue = "false")
  private boolean enablePrivacyEnhancements;

  @XmlElement(defaultValue = "false")
  private boolean enableMultiplePrivateStates;

  public FeatureToggles() {}

  public boolean isEnableRemoteKeyValidation() {
    return enableRemoteKeyValidation;
  }

  public void setEnableRemoteKeyValidation(final boolean enableRemoteKeyValidation) {
    this.enableRemoteKeyValidation = enableRemoteKeyValidation;
  }

  public boolean isEnablePrivacyEnhancements() {
    return enablePrivacyEnhancements;
  }

  public void setEnablePrivacyEnhancements(boolean enablePrivacyEnhancements) {
    this.enablePrivacyEnhancements = enablePrivacyEnhancements;
  }

  public boolean isEnableMultiplePrivateStates() {
    return enableMultiplePrivateStates;
  }

  public void setEnableMultiplePrivateStates(boolean enableMultiplePrivateStates) {
    this.enableMultiplePrivateStates = enableMultiplePrivateStates;
  }

  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof FeatureToggles)) {
      return false;
    }

    final FeatureToggles that = (FeatureToggles) o;
    return isEnableRemoteKeyValidation() == that.isEnableRemoteKeyValidation()
        && isEnablePrivacyEnhancements() == that.isEnablePrivacyEnhancements()
        && isEnableMultiplePrivateStates() == that.isEnableMultiplePrivateStates();
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        isEnableRemoteKeyValidation(),
        isEnablePrivacyEnhancements(),
        isEnableMultiplePrivateStates());
  }
}
