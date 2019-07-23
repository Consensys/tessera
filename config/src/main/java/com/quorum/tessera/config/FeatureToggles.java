package com.quorum.tessera.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
public class FeatureToggles {

    @XmlElement(defaultValue = "false")
    private boolean enableRemoteKeyValidation;

    public FeatureToggles() {}

    public boolean isEnableRemoteKeyValidation() {
        return enableRemoteKeyValidation;
    }

    public void setEnableRemoteKeyValidation(final boolean enableRemoteKeyValidation) {
        this.enableRemoteKeyValidation = enableRemoteKeyValidation;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof FeatureToggles)) {
            return false;
        }

        final FeatureToggles that = (FeatureToggles) o;
        return isEnableRemoteKeyValidation() == that.isEnableRemoteKeyValidation();
    }

    @Override
    public int hashCode() {
        return Objects.hash(isEnableRemoteKeyValidation());
    }
}
