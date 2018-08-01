package com.quorum.tessera.config;

import java.lang.reflect.Field;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public abstract class ConfigItem {

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE) {
            @Override
            protected boolean accept(Field f) {
                return super.accept(f) && !f.getName().toLowerCase().contains("password");
            }
        }.toString();
    }

}
