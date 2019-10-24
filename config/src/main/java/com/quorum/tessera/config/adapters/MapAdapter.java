package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.adapters.MapAdapter.ConfigProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;

public class MapAdapter extends XmlAdapter<ConfigProperties, Map<String,String>> {

    @Override
    public Map<String, String> unmarshal(ConfigProperties configProperties) throws Exception {
        if(configProperties == null) return null;
        return configProperties.properties.stream()
                .collect(Collectors.toMap(p -> p.getName().getLocalPart(), p -> p.getValue()));
    }

    @Override
    public ConfigProperties marshal(Map<String, String> map) throws Exception {
        if(map == null) return null;
        
        ConfigProperties configProperties = new ConfigProperties();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            configProperties.properties.add(new JAXBElement<>(new QName(entry.getKey()), String.class, entry.getValue()));
        }

        return configProperties;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class ConfigProperties {
        @XmlAnyElement
        private List<JAXBElement<String>> properties = new ArrayList<>();

        ConfigProperties() {}

        protected List<JAXBElement<String>> getProperties() {
            return Collections.unmodifiableList(properties);
        }

        protected void setProperties(List<JAXBElement<String>> properties) {
            this.properties = properties;
        }
    }
}
